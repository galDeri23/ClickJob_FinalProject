const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const logger = require("firebase-functions/logger");

initializeApp();

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