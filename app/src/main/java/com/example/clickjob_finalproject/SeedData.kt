package com.example.clickjob_finalproject

import com.example.clickjob_finalproject.data.model.Application
import com.example.clickjob_finalproject.data.model.JobPost
import com.example.clickjob_finalproject.data.model.Notification
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger

object SeedData {

    private val db = FirebaseFirestore.getInstance()

    // ===== Real Firebase Auth users =====
    private const val GAL_ID    = "Bqp5ddpJXjd8A9XIXmYQnyTg0O03"
    private const val NOA_ID    = "b4DqRCqjFRaRCvw9Dww7hjjqW6t2"
    private const val YOSI_ID   = "deRhC5qnvUaoGHvL07OiqkfW5zV2"
    private const val DANA_ID   = "NAY1uD5jMEhoZpHWT5pZAe0DBMA2"
    private const val RON_ID    = "Zzj4RQROP3hslycmWP7UDEsuPH83"
    private const val MICHAL_ID = "W2hNEiyQE6UUu1wIn5qVAWdEUjf1"
    private const val AVI_ID    = "e2O0D8DhwaV8BdS4cgNF5RnKsku1"

    // ===== Filler workers (Firestore profiles only) =====
    private const val TOMER_ID = "seed_worker_tomer"
    private const val LIHI_ID  = "seed_worker_lihi"
    private const val OR_ID    = "seed_worker_or"

    private const val DAY = 24 * 60 * 60 * 1000L
    private const val HOUR = 60 * 60 * 1000L

    /**
     * Call this single function from a temporary admin/debug button.
     * Users are written first, so applications never load empty worker profiles.
     */
    fun seedAll() {
        SeedUsers.seedAll {
            seedJobs { jobIds ->
                seedApplicationsAndNotifications(jobIds)
            }
        }
    }

    // Formats a notification dateTime line from the job's timestamp and start time
    private fun shiftLine(dateMillis: Long, startTime: String): String {
        val dateStr = SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date(dateMillis))
        return "עבודה ביום $dateStr $startTime"
    }

    private fun seedJobs(onComplete: (Map<String, String>) -> Unit) {
        val now = System.currentTimeMillis()

        val jobs = mapOf(

            // =====================================================
            // ===== דנה / מסעדת הים - 2 משרות הדמו (מכשיר ב') =====
            // =====================================================

            // Demo job 1: TODAY, Gal is confirmed -> QR scan demo
            "dana_qr" to JobPost(
                employerId = DANA_ID,
                title = "ברמן/ית למשמרת ערב",
                company = "מסעדת הים",
                category = "מסעדות",
                salary = "65", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now, endDate = now,
                startTime = "18:00", endTime = "23:30",
                workersNeeded = 2, workersRegistered = 1,
                description = "דרוש/ה ברמן/ית למשמרת ערב במסעדת הים. הכנת קוקטיילים ושירות בבר.",
                requirements = listOf("ניסיון כברמן/ית", "שירותיות", "עבודה בלחץ"),
                phone = "0541234567", address = "נמל תל אביב, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800",
                isUrgent = true
            ),

            // Demo job 2: OPEN with 4 pending applicants.
            // In the demo, Gal applies live as the 5th -> the "%5" employer
            // notification fires in real time on Dana's device!
            "dana_open" to JobPost(
                employerId = DANA_ID,
                title = "מלצר/ית לסוף השבוע",
                company = "מסעדת הים",
                category = "מסעדות",
                salary = "58", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 3 * DAY, endDate = now + 3 * DAY,
                startTime = "19:00", endTime = "01:00",
                workersNeeded = 3, workersRegistered = 0,
                description = "דרושים/ות מלצרים/ות למשמרת סוף שבוע עמוסה במסעדת הים. אווירה נעימה וטיפים מעולים.",
                requirements = listOf("ניסיון במלצרות", "זמינות בסופ\"ש", "יחסי אנוש מעולים"),
                phone = "0541234567", address = "נמל תל אביב, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = false
            ),

            // =====================================================
            // ===== גל דרעי כמעסיקה - 2 משרות ==================
            // =====================================================

            // Active job with a mix of applicants for the sorting screen
            "gal_active" to JobPost(
                employerId = GAL_ID,
                title = "בריסטה לקפה בוקר",
                company = "קפה גל",
                category = "מסעדות",
                salary = "52", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 4 * DAY, endDate = now + 4 * DAY,
                startTime = "07:00", endTime = "13:00",
                workersNeeded = 2, workersRegistered = 1,
                description = "דרוש/ה בריסטה למשמרת בוקר בקפה גל. הכנת קפה איכותי ושירות לקוחות.",
                requirements = listOf("ניסיון כבריסטה", "חייכנות", "הגעה בזמן"),
                phone = "0545595563", address = "אבן גבירול 45, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800",
                isUrgent = false
            ),

            // Finished job -> employer history + rating notification for Gal
            "gal_history" to JobPost(
                employerId = GAL_ID,
                title = "מלצר/ית לאירוע השקה",
                company = "קפה גל",
                category = "הפקה ואירועים",
                salary = "60", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 6 * DAY, endDate = now - 6 * DAY,
                startTime = "18:00", endTime = "23:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "אירוע השקה של תפריט חדש בקפה גל.",
                requirements = listOf("ניסיון בשירות", "מראה ייצוגי"),
                phone = "0545595563", address = "אבן גבירול 45, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),

            // =====================================================
            // ===== דנה - מסעדת הים (משרות רגילות) ===============
            // =====================================================
            "dana_1" to JobPost(
                employerId = DANA_ID,
                title = "מלצר/ית לאירוע חתונה",
                company = "מסעדת הים",
                category = "מסעדות",
                salary = "60", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 2 * DAY, endDate = now + 2 * DAY,
                startTime = "18:00", endTime = "23:00",
                workersNeeded = 2, workersRegistered = 2,
                description = "דרוש/ה מלצר/ית מנוסה לאירוע חתונה יוקרתי בתל אביב. שירות ברמה גבוהה.",
                requirements = listOf("ניסיון בשירות", "מראה מטופח", "אנגלית בסיסית"),
                phone = "0541234567", address = "נמל תל אביב, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = true
            ),
            "dana_2" to JobPost(
                employerId = DANA_ID,
                title = "טבח/ית סו שף",
                company = "מסעדת הים",
                category = "מסעדות",
                salary = "80", salaryType = "hourly",
                workFrequency = "רציף",
                date = now + 4 * DAY, endDate = now + 18 * DAY,
                startTime = "10:00", endTime = "18:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה טבח/ית מנוסה למסעדת דגים יוקרתית בתל אביב. עבודה רציפה לאורך תקופה.",
                requirements = listOf("ניסיון במטבח מקצועי", "תעודת בריאות", "יכולת עבודה בלחץ"),
                phone = "0541234567", address = "נמל תל אביב, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800",
                isUrgent = false
            ),
            "dana_history_1" to JobPost(
                employerId = DANA_ID,
                title = "מלצר/ית לאירוע פרטי",
                company = "מסעדת הים",
                category = "מסעדות",
                salary = "55", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 5 * DAY, endDate = now - 5 * DAY,
                startTime = "19:00", endTime = "23:00",
                workersNeeded = 2, workersRegistered = 2,
                description = "אירוע פרטי ליום הולדת.",
                requirements = listOf("ניסיון בשירות"),
                phone = "0541234567", address = "הרצליה פיתוח, הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),
            "dana_history_2" to JobPost(
                employerId = DANA_ID,
                title = "טבח/ית לאירוע חברה",
                company = "מסעדת הים",
                category = "מסעדות",
                salary = "70", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 10 * DAY, endDate = now - 10 * DAY,
                startTime = "12:00", endTime = "20:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "אירוע חברה גדול.",
                requirements = listOf("ניסיון במטבח", "תעודת בריאות"),
                phone = "0541234567", address = "נמל תל אביב, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),

            // =====================================================
            // ===== רון - אולם האירועים רויאל =====================
            // =====================================================
            "ron_1" to JobPost(
                employerId = RON_ID,
                title = "אבטח/ת לפסטיבל מוזיקה",
                company = "אולם האירועים רויאל",
                category = "אבטחה וביטחון",
                salary = "70", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 1 * DAY, endDate = now + 1 * DAY,
                startTime = "20:00", endTime = "02:00",
                workersNeeded = 3, workersRegistered = 3,
                description = "דרוש/ה אבטח/ת לפסטיבל מוזיקה גדול. חובה רישיון אבטחה בתוקף.",
                requirements = listOf("רישיון אבטחה", "כושר גופני", "ניסיון"),
                phone = "0551234567", address = "ז'בוטינסקי 33, רמת גן",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = true
            ),
            "ron_2" to JobPost(
                employerId = RON_ID,
                title = "צלמ/ת לאירוע בר מצווה",
                company = "אולם האירועים רויאל",
                category = "עיצוב וקריאייטיב",
                salary = "120", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 6 * DAY, endDate = now + 6 * DAY,
                startTime = "17:00", endTime = "23:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה צלמ/ת מקצועי/ת לאירוע בר מצווה. ניסיון בצילום אירועים חובה.",
                requirements = listOf("ניסיון בצילום אירועים", "ציוד מקצועי", "תיק עבודות"),
                phone = "0551234567", address = "העצמאות 12, פתח תקווה",
                imageUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800",
                isUrgent = false
            ),
            "ron_3" to JobPost(
                employerId = RON_ID,
                title = "מנחה/ת אירוע",
                company = "אולם האירועים רויאל",
                category = "הפקה ואירועים",
                salary = "200", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 3 * DAY, endDate = now + 3 * DAY,
                startTime = "18:00", endTime = "23:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מנחה/ת אירוע מנוסה לחתונה גדולה. ניסיון בהנחיית אירועים חובה.",
                requirements = listOf("ניסיון בהנחיה", "כריזמה", "עברית ברמה גבוהה"),
                phone = "0551234567", address = "רוטשילד 22, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = true
            ),
            "ron_4" to JobPost(
                employerId = RON_ID,
                title = "DJ לאירוע",
                company = "אולם האירועים רויאל",
                category = "הפקה ואירועים",
                salary = "300", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 8 * DAY, endDate = now + 8 * DAY,
                startTime = "20:00", endTime = "02:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה DJ מנוסה לחתונה. סגנון מוזיקה: פופ ישראלי, מזרחי.",
                requirements = listOf("ניסיון כ-DJ", "ציוד מקצועי", "תיק עבודות"),
                phone = "0551234567", address = "ז'בוטינסקי 33, רמת גן",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = false
            ),
            "ron_history_1" to JobPost(
                employerId = RON_ID,
                title = "מלצר/ית לאירוע קורפורייט",
                company = "אולם האירועים רויאל",
                category = "מסעדות",
                salary = "55", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 3 * DAY, endDate = now - 3 * DAY,
                startTime = "18:00", endTime = "23:00",
                workersNeeded = 3, workersRegistered = 3,
                description = "אירוע קורפורייט לחברת הייטק.",
                requirements = listOf("ניסיון", "אנגלית"),
                phone = "0551234567", address = "רוטשילד 22, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),
            "ron_history_2" to JobPost(
                employerId = RON_ID,
                title = "אבטח/ת להופעה",
                company = "אולם האירועים רויאל",
                category = "אבטחה וביטחון",
                salary = "65", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 7 * DAY, endDate = now - 7 * DAY,
                startTime = "19:00", endTime = "01:00",
                workersNeeded = 2, workersRegistered = 2,
                description = "הופעה של אמן מפורסם.",
                requirements = listOf("רישיון אבטחה"),
                phone = "0551234567", address = "רוטשילד 22, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),
            "ron_history_3" to JobPost(
                employerId = RON_ID,
                title = "צלמ/ת לאירוע סיום",
                company = "אולם האירועים רויאל",
                category = "עיצוב וקריאייטיב",
                salary = "100", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 12 * DAY, endDate = now - 12 * DAY,
                startTime = "17:00", endTime = "22:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "צילום אירוע סיום שנה.",
                requirements = listOf("ניסיון בצילום", "ציוד מקצועי"),
                phone = "0551234567", address = "הנמל 5, חיפה",
                imageUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),

            // =====================================================
            // ===== מיכל - TechStart ==============================
            // =====================================================
            "michal_1" to JobPost(
                employerId = MICHAL_ID,
                title = "מפתח/ת Frontend",
                company = "TechStart",
                category = "טכנולוגיה",
                salary = "150", salaryType = "hourly",
                workFrequency = "רציף",
                date = now + 3 * DAY, endDate = now + 30 * DAY,
                startTime = "09:00", endTime = "17:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "דרוש/ה מפתח/ת Frontend לפרויקט קצר טווח. ניסיון ב-React חובה.",
                requirements = listOf("React", "JavaScript", "CSS", "ניסיון של שנתיים לפחות"),
                phone = "0561234567", address = "מדינת היהודים 60, הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
                isUrgent = true
            ),
            "michal_2" to JobPost(
                employerId = MICHAL_ID,
                title = "מנהל/ת מדיה חברתית",
                company = "TechStart",
                category = "שירות לקוחות",
                salary = "60", salaryType = "hourly",
                workFrequency = "רציף",
                date = now + 5 * DAY, endDate = now + 21 * DAY,
                startTime = "10:00", endTime = "14:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מנהל/ת מדיה חברתית לחברת סטארטאפ.",
                requirements = listOf("ניסיון במדיה חברתית", "יצירתיות", "כתיבה שיווקית"),
                phone = "0561234567", address = "מדינת היהודים 60, הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1553775282-20af80779df7?w=800",
                isUrgent = false
            ),
            "michal_3" to JobPost(
                employerId = MICHAL_ID,
                title = "מעצב/ת UX/UI",
                company = "TechStart",
                category = "עיצוב וקריאייטיב",
                salary = "130", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 9 * DAY, endDate = now + 9 * DAY,
                startTime = "09:00", endTime = "17:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מעצב/ת UX/UI לפרויקט אפליקציה מובייל.",
                requirements = listOf("Figma", "ניסיון בעיצוב מובייל", "תיק עבודות"),
                phone = "0561234567", address = "רוטשילד 45, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1561070791-2526d30994b5?w=800",
                isUrgent = false
            ),
            "michal_history_1" to JobPost(
                employerId = MICHAL_ID,
                title = "QA Tester",
                company = "TechStart",
                category = "טכנולוגיה",
                salary = "100", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 4 * DAY, endDate = now - 4 * DAY,
                startTime = "09:00", endTime = "17:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "בדיקות איכות לאפליקציה מובייל.",
                requirements = listOf("ניסיון ב-QA", "אנגלית"),
                phone = "0561234567", address = "מדינת היהודים 60, הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),
            "michal_history_2" to JobPost(
                employerId = MICHAL_ID,
                title = "מפתח/ת Backend",
                company = "TechStart",
                category = "טכנולוגיה",
                salary = "160", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 8 * DAY, endDate = now - 8 * DAY,
                startTime = "09:00", endTime = "17:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "פיתוח Backend לפרויקט.",
                requirements = listOf("Node.js", "Python", "ניסיון של 3 שנים"),
                phone = "0561234567", address = "מדינת היהודים 60, הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),

            // =====================================================
            // ===== אבי - EduCenter / PetCare / SpeedEx ===========
            // =====================================================
            "avi_1" to JobPost(
                employerId = AVI_ID,
                title = "מורה לאנגלית",
                company = "EduCenter",
                category = "חינוך והוראה",
                salary = "90", salaryType = "hourly",
                workFrequency = "רציף",
                date = now + 2 * DAY, endDate = now + 16 * DAY,
                startTime = "16:00", endTime = "20:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מורה לאנגלית לתלמידי תיכון. ניסיון בהוראה חובה.",
                requirements = listOf("תואר רלוונטי", "ניסיון בהוראה", "אנגלית ברמת שפת אם"),
                phone = "0571234567", address = "סוקולוב 48, הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800",
                isUrgent = false
            ),
            "avi_2" to JobPost(
                employerId = AVI_ID,
                title = "שליח/ה למשלוחים",
                company = "SpeedEx",
                category = "משלוחים ותחבורה",
                salary = "50", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 1 * DAY, endDate = now + 1 * DAY,
                startTime = "09:00", endTime = "17:00",
                workersNeeded = 2, workersRegistered = 1,
                description = "דרוש/ה שליח/ה עם רכב פרטי למשלוחים באזור המרכז.",
                requirements = listOf("רכב פרטי", "רישיון נהיגה"),
                phone = "0571234567", address = "המסגר 20, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800",
                isUrgent = true
            ),
            "avi_3" to JobPost(
                employerId = AVI_ID,
                title = "מטפל/ת בכלבים",
                company = "PetCare",
                category = "בעלי חיים",
                salary = "55", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 3 * DAY, endDate = now + 3 * DAY,
                startTime = "08:00", endTime = "14:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה מטפל/ת בכלבים לעסק מטיילי כלבים. אהבה לבעלי חיים חובה.",
                requirements = listOf("אהבה לבעלי חיים", "אחריות", "כושר גופני"),
                phone = "0571234567", address = "יהודה הלוי 15, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800",
                isUrgent = false
            ),
            "avi_4" to JobPost(
                employerId = AVI_ID,
                title = "עוזר/ת וטרינר",
                company = "PetCare",
                category = "בעלי חיים",
                salary = "65", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 5 * DAY, endDate = now + 5 * DAY,
                startTime = "09:00", endTime = "15:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה עוזר/ת לקליניקה וטרינרית. ניסיון עם בעלי חיים יתרון.",
                requirements = listOf("אהבה לבעלי חיים", "יחסי אנוש", "אחריות"),
                phone = "0571234567", address = "ביאליק 8, רמת גן",
                imageUrl = "https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?w=800",
                isUrgent = true
            ),
            "avi_5" to JobPost(
                employerId = AVI_ID,
                title = "סייע/ת לגן ילדים",
                company = "גן הפרחים",
                category = "חינוך והוראה",
                salary = "45", salaryType = "hourly",
                workFrequency = "רציף",
                date = now + 4 * DAY, endDate = now + 25 * DAY,
                startTime = "07:30", endTime = "13:30",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה סייע/ת לגן ילדים. אוהב/ת ילדים, סבלני/ת ואחראי/ת.",
                requirements = listOf("אהבה לילדים", "סבלנות", "תעודת סייע/ת יתרון"),
                phone = "0571234567", address = "ויצמן 30, כפר סבא",
                imageUrl = "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=800",
                isUrgent = false
            ),
            "avi_history_1" to JobPost(
                employerId = AVI_ID,
                title = "נהג/ת להסעות",
                company = "SpeedEx",
                category = "משלוחים ותחבורה",
                salary = "55", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 6 * DAY, endDate = now - 6 * DAY,
                startTime = "07:00", endTime = "15:00",
                workersNeeded = 2, workersRegistered = 2,
                description = "הסעת עובדים.",
                requirements = listOf("רישיון ב", "ניסיון נהיגה"),
                phone = "0571234567", address = "רגר 10, באר שבע",
                imageUrl = "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),
            "avi_history_2" to JobPost(
                employerId = AVI_ID,
                title = "מורה לגיטרה",
                company = "EduCenter",
                category = "חינוך והוראה",
                salary = "80", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 9 * DAY, endDate = now - 9 * DAY,
                startTime = "15:00", endTime = "19:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "שיעורי גיטרה לילדים ומבוגרים.",
                requirements = listOf("ניסיון בהוראת מוזיקה", "סבלנות"),
                phone = "0571234567", address = "סוקולוב 48, הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),
            "avi_history_3" to JobPost(
                employerId = AVI_ID,
                title = "מטפל/ת בחתולים",
                company = "PetCare",
                category = "בעלי חיים",
                salary = "50", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now - 14 * DAY, endDate = now - 14 * DAY,
                startTime = "09:00", endTime = "13:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "טיפול בחתולים בבית הלקוח.",
                requirements = listOf("אהבה לחתולים", "אחריות"),
                phone = "0571234567", address = "יהודה הלוי 15, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?w=800",
                isUrgent = false,
                ratingNotificationsSent = true
            ),

            // =====================================================
            // ===== משרות נוספות - שהעולם ייראה חי ===============
            // =====================================================
            "extra_1" to JobPost(
                employerId = DANA_ID,
                title = "עוזר/ת אחות",
                company = "מרפאת השרון",
                category = "רפואה ובריאות",
                salary = "70", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 2 * DAY, endDate = now + 2 * DAY,
                startTime = "08:00", endTime = "14:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה עוזר/ת אחות למרפאה פרטית באזור השרון.",
                requirements = listOf("הסמכה רלוונטית", "ניסיון", "יחסי אנוש טובים"),
                phone = "0541234567", address = "הרצל 55, נתניה",
                imageUrl = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800",
                isUrgent = false
            ),
            "extra_2" to JobPost(
                employerId = RON_ID,
                title = "מאבטח/ת קניון",
                company = "SafeMall",
                category = "אבטחה וביטחון",
                salary = "55", salaryType = "hourly",
                workFrequency = "רציף",
                date = now + 3 * DAY, endDate = now + 17 * DAY,
                startTime = "14:00", endTime = "22:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה מאבטח/ת לקניון. משמרות ערב. רישיון אבטחה חובה.",
                requirements = listOf("רישיון אבטחה", "ניסיון", "אמינות"),
                phone = "0551234567", address = "העצמאות 1, אשדוד",
                imageUrl = "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?w=800",
                isUrgent = false
            ),
            "extra_3" to JobPost(
                employerId = MICHAL_ID,
                title = "נציג/ת שירות לקוחות",
                company = "TechStart",
                category = "שירות לקוחות",
                salary = "42", salaryType = "hourly",
                workFrequency = "רציף",
                date = now + 4 * DAY, endDate = now + 32 * DAY,
                startTime = "08:00", endTime = "16:00",
                workersNeeded = 3, workersRegistered = 0,
                description = "דרוש/ה נציג/ת שירות לקוחות למוקד טלפוני.",
                requirements = listOf("עברית ברמה גבוהה", "יחסי אנוש", "ניסיון יתרון"),
                phone = "0561234567", address = "הרצל 90, רחובות",
                imageUrl = "https://images.unsplash.com/photo-1553775282-20af80779df7?w=800",
                isUrgent = false
            ),
            "extra_4" to JobPost(
                employerId = AVI_ID,
                title = "פועל/ת בניין",
                company = "BuildRight",
                category = "בניין וייצור",
                salary = "60", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 2 * DAY, endDate = now + 2 * DAY,
                startTime = "07:00", endTime = "16:00",
                workersNeeded = 3, workersRegistered = 0,
                description = "דרוש/ה פועל/ת בניין לפרויקט בנייה חדש.",
                requirements = listOf("כושר גופני", "ניסיון בבניין", "אמינות"),
                phone = "0571234567", address = "דרך שלמה 100, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                isUrgent = true
            ),
            "extra_5" to JobPost(
                employerId = RON_ID,
                title = "מוכר/ת בחנות בגדים",
                company = "FashionStore",
                category = "מכירות ואופנה",
                salary = "40", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 1 * DAY, endDate = now + 1 * DAY,
                startTime = "10:00", endTime = "18:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה מוכר/ת לחנות בגדים במרכז קניות.",
                requirements = listOf("שירותיות", "ניסיון במכירות", "עברית ברמה גבוהה"),
                phone = "0551234567", address = "רוטשילד 10, ראשון לציון",
                imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800",
                isUrgent = true
            ),
            "extra_6" to JobPost(
                employerId = DANA_ID,
                title = "מטפל/ת בקשישים",
                company = "CareHome",
                category = "רפואה ובריאות",
                salary = "65", salaryType = "hourly",
                workFrequency = "רציף",
                date = now + 5 * DAY, endDate = now + 26 * DAY,
                startTime = "08:00", endTime = "16:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מטפל/ת בקשישים לבית אבות. ניסיון יתרון.",
                requirements = listOf("סבלנות", "אמפתיה", "ניסיון בטיפול"),
                phone = "0541234567", address = "ויצמן 12, נתניה",
                imageUrl = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800",
                isUrgent = false
            ),
            "extra_7" to JobPost(
                employerId = MICHAL_ID,
                title = "מנהל/ת לוגיסטיקה",
                company = "LogiPro",
                category = "אפסנאות ולוגיסטיקה",
                salary = "80", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 6 * DAY, endDate = now + 6 * DAY,
                startTime = "08:00", endTime = "16:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מנהל/ת לוגיסטיקה לחברת יבוא.",
                requirements = listOf("ניסיון בלוגיסטיקה", "Excel", "אנגלית"),
                phone = "0561234567", address = "האורגים 7, אשדוד",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800",
                isUrgent = false
            ),
            "extra_8" to JobPost(
                employerId = AVI_ID,
                title = "עובד/ת אחזקה",
                company = "MaintenancePro",
                category = "אחזקה",
                salary = "55", salaryType = "hourly",
                workFrequency = "חד פעמי",
                date = now + 3 * DAY, endDate = now + 3 * DAY,
                startTime = "08:00", endTime = "16:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה עובד/ת אחזקה לבניין משרדים.",
                requirements = listOf("ניסיון באחזקה", "כלים ידניים", "אמינות"),
                phone = "0571234567", address = "יגאל אלון 88, תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                isUrgent = false
            )
        )

        val jobIds = mutableMapOf<String, String>()
        var savedCount = 0
        val totalJobs = jobs.size

        jobs.forEach { (key, job) ->
            // Stable IDs make this seeder safe to run again without duplicating data.
            val ref = db.collection("jobs").document("seed_job_$key")
            val jobWithId = job.copy(id = ref.id)
            val metadata = mapOf<String, Any>(
                "seedData" to true,
                "seedKey" to key,
                "createdAt" to now - jobAgeDays(key) * DAY
            )

            ref.set(jobWithId)
                .continueWithTask { ref.update(metadata) }
                .addOnSuccessListener {
                    synchronized(jobIds) {
                        jobIds[key] = ref.id
                        savedCount++
                        if (savedCount == totalJobs) {
                            onComplete(jobIds)
                        }
                    }
                }
                .addOnFailureListener {
                    android.util.Log.e("SeedData", "Failed to save job $key: ${it.message}")
                }
        }
    }

    private fun seedApplicationsAndNotifications(jobIds: Map<String, String>) {
        loadWorkerProfiles { profiles ->

            updateGalJobMatches(jobIds)

            // Small helper to keep dateTime strings consistent with actual job dates
            fun line(startTime: String, baseOffsetDays: Long): String {
                val millis = System.currentTimeMillis() + baseOffsetDays * DAY
                return shiftLine(millis, startTime)
            }

            // =====================================================
            // ===== גל דרעי - צד עובד =============================
            // =====================================================

            // 1. CONFIRMED today at Dana's restaurant -> QR scan demo + upcoming shifts
            createApplication(jobIds["dana_qr"]!!, GAL_ID, DANA_ID, profiles[GAL_ID], "confirmed") { appId ->
                sendNotification(
                    GAL_ID, "worker", "CONFIRMED",
                    "התקבלת לעבודה ב\"מסעדת הים\"",
                    line("18:00", 0),
                    jobIds["dana_qr"]!!, appId,
                    hoursAgo = 5
                )
                sendNotification(
                    DANA_ID, "employer", "WORKER_CONFIRMED",
                    "גל דרעי אישר/ה את העבודה ב\"מסעדת הים\"",
                    line("18:00", 0),
                    jobIds["dana_qr"]!!, appId,
                    hoursAgo = 5
                )
            }
            updateUpcomingShift(GAL_ID, jobIds["dana_qr"]!!)

            // 2. EMPLOYER_APPROVED -> live double-check notification with buttons
            //    (this is the card Gal can confirm from any of the 3 paths)
            createApplication(jobIds["avi_1"]!!, GAL_ID, AVI_ID, profiles[GAL_ID], "employer_approved") { appId ->
                sendNotification(
                    GAL_ID, "worker", "CONFIRMED",
                    "נדרש אישור לעבודה ב\"EduCenter\"",
                    line("16:00", 2),
                    jobIds["avi_1"]!!, appId,
                    actionRequired = true,
                    hoursAgo = 1
                )
            }

            // 3. PENDING - waiting for employer response
            createApplication(jobIds["avi_3"]!!, GAL_ID, AVI_ID, profiles[GAL_ID], "pending")

            // 4. History: finished job -> worker history tab + rating notification
            createApplication(jobIds["avi_history_2"]!!, GAL_ID, AVI_ID, profiles[GAL_ID], "confirmed") { appId ->
                sendNotification(
                    GAL_ID, "worker", "RATING",
                    "דרגי את העבודה ב\"EduCenter\"",
                    "הדירוג שלך עוזר לעובדים אחרים לבחור נכון",
                    jobIds["avi_history_2"]!!, appId,
                    isRated = false,
                    hoursAgo = 24 * 8
                )
            }

            // 5. Old cancelled notification for flavor
            sendNotification(
                GAL_ID, "worker", "CANCELLED",
                "המשרה ב\"BuildRight\" בוטלה",
                "המעסיק ביטל את המשרה",
                jobIds["extra_4"]!!,
                hoursAgo = 24 * 3
            )

            // =====================================================
            // ===== גל דרעי - צד מעסיקה ===========================
            // =====================================================

            // Active job applicants: 3 pending, 1 waiting double-check, 1 accepted
            createApplication(jobIds["gal_active"]!!, TOMER_ID, GAL_ID, profiles[TOMER_ID], "pending")
            createApplication(jobIds["gal_active"]!!, LIHI_ID, GAL_ID, profiles[LIHI_ID], "pending")
            createApplication(jobIds["gal_active"]!!, OR_ID, GAL_ID, profiles[OR_ID], "pending")
            createApplication(jobIds["gal_active"]!!, YOSI_ID, GAL_ID, profiles[YOSI_ID], "employer_approved")
            createApplication(jobIds["gal_active"]!!, NOA_ID, GAL_ID, profiles[NOA_ID], "confirmed") { appId ->
                sendNotification(
                    GAL_ID, "employer", "WORKER_CONFIRMED",
                    "נועה טל אישר/ה את העבודה ב\"קפה גל\"",
                    line("07:00", 4),
                    jobIds["gal_active"]!!, appId,
                    hoursAgo = 3
                )
            }

            // Candidate alert for Gal-as-employer (ALERT -> shows the job page button)
            sendNotification(
                GAL_ID, "employer", "ALERT",
                "יש 5 מועמדים חדשים למשרה בריסטה לקפה בוקר",
                SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - 7 * HOUR)),
                jobIds["gal_active"]!!,
                hoursAgo = 7
            )

            // Finished employer job: worker confirmed + rating notification (with applicationId!)
            createApplication(jobIds["gal_history"]!!, TOMER_ID, GAL_ID, profiles[TOMER_ID], "arrived") { appId ->
                sendNotification(
                    GAL_ID, "employer", "RATING",
                    "דרגי את העובד \"תומר שגב\"",
                    "הדירוג שלך יסייע למעסיקים נוספים",
                    jobIds["gal_history"]!!, appId,
                    isRated = false,
                    hoursAgo = 24 * 5
                )
            }

            // =====================================================
            // ===== מסעדת הים - המשרה הפתוחה: 4 ממתינים ==========
            // ===== (גל מגישה בלייב כחמישית -> הודעה בזמן אמת!) ===
            // =====================================================
            createApplication(jobIds["dana_open"]!!, TOMER_ID, DANA_ID, profiles[TOMER_ID], "pending")
            createApplication(jobIds["dana_open"]!!, LIHI_ID, DANA_ID, profiles[LIHI_ID], "pending")
            createApplication(jobIds["dana_open"]!!, OR_ID, DANA_ID, profiles[OR_ID], "pending")
            createApplication(jobIds["dana_open"]!!, YOSI_ID, DANA_ID, profiles[YOSI_ID], "pending")

            // =====================================================
            // ===== שאר העולם - עובדים ==========================
            // =====================================================
            createApplication(jobIds["dana_1"]!!, YOSI_ID, DANA_ID, profiles[YOSI_ID], "confirmed")
            createApplication(jobIds["ron_1"]!!, YOSI_ID, RON_ID, profiles[YOSI_ID], "confirmed")
            updateUpcomingShift(YOSI_ID, jobIds["dana_1"]!!)
            updateUpcomingShift(YOSI_ID, jobIds["ron_1"]!!)

            createApplication(jobIds["michal_1"]!!, NOA_ID, MICHAL_ID, profiles[NOA_ID], "confirmed")
            createApplication(jobIds["dana_1"]!!, NOA_ID, DANA_ID, profiles[NOA_ID], "confirmed")
            updateUpcomingShift(NOA_ID, jobIds["michal_1"]!!)
            updateUpcomingShift(NOA_ID, jobIds["dana_1"]!!)

            createApplication(jobIds["ron_1"]!!, AVI_ID, RON_ID, profiles[AVI_ID], "confirmed")
            updateUpcomingShift(AVI_ID, jobIds["ron_1"]!!)

            // Pending applicants on other jobs so employers' sorting screens look alive
            createApplication(jobIds["avi_2"]!!, OR_ID, AVI_ID, profiles[OR_ID], "confirmed")
            updateUpcomingShift(OR_ID, jobIds["avi_2"]!!)
            createApplication(jobIds["dana_2"]!!, TOMER_ID, DANA_ID, profiles[TOMER_ID], "pending")
            createApplication(jobIds["extra_5"]!!, LIHI_ID, RON_ID, profiles[LIHI_ID], "pending")

            // =====================================================
            // ===== הודעות לשאר המשתמשים ========================
            // =====================================================

            // נועה
            sendNotification(NOA_ID, "worker", "CONFIRMED",
                "התקבלת לעבודה ב\"TechStart\"",
                line("09:00", 3), jobIds["michal_1"]!!, hoursAgo = 10)
            sendNotification(NOA_ID, "worker", "CONFIRMED",
                "התקבלת לעבודה ב\"מסעדת הים\"",
                line("18:00", 2), jobIds["dana_1"]!!, hoursAgo = 20)

            // יוסי
            sendNotification(YOSI_ID, "worker", "CONFIRMED",
                "התקבלת לעבודה ב\"מסעדת הים\"",
                line("18:00", 2), jobIds["dana_1"]!!, hoursAgo = 18)
            sendNotification(YOSI_ID, "worker", "CONFIRMED",
                "התקבלת לעבודה ב\"אולם האירועים רויאל\"",
                line("20:00", 1), jobIds["ron_1"]!!, hoursAgo = 26)

            // דנה (מעסיקה - מכשיר ב')
            sendNotification(DANA_ID, "employer", "ALERT",
                "יש 5 מועמדים חדשים למשרה מלצר/ית לאירוע חתונה",
                SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - 30 * HOUR)),
                jobIds["dana_1"]!!, hoursAgo = 30)
            sendNotification(DANA_ID, "employer", "WORKER_CONFIRMED",
                "יוסי כהן אישר/ה את העבודה ב\"מסעדת הים\"",
                line("18:00", 2), jobIds["dana_1"]!!, hoursAgo = 12)

            // רון (מעסיק)
            sendNotification(RON_ID, "employer", "ALERT",
                "יש 5 מועמדים חדשים למשרה אבטח/ת לפסטיבל מוזיקה",
                SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() - 40 * HOUR)),
                jobIds["ron_1"]!!, hoursAgo = 40)
            sendNotification(RON_ID, "employer", "WORKER_CONFIRMED",
                "יוסי כהן אישר/ה את העבודה ב\"אולם האירועים רויאל\"",
                line("20:00", 1), jobIds["ron_1"]!!, hoursAgo = 15)

            // מיכל (מעסיקה)
            sendNotification(MICHAL_ID, "employer", "WORKER_CONFIRMED",
                "נועה טל אישר/ה את העבודה ב\"TechStart\"",
                line("09:00", 3), jobIds["michal_1"]!!, hoursAgo = 9)

            // אבי (מעסיק)
            sendNotification(AVI_ID, "employer", "WORKER_CONFIRMED",
                "אור מזרחי אישר/ה את העבודה ב\"SpeedEx\"",
                line("09:00", 1), jobIds["avi_2"]!!, hoursAgo = 8)
        }
    }

    private fun loadWorkerProfiles(onComplete: (Map<String, Map<String, String>>) -> Unit) {
        val workerIds = listOf(GAL_ID, NOA_ID, YOSI_ID, AVI_ID, TOMER_ID, LIHI_ID, OR_ID)
        val profiles = mutableMapOf<String, Map<String, String>>()
        val completed = AtomicInteger(0)

        fun finishOne() {
            if (completed.incrementAndGet() == workerIds.size) {
                onComplete(profiles)
            }
        }

        workerIds.forEach { userId ->
            db.collection("candidates").document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    profiles[userId] = mapOf(
                        "name"            to (doc.getString("name") ?: ""),
                        "phone"           to (doc.getString("phone") ?: ""),
                        "profileImageUrl" to (doc.getString("profileImageUrl") ?: ""),
                        "jobCategory"     to ((doc.get("jobCategories") as? List<*>)?.firstOrNull()?.toString() ?: "")
                    )
                    finishOne()
                }
                .addOnFailureListener {
                    android.util.Log.e("SeedData", "Failed to load profile $userId: ${it.message}")
                    profiles[userId] = emptyMap()
                    finishOne()
                }
        }
    }

    private fun createApplication(
        jobId: String,
        workerId: String,
        employerId: String,
        profile: Map<String, String>?,
        status: String,
        onCreated: ((String) -> Unit)? = null
    ) {
        val ref = db.collection("applications")
            .document("seed_app_${jobId}_${workerId}")
        val application = Application(
            id = ref.id,
            jobId = jobId,
            workerId = workerId,
            employerId = employerId,
            workerName = profile?.get("name") ?: "",
            workerPhone = profile?.get("phone") ?: "",
            workerBio = profile?.get("jobCategory") ?: "",
            workerProfileImageUrl = profile?.get("profileImageUrl") ?: "",
            status = status
        )
        ref.set(application)
            .continueWithTask { ref.update(mapOf<String, Any>("seedData" to true)) }
            .addOnSuccessListener {
                onCreated?.invoke(ref.id)
            }
            .addOnFailureListener {
                android.util.Log.e("SeedData", "Failed to save application ${ref.id}: ${it.message}")
            }
    }

    private fun updateUpcomingShift(userId: String, jobId: String) {
        db.collection("candidates").document(userId)
            .update("upcomingShifts", FieldValue.arrayUnion(jobId))
    }

    // NOTE: isRated is now written explicitly so the rating button shows/hides
    // reliably regardless of the model's default value.
    private fun sendNotification(
        userId: String,
        role: String,
        type: String,
        title: String,
        dateTime: String,
        jobId: String,
        applicationId: String = "",
        actionRequired: Boolean = false,
        isRated: Boolean = false,
        hoursAgo: Long = 2
    ) {
        val rawId = listOf(userId, role, type, title, jobId, applicationId).joinToString("|")
        val ref = db.collection("notifications")
            .document(stableDocumentId("seed_notification", rawId))
        val notification = Notification(
            id = ref.id,
            userId = userId,
            role = role,
            type = type,
            title = title,
            dateTime = dateTime,
            jobId = jobId,
            applicationId = applicationId,
            actionRequired = actionRequired,
            isRated = isRated,
            createdAt = System.currentTimeMillis() - hoursAgo * HOUR
        )
        ref.set(notification)
            .continueWithTask { ref.update(mapOf<String, Any>("seedData" to true)) }
            .addOnFailureListener {
                android.util.Log.e("SeedData", "Failed to save notification ${ref.id}: ${it.message}")
            }
    }

    private fun updateGalJobMatches(jobIds: Map<String, String>) {
        val matches = listOf(
            mapOf("jobId" to jobIds.getValue("avi_4"), "score" to 96),
            mapOf("jobId" to jobIds.getValue("michal_1"), "score" to 92),
            mapOf("jobId" to jobIds.getValue("extra_1"), "score" to 88),
            mapOf("jobId" to jobIds.getValue("avi_5"), "score" to 85),
            mapOf("jobId" to jobIds.getValue("extra_6"), "score" to 81)
        )

        db.collection("candidates").document(GAL_ID)
            .update("jobMatches", matches)
            .addOnFailureListener {
                android.util.Log.e("SeedData", "Failed to update Gal job matches: ${it.message}")
            }
    }

    private fun jobAgeDays(key: String): Long = when {
        key == "gal_history" -> 16
        key.contains("history") -> 18
        key == "dana_qr" -> 4
        key == "dana_open" -> 2
        key == "gal_active" -> 6
        else -> 1L + (kotlin.math.abs(key.hashCode().toLong()) % 10L)
    }

    private fun stableDocumentId(prefix: String, rawValue: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(rawValue.toByteArray(Charsets.UTF_8))
            .take(12)
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        return "${prefix}_$digest"
    }
}