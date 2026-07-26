const {
  onDocumentWritten,
  onDocumentCreated,
  onDocumentUpdated,
} = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { setGlobalOptions } = require("firebase-functions/v2");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const logger = require("firebase-functions/logger");

initializeApp();
setGlobalOptions({ region: "me-west1" });

function formatDateTimeIL(date) {
  const parts = new Intl.DateTimeFormat("en-GB", {
    timeZone: "Asia/Jerusalem",
    day: "2-digit",
    month: "2-digit",
    year: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).formatToParts(date);
  const get = (type) => parts.find((p) => p.type === type).value;
  return `${get("day")}.${get("month")}.${get("year")} ${get("hour")}:${get("minute")}`;
}

// Returns the {year, month, day} a UTC timestamp falls on in Asia/Jerusalem.
function jerusalemDateParts(millis) {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: "Asia/Jerusalem",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(new Date(millis));
  const get = (type) => parseInt(parts.find((p) => p.type === type).value, 10);
  return { year: get("year"), month: get("month"), day: get("day") };
}

// Converts a Y/M/D H:M wall-clock time in Asia/Jerusalem to a UTC epoch ms.
function jerusalemWallClockToUtcMillis(year, month, day, hour, minute) {
  const guess = Date.UTC(year, month - 1, day, hour, minute);
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: "Asia/Jerusalem",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).formatToParts(new Date(guess));
  const get = (type) => parseInt(parts.find((p) => p.type === type).value, 10);
  const localAsUtc = Date.UTC(
    get("year"),
    get("month") - 1,
    get("day"),
    get("hour") % 24,
    get("minute")
  );
  return guess - (localAsUtc - guess);
}

// Mirrors the client's isShiftEnded: combines endDate (or date) with endTime,
// both interpreted in Asia/Jerusalem, since that's how the app writes them.
function shiftEndMillis(job) {
  const baseMillis = job.endDate && job.endDate !== 0 ? job.endDate : job.date;
  if (!baseMillis) return null;

  const { year, month, day } = jerusalemDateParts(baseMillis);
  const [hourStr, minuteStr] = (job.endTime || "").split(":");
  const hour = parseInt(hourStr, 10) || 0;
  const minute = parseInt(minuteStr, 10) || 0;

  return jerusalemWallClockToUtcMillis(year, month, day, hour, minute);
}

// Mirrors the client's createRatingNotificationsForJob.
async function createRatingNotificationsForJob(db, jobId, job) {
  const applicationsSnap = await db
    .collection("applications")
    .where("jobId", "==", jobId)
    .get();

  const applications = applicationsSnap.docs
    .map((doc) => ({ ...doc.data(), id: doc.id }))
    .filter((a) => a.status === "confirmed" || a.status === "arrived");

  const batch = db.batch();

  applications.forEach((application) => {
    const employerRef = db.collection("notifications").doc();
    batch.set(employerRef, {
      id: employerRef.id,
      userId: job.employerId,
      role: "employer",
      type: "RATING",
      title: `דירוג העובד "${application.workerName}"`,
      dateTime: "דירוגים מסייעים למצוא עובדים מתאימים",
      jobId,
      applicationId: application.id,
      isRead: false,
      createdAt: Date.now(),
    });

    if (application.status === "arrived") {
      const workerRef = db.collection("notifications").doc();
      batch.set(workerRef, {
        id: workerRef.id,
        userId: application.workerId,
        role: "worker",
        type: "RATING",
        title: `דירוג עבודה ב"${job.company}"`,
        dateTime: "דירוגים מעלים את הסיכוי למצוא עבודה",
        jobId,
        applicationId: application.id,
        isRead: false,
        createdAt: Date.now(),
      });
    }
  });

  batch.update(db.collection("jobs").doc(jobId), {
    ratingNotificationsSent: true,
  });

  await batch.commit();
}

/**
 * Sends a push whenever a notification document is created,
 * or meaningfully updated (e.g. the candidates counter grows).
 */
exports.sendNotificationPush = onDocumentWritten(
  "notifications/{notifId}",
  async (event) => {
    const before = event.data.before.exists ? event.data.before.data() : null;
    const after = event.data.after.exists ? event.data.after.data() : null;

    // Document was deleted — nothing to send
    if (!after) return;

    // Already read — don't push (this also skips markNotificationsAsRead updates)
    if (after.isRead === true) return;

    // Decide whether this write deserves a push:
    // 1. brand new notification
    // 2. the text changed (our candidates counter going 5 -> 6)
    // 3. it was marked unread again
    const isNew = !before;
    const textChanged = before && before.title !== after.title;
    const becameUnread = before && before.isRead === true && after.isRead === false;

    if (!isNew && !textChanged && !becameUnread) return;

    const userId = after.userId;
    if (!userId) return;

    // Look up the recipient's device token
    const userDoc = await getFirestore()
      .collection("candidates")
      .doc(userId)
      .get();

    const token = userDoc.exists ? userDoc.data().fcmToken : null;

    if (!token) {
      logger.log(`No fcmToken for user ${userId} — skipping push`);
      return;
    }

    const message = {
      token: token,
      notification: {
        title: after.title || "ClickJob",
        body: after.dateTime || "",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "clickjob_notifications",
        },
      },
      data: {
        jobId: after.jobId || "",
        role: after.role || "",
      },
    };

    try {
      await getMessaging().send(message);
      logger.log(`Push sent to ${userId}: ${after.title}`);
    } catch (error) {
      logger.error(`Push failed for ${userId}: ${error.code}`);

      // Token is dead (app uninstalled / reinstalled) — clean it up
      if (
        error.code === "messaging/registration-token-not-registered" ||
        error.code === "messaging/invalid-registration-token"
      ) {
        await getFirestore()
          .collection("candidates")
          .doc(userId)
          .update({ fcmToken: null });
      }
    }
  }
);

/**
 * Notifies the employer when a new application comes in, replacing the
 * per-applicant notification with a single running-count notification
 * (document id candidates_{jobId}).
 */
exports.notifyEmployerOnNewApplication = onDocumentCreated(
  "applications/{applicationId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const application = snap.data();
    const jobId = application.jobId;
    if (!jobId) return;

    const db = getFirestore();

    const jobDoc = await db.collection("jobs").doc(jobId).get();
    if (!jobDoc.exists) return;
    const job = jobDoc.data();

    const pendingSnap = await db
      .collection("applications")
      .where("jobId", "==", jobId)
      .where("status", "==", "pending")
      .get();

    const count = pendingSnap.size;
    if (count === 0) return;

    const notifId = `candidates_${jobId}`;
    const title =
      count === 1
        ? `מועמד חדש נרשם למשרה "${job.title}"`
        : `${count} מועמדים נרשמו למשרה "${job.title}"`;

    await db
      .collection("notifications")
      .doc(notifId)
      .set({
        id: notifId,
        userId: job.employerId,
        role: "employer",
        type: "ALERT",
        title,
        dateTime: formatDateTimeIL(new Date()),
        jobId,
        isRead: false,
        createdAt: Date.now(),
      });
  }
);

/**
 * Fires when a worker's application status flips to "arrived" (QR scan).
 * Notifies the worker and the employer, and — once every worker needed for
 * the job has arrived — sends the employer a single CONFIRMED notification.
 * Rating requests are NOT sent here; those come only from the shift-end
 * sweep, so a job never produces two rating requests.
 */
exports.notifyOnWorkerArrival = onDocumentUpdated(
  "applications/{applicationId}",
  async (event) => {
    const before = event.data.before.data();
    const after = event.data.after.data();
    if (!before || !after) return;
    if (before.status === "arrived" || after.status !== "arrived") return;

    const jobId = after.jobId;
    if (!jobId) return;

    const db = getFirestore();
    const jobDoc = await db.collection("jobs").doc(jobId).get();
    if (!jobDoc.exists) return;
    const job = jobDoc.data();

    const applicationId = event.params.applicationId;
    const workerId = after.workerId;
    const workerName = after.workerName || "";
    const scanTime = formatDateTimeIL(new Date());

    const batch = db.batch();

    const workerNotifRef = db.collection("notifications").doc();
    batch.set(workerNotifRef, {
      id: workerNotifRef.id,
      userId: workerId,
      role: "worker",
      type: "WORKER_ARRIVED",
      title: `סריקה להתחלת העבודה ב"${job.company}"`,
      dateTime: `עבודה החלה ב-${scanTime}`,
      jobId,
      applicationId,
      isRead: false,
      createdAt: Date.now(),
    });

    const employerNotifRef = db.collection("notifications").doc();
    batch.set(employerNotifRef, {
      id: employerNotifRef.id,
      userId: job.employerId,
      role: "employer",
      type: "WORKER_ARRIVED",
      title: `סריקה להתחלת עבודה ב"${job.company}"`,
      dateTime: `${workerName} סרק/ה ב-${scanTime}`,
      jobId,
      applicationId,
      isRead: false,
      createdAt: Date.now(),
    });

    await batch.commit();

    const workersNeeded = job.workersNeeded;
    if (!workersNeeded || workersNeeded < 1) return;

    const arrivedSnap = await db
      .collection("applications")
      .where("jobId", "==", jobId)
      .where("status", "==", "arrived")
      .get();

    if (arrivedSnap.size >= workersNeeded) {
      const confirmedRef = db.collection("notifications").doc();
      await confirmedRef.set({
        id: confirmedRef.id,
        userId: job.employerId,
        role: "employer",
        type: "CONFIRMED",
        title: `כל העובדים הגיעו ל"${job.title}"`,
        dateTime: formatDateTimeIL(new Date()),
        jobId,
        isRead: false,
        createdAt: Date.now(),
      });
    }
  }
);

/**
 * Sweeps jobs whose shift has ended and creates the rating-request
 * notifications for both sides, replacing the client's per-launch scan of
 * every job in the system. ratingNotificationsSent guards against
 * duplicates, so running every 15 minutes is safe.
 */
exports.sweepFinishedShiftsForRatings = onSchedule(
  {
    schedule: "every 15 minutes",
    timeZone: "Asia/Jerusalem",
  },
  async () => {
    const db = getFirestore();
    const now = Date.now();

    const jobsSnap = await db.collection("jobs").get();

    for (const jobDoc of jobsSnap.docs) {
      const job = jobDoc.data();
      if (job.ratingNotificationsSent === true) continue;

      const endMillis = shiftEndMillis(job);
      if (endMillis === null || endMillis >= now) continue;

      try {
        await createRatingNotificationsForJob(db, jobDoc.id, job);
      } catch (error) {
        logger.error(`Rating sweep failed for job ${jobDoc.id}: ${error.message}`);
      }
    }
  }
);