package com.example.clickjob_finalproject

import com.example.clickjob_finalproject.data.model.Application
import com.example.clickjob_finalproject.data.model.JobPost
import com.example.clickjob_finalproject.data.model.Notification
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object SeedData {

    private val db = FirebaseFirestore.getInstance()

    private const val GAL_ID    = "Bqp5ddpJXjd8A9XIXmYQnyTg0O03"
    private const val NOA_ID    = "b4DqRCqjFRaRCvw9Dww7hjjqW6t2"
    private const val YOSI_ID   = "deRhC5qnvUaoGHvL07OiqkfW5zV2"
    private const val DANA_ID   = "NAY1uD5jMEhoZpHWT5pZAe0DBMA2"
    private const val RON_ID    = "Zzj4RQROP3hslycmWP7UDEsuPH83"
    private const val MICHAL_ID = "W2hNEiyQE6UUu1wIn5qVAWdEUjf1"
    private const val AVI_ID    = "e2O0D8DhwaV8BdS4cgNF5RnKsku1"

    fun seedAll() {
        seedRatings()
        seedJobs { jobIds ->
            seedApplicationsAndNotifications(jobIds)
        }
    }

    private fun seedRatings() {
        mapOf(
            GAL_ID    to Triple(4.7, 12, false),
            NOA_ID    to Triple(4.9, 8,  false),
            YOSI_ID   to Triple(4.5, 15, false),
            DANA_ID   to Triple(4.8, 20, true),
            RON_ID    to Triple(4.6, 18, true),
            MICHAL_ID to Triple(4.3, 10, true),
            AVI_ID    to Triple(4.9, 25, true)
        ).forEach { (userId, data) ->
            db.collection("candidates").document(userId)
                .update(mapOf(
                    "rating"       to data.first,
                    "ratingsCount" to data.second,
                    "hasPostedJob" to data.third
                ))
        }
    }

    private fun seedJobs(onComplete: (Map<String, String>) -> Unit) {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L

        val jobs = mapOf(

            // ===== דנה - מסעדת הים (6 משרות) =====
            "dana_1" to JobPost(
                employerId = DANA_ID,
                title = "מלצר/ית לאירוע חתונה",
                company = "מסעדת הים",
                category = "מסעדנות",
                salary = "60", salaryType = "hourly",
                date = now + 2 * day, startTime = "18:00", endTime = "23:00",
                workersNeeded = 2, workersRegistered = 2,
                description = "דרוש/ה מלצר/ית מנוסה לאירוע חתונה יוקרתי בתל אביב. שירות ברמה גבוהה.",
                requirements = listOf("ניסיון בשירות", "מראה מטופח", "אנגלית בסיסית"),
                phone = "0521234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = true
            ),
            "dana_2" to JobPost(
                employerId = DANA_ID,
                title = "טבח/ית סו שף",
                company = "מסעדת הים",
                category = "מסעדנות",
                salary = "80", salaryType = "hourly",
                date = now + 4 * day, startTime = "10:00", endTime = "18:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה טבח/ית מנוסה למסעדת דגים יוקרתית בתל אביב.",
                requirements = listOf("ניסיון במטבח מקצועי", "תעודת בריאות", "יכולת עבודה בלחץ"),
                phone = "0521234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800",
                isUrgent = false
            ),
            "dana_3" to JobPost(
                employerId = DANA_ID,
                title = "ברמן/ית לאירוע",
                company = "מסעדת הים",
                category = "מסעדנות",
                salary = "65", salaryType = "hourly",
                date = now + 7 * day, startTime = "20:00", endTime = "02:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה ברמן/ית מנוסה לאירוע פרטי. ניסיון בהכנת קוקטיילים.",
                requirements = listOf("ניסיון כברמן", "ידע בקוקטיילים", "מראה מטופח"),
                phone = "0521234567", address = "הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800",
                isUrgent = false
            ),
            "dana_history_1" to JobPost(
                employerId = DANA_ID,
                title = "מלצר/ית לאירוע פרטי",
                company = "מסעדת הים",
                category = "מסעדנות",
                salary = "55", salaryType = "hourly",
                date = now - 5 * day, startTime = "19:00", endTime = "23:00",
                workersNeeded = 2, workersRegistered = 2,
                description = "אירוע פרטי ליום הולדת.",
                requirements = listOf("ניסיון בשירות"),
                phone = "0521234567", address = "הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = false
            ),
            "dana_history_2" to JobPost(
                employerId = DANA_ID,
                title = "טבח/ית לאירוע חברה",
                company = "מסעדת הים",
                category = "מסעדנות",
                salary = "70", salaryType = "hourly",
                date = now - 10 * day, startTime = "12:00", endTime = "20:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "אירוע חברה גדול.",
                requirements = listOf("ניסיון במטבח", "תעודת בריאות"),
                phone = "0521234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800",
                isUrgent = false
            ),

            // ===== רון - אולם האירועים (7 משרות) =====
            "ron_1" to JobPost(
                employerId = RON_ID,
                title = "אבטח/ת לפסטיבל מוזיקה",
                company = "אולם האירועים רויאל",
                category = "אבטחה וביטחון",
                salary = "70", salaryType = "hourly",
                date = now + 1 * day, startTime = "20:00", endTime = "02:00",
                workersNeeded = 3, workersRegistered = 3,
                description = "דרוש/ה אבטח/ת לפסטיבל מוזיקה גדול. חובה רישיון אבטחה בתוקף.",
                requirements = listOf("רישיון אבטחה", "כושר גופני", "ניסיון"),
                phone = "0531234567", address = "רמת גן",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = true
            ),
            "ron_2" to JobPost(
                employerId = RON_ID,
                title = "צלמ/ת לאירוע בר מצווה",
                company = "אולם האירועים רויאל",
                category = "קריאייטיב, עיצוב ומדיה",
                salary = "120", salaryType = "hourly",
                date = now + 6 * day, startTime = "17:00", endTime = "23:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה צלמ/ת מקצועי/ת לאירוע בר מצווה. ניסיון בצילום אירועים חובה.",
                requirements = listOf("ניסיון בצילום אירועים", "ציוד מקצועי", "תיק עבודות"),
                phone = "0531234567", address = "פתח תקווה",
                imageUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800",
                isUrgent = false
            ),
            "ron_3" to JobPost(
                employerId = RON_ID,
                title = "מנחה/ת אירוע",
                company = "אולם האירועים רויאל",
                category = "הפקה ואירועים",
                salary = "200", salaryType = "hourly",
                date = now + 3 * day, startTime = "18:00", endTime = "23:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מנחה/ת אירוע מנוסה לחתונה גדולה. ניסיון בהנחיית אירועים חובה.",
                requirements = listOf("ניסיון בהנחיה", "כריזמה", "עברית ברמה גבוהה"),
                phone = "0531234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = true
            ),
            "ron_4" to JobPost(
                employerId = RON_ID,
                title = "DJ לאירוע",
                company = "אולם האירועים רויאל",
                category = "הפקה ואירועים",
                salary = "300", salaryType = "hourly",
                date = now + 8 * day, startTime = "20:00", endTime = "02:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה DJ מנוסה לחתונה. סגנון מוזיקה: פופ ישראלי, מזרחי.",
                requirements = listOf("ניסיון כ-DJ", "ציוד מקצועי", "תיק עבודות"),
                phone = "0531234567", address = "רמת גן",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = false
            ),
            "ron_history_1" to JobPost(
                employerId = RON_ID,
                title = "מלצר/ית לאירוע קורפורייט",
                company = "אולם האירועים רויאל",
                category = "מסעדנות",
                salary = "55", salaryType = "hourly",
                date = now - 3 * day, startTime = "18:00", endTime = "23:00",
                workersNeeded = 3, workersRegistered = 3,
                description = "אירוע קורפורייט לחברת הייטק.",
                requirements = listOf("ניסיון", "אנגלית"),
                phone = "0531234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = false
            ),
            "ron_history_2" to JobPost(
                employerId = RON_ID,
                title = "אבטח/ת להופעה",
                company = "אולם האירועים רויאל",
                category = "אבטחה וביטחון",
                salary = "65", salaryType = "hourly",
                date = now - 7 * day, startTime = "19:00", endTime = "01:00",
                workersNeeded = 2, workersRegistered = 2,
                description = "הופעה של אמן מפורסם.",
                requirements = listOf("רישיון אבטחה"),
                phone = "0531234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = false
            ),
            "ron_history_3" to JobPost(
                employerId = RON_ID,
                title = "צלמ/ת לאירוע סיום",
                company = "אולם האירועים רויאל",
                category = "קריאייטיב, עיצוב ומדיה",
                salary = "100", salaryType = "hourly",
                date = now - 12 * day, startTime = "17:00", endTime = "22:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "צילום אירוע סיום שנה.",
                requirements = listOf("ניסיון בצילום", "ציוד מקצועי"),
                phone = "0531234567", address = "חיפה",
                imageUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800",
                isUrgent = false
            ),

            // ===== מיכל - TechStart (6 משרות) =====
            "michal_1" to JobPost(
                employerId = MICHAL_ID,
                title = "מפתח/ת Frontend",
                company = "TechStart",
                category = "טכנולוגיה ותוכנה",
                salary = "150", salaryType = "hourly",
                date = now + 3 * day, startTime = "09:00", endTime = "17:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "דרוש/ה מפתח/ת Frontend לפרויקט קצר טווח. ניסיון ב-React חובה.",
                requirements = listOf("React", "JavaScript", "CSS", "ניסיון של שנתיים לפחות"),
                phone = "0541234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
                isUrgent = true
            ),
            "michal_2" to JobPost(
                employerId = MICHAL_ID,
                title = "מנהל/ת מדיה חברתית",
                company = "TechStart",
                category = "שירות לקוחות ותמיכה",
                salary = "60", salaryType = "hourly",
                date = now + 5 * day, startTime = "10:00", endTime = "14:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מנהל/ת מדיה חברתית לחברת סטארטאפ.",
                requirements = listOf("ניסיון במדיה חברתית", "יצירתיות", "כתיבה שיווקית"),
                phone = "0541234567", address = "הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1553775282-20af80779df7?w=800",
                isUrgent = false
            ),
            "michal_3" to JobPost(
                employerId = MICHAL_ID,
                title = "מעצב/ת UX/UI",
                company = "TechStart",
                category = "קריאייטיב, עיצוב ומדיה",
                salary = "130", salaryType = "hourly",
                date = now + 9 * day, startTime = "09:00", endTime = "17:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מעצב/ת UX/UI לפרויקט אפליקציה מובייל.",
                requirements = listOf("Figma", "ניסיון בעיצוב מובייל", "תיק עבודות"),
                phone = "0541234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1561070791-2526d30994b5?w=800",
                isUrgent = false
            ),
            "michal_history_1" to JobPost(
                employerId = MICHAL_ID,
                title = "QA Tester",
                company = "TechStart",
                category = "טכנולוגיה ותוכנה",
                salary = "100", salaryType = "hourly",
                date = now - 4 * day, startTime = "09:00", endTime = "17:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "בדיקות איכות לאפליקציה מובייל.",
                requirements = listOf("ניסיון ב-QA", "אנגלית"),
                phone = "0541234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
                isUrgent = false
            ),
            "michal_history_2" to JobPost(
                employerId = MICHAL_ID,
                title = "מפתח/ת Backend",
                company = "TechStart",
                category = "טכנולוגיה ותוכנה",
                salary = "160", salaryType = "hourly",
                date = now - 8 * day, startTime = "09:00", endTime = "17:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "פיתוח Backend לפרויקט.",
                requirements = listOf("Node.js", "Python", "ניסיון של 3 שנים"),
                phone = "0541234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
                isUrgent = false
            ),

            // ===== אבי - מגוון (8 משרות) =====
            "avi_1" to JobPost(
                employerId = AVI_ID,
                title = "מורה לאנגלית",
                company = "EduCenter",
                category = "חינוך והוראה",
                salary = "90", salaryType = "hourly",
                date = now + 2 * day, startTime = "16:00", endTime = "20:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מורה לאנגלית לתלמידי תיכון. ניסיון בהוראה חובה.",
                requirements = listOf("תואר רלוונטי", "ניסיון בהוראה", "אנגלית ברמת שפת אם"),
                phone = "0551234567", address = "הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800",
                isUrgent = false
            ),
            "avi_2" to JobPost(
                employerId = AVI_ID,
                title = "שליח/ה למשלוחים",
                company = "SpeedEx",
                category = "משלוחים ותחבורה",
                salary = "50", salaryType = "hourly",
                date = now + 1 * day, startTime = "09:00", endTime = "17:00",
                workersNeeded = 2, workersRegistered = 1,
                description = "דרוש/ה שליח/ה עם רכב פרטי למשלוחים באזור המרכז.",
                requirements = listOf("רכב פרטי", "רישיון נהיגה"),
                phone = "0551234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800",
                isUrgent = true
            ),
            "avi_3" to JobPost(
                employerId = AVI_ID,
                title = "מטפל/ת בכלבים",
                company = "PetCare",
                category = "בעלי חיים",
                salary = "55", salaryType = "hourly",
                date = now + 3 * day, startTime = "08:00", endTime = "14:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה מטפל/ת בכלבים לעסק מטיילי כלבים. אהבה לבעלי חיים חובה.",
                requirements = listOf("אהבה לבעלי חיים", "אחריות", "כושר גופני"),
                phone = "0551234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800",
                isUrgent = false
            ),
            "avi_4" to JobPost(
                employerId = AVI_ID,
                title = "עוזר/ת וטרינר",
                company = "PetCare",
                category = "בעלי חיים",
                salary = "65", salaryType = "hourly",
                date = now + 5 * day, startTime = "09:00", endTime = "15:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה עוזר/ת לקליניקה וטרינרית. ניסיון עם בעלי חיים יתרון.",
                requirements = listOf("אהבה לבעלי חיים", "יחסי אנוש", "אחריות"),
                phone = "0551234567", address = "רמת גן",
                imageUrl = "https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?w=800",
                isUrgent = true
            ),
            "avi_5" to JobPost(
                employerId = AVI_ID,
                title = "סייע/ת לגן ילדים",
                company = "גן הפרחים",
                category = "חינוך והוראה",
                salary = "45", salaryType = "hourly",
                date = now + 4 * day, startTime = "07:30", endTime = "13:30",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה סייע/ת לגן ילדים. אוהב/ת ילדים, סבלני/ת ואחראי/ת.",
                requirements = listOf("אהבה לילדים", "סבלנות", "תעודת סייע/ת יתרון"),
                phone = "0551234567", address = "כפר סבא",
                imageUrl = "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=800",
                isUrgent = false
            ),
            "avi_history_1" to JobPost(
                employerId = AVI_ID,
                title = "נהג/ת להסעות",
                company = "SpeedEx",
                category = "משלוחים ותחבורה",
                salary = "55", salaryType = "hourly",
                date = now - 6 * day, startTime = "07:00", endTime = "15:00",
                workersNeeded = 2, workersRegistered = 2,
                description = "הסעת עובדים.",
                requirements = listOf("רישיון ב", "ניסיון נהיגה"),
                phone = "0551234567", address = "באר שבע",
                imageUrl = "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=800",
                isUrgent = false
            ),
            "avi_history_2" to JobPost(
                employerId = AVI_ID,
                title = "מורה לגיטרה",
                company = "EduCenter",
                category = "חינוך והוראה",
                salary = "80", salaryType = "hourly",
                date = now - 9 * day, startTime = "15:00", endTime = "19:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "שיעורי גיטרה לילדים ומבוגרים.",
                requirements = listOf("ניסיון בהוראת מוזיקה", "סבלנות"),
                phone = "0551234567", address = "הרצליה",
                imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800",
                isUrgent = false
            ),
            "avi_history_3" to JobPost(
                employerId = AVI_ID,
                title = "מטפל/ת בחתולים",
                company = "PetCare",
                category = "בעלי חיים",
                salary = "50", salaryType = "hourly",
                date = now - 14 * day, startTime = "09:00", endTime = "13:00",
                workersNeeded = 1, workersRegistered = 1,
                description = "טיפול בחתולים בבית הלקוח.",
                requirements = listOf("אהבה לחתולים", "אחריות"),
                phone = "0551234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?w=800",
                isUrgent = false
            ),

            // ===== משרות נוספות מגוונות (8 משרות) =====
            "extra_1" to JobPost(
                employerId = DANA_ID,
                title = "עוזר/ת אחות",
                company = "מרפאת השרון",
                category = "בריאות ורווחה",
                salary = "70", salaryType = "hourly",
                date = now + 2 * day, startTime = "08:00", endTime = "14:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה עוזר/ת אחות למרפאה פרטית באזור השרון.",
                requirements = listOf("הסמכה רלוונטית", "ניסיון", "יחסי אנוש טובים"),
                phone = "0521234567", address = "נתניה",
                imageUrl = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800",
                isUrgent = false
            ),
            "extra_2" to JobPost(
                employerId = RON_ID,
                title = "מאבטח/ת קניון",
                company = "SafeMall",
                category = "אבטחה וביטחון",
                salary = "55", salaryType = "hourly",
                date = now + 3 * day, startTime = "14:00", endTime = "22:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה מאבטח/ת לקניון. משמרות ערב. רישיון אבטחה חובה.",
                requirements = listOf("רישיון אבטחה", "ניסיון", "אמינות"),
                phone = "0531234567", address = "אשדוד",
                imageUrl = "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?w=800",
                isUrgent = false
            ),
            "extra_3" to JobPost(
                employerId = MICHAL_ID,
                title = "נציג/ת שירות לקוחות",
                company = "TechStart",
                category = "שירות לקוחות ותמיכה",
                salary = "42", salaryType = "hourly",
                date = now + 4 * day, startTime = "08:00", endTime = "16:00",
                workersNeeded = 3, workersRegistered = 0,
                description = "דרוש/ה נציג/ת שירות לקוחות למוקד טלפוני.",
                requirements = listOf("עברית ברמה גבוהה", "יחסי אנוש", "ניסיון יתרון"),
                phone = "0541234567", address = "רחובות",
                imageUrl = "https://images.unsplash.com/photo-1553775282-20af80779df7?w=800",
                isUrgent = false
            ),
            "extra_4" to JobPost(
                employerId = AVI_ID,
                title = "פועל/ת בניין",
                company = "BuildRight",
                category = "בניין, תעשייה וייצור",
                salary = "60", salaryType = "hourly",
                date = now + 2 * day, startTime = "07:00", endTime = "16:00",
                workersNeeded = 3, workersRegistered = 0,
                description = "דרוש/ה פועל/ת בניין לפרויקט בנייה חדש.",
                requirements = listOf("כושר גופני", "ניסיון בבניין", "אמינות"),
                phone = "0551234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                isUrgent = true
            ),
            "extra_5" to JobPost(
                employerId = RON_ID,
                title = "מוכר/ת בחנות בגדים",
                company = "FashionStore",
                category = "מכירות ואופנה",
                salary = "40", salaryType = "hourly",
                date = now + 1 * day, startTime = "10:00", endTime = "18:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה מוכר/ת לחנות בגדים במרכז קניות.",
                requirements = listOf("שירותיות", "ניסיון במכירות", "עברית ברמה גבוהה"),
                phone = "0531234567", address = "ראשון לציון",
                imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800",
                isUrgent = true
            ),
            "extra_6" to JobPost(
                employerId = DANA_ID,
                title = "מטפל/ת בקשישים",
                company = "CareHome",
                category = "בריאות ורווחה",
                salary = "65", salaryType = "hourly",
                date = now + 5 * day, startTime = "08:00", endTime = "16:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מטפל/ת בקשישים לבית אבות. ניסיון יתרון.",
                requirements = listOf("סבלנות", "אמפתיה", "ניסיון בטיפול"),
                phone = "0521234567", address = "נתניה",
                imageUrl = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800",
                isUrgent = false
            ),
            "extra_7" to JobPost(
                employerId = MICHAL_ID,
                title = "מנהל/ת לוגיסטיקה",
                company = "LogiPro",
                category = "אפסנאות ולוגיסטיקה",
                salary = "80", salaryType = "hourly",
                date = now + 6 * day, startTime = "08:00", endTime = "16:00",
                workersNeeded = 1, workersRegistered = 0,
                description = "דרוש/ה מנהל/ת לוגיסטיקה לחברת יבוא.",
                requirements = listOf("ניסיון בלוגיסטיקה", "Excel", "אנגלית"),
                phone = "0541234567", address = "אשדוד",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800",
                isUrgent = false
            ),
            "extra_8" to JobPost(
                employerId = AVI_ID,
                title = "עובד/ת אחזקה",
                company = "MaintenancePro",
                category = "אחזקה",
                salary = "55", salaryType = "hourly",
                date = now + 3 * day, startTime = "08:00", endTime = "16:00",
                workersNeeded = 2, workersRegistered = 0,
                description = "דרוש/ה עובד/ת אחזקה לבניין משרדים.",
                requirements = listOf("ניסיון באחזקה", "כלים ידניים", "אמינות"),
                phone = "0551234567", address = "תל אביב",
                imageUrl = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                isUrgent = false
            )
        )

        val jobIds = mutableMapOf<String, String>()
        var savedCount = 0
        val totalJobs = jobs.size

        jobs.forEach { (key, job) ->
            val ref = db.collection("jobs").document()
            val jobWithId = job.copy(id = ref.id)
            ref.set(jobWithId)
                .addOnSuccessListener {
                    synchronized(jobIds) {
                        jobIds[key] = ref.id
                        savedCount++
                        if (savedCount == totalJobs) {
                            onComplete(jobIds)
                        }
                    }
                }
        }
    }

    private fun seedApplicationsAndNotifications(jobIds: Map<String, String>) {
        loadWorkerProfiles { profiles ->

            // ===== גל - המשתמש הראשי =====
            // pending - מחכה לאישור מעסיק
            createApplication(jobIds["avi_2"]!!, GAL_ID, AVI_ID, profiles[GAL_ID], "pending")
            createApplication(jobIds["avi_3"]!!, GAL_ID, AVI_ID, profiles[GAL_ID], "pending")

            // employer_approved - ממתין לאישור גל
            createApplication(jobIds["avi_1"]!!, GAL_ID, AVI_ID, profiles[GAL_ID], "employer_approved") { appId ->
                sendNotification(GAL_ID, "worker", "ALERT",
                    "נדרש אישור לעבודה ב\"EduCenter\"",
                    "עבודה בעוד יומיים ב-16:00",
                    jobIds["avi_1"]!!, appId)
            }
            createApplication(jobIds["dana_2"]!!, GAL_ID, DANA_ID, profiles[GAL_ID], "employer_approved") { appId ->
                sendNotification(GAL_ID, "worker", "ALERT",
                    "נדרש אישור לעבודה ב\"מסעדת הים\"",
                    "עבודה בעוד 4 ימים ב-10:00",
                    jobIds["dana_2"]!!, appId)
            }

            // התראות נוספות לגל
            sendNotification(GAL_ID, "worker", "RATING",
                "דרג את עבודה ב\"SpeedEx\"",
                "דירוגים מעלים את הסיכוי למצוא עבודה",
                jobIds["avi_history_1"]!!)
            sendNotification(GAL_ID, "worker", "PEOPLE",
                "תזכורת: השלם את הפרופיל שלך",
                "פרופיל מלא מגדיל את הסיכוי להתקבל",
                jobIds["avi_1"]!!)
            sendNotification(GAL_ID, "worker", "CANCELLED",
                "המשרה ב\"BuildRight\" בוטלה",
                "המעסיק ביטל את המשרה",
                jobIds["extra_4"]!!)

            // ===== יוסי ונועה - עובדים במשרות מאויישות =====
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

            // ===== התראות לכל המשתמשים =====

            // נועה
            sendNotification(NOA_ID, "worker", "CONFIRMED",
                "התקבלת לעבודה ב\"TechStart\"",
                "עבודה בעוד 3 ימים ב-09:00", jobIds["michal_1"]!!)
            sendNotification(NOA_ID, "worker", "CONFIRMED",
                "התקבלת לעבודה ב\"מסעדת הים\"",
                "עבודה בעוד יומיים ב-18:00", jobIds["dana_1"]!!)
            sendNotification(NOA_ID, "worker", "RATING",
                "דרג את עבודה ב\"אולם האירועים רויאל\"",
                "דירוגים מעלים את הסיכוי למצוא עבודה", jobIds["ron_history_1"]!!)

            // יוסי
            sendNotification(YOSI_ID, "worker", "CONFIRMED",
                "התקבלת לעבודה ב\"מסעדת הים\"",
                "עבודה בעוד יומיים ב-18:00", jobIds["dana_1"]!!)
            sendNotification(YOSI_ID, "worker", "CONFIRMED",
                "התקבלת לעבודה ב\"אולם האירועים רויאל\"",
                "עבודה מחר ב-20:00", jobIds["ron_1"]!!)
            sendNotification(YOSI_ID, "worker", "RATING",
                "דרג את עבודה ב\"SpeedEx\"",
                "דירוגים מעלים את הסיכוי למצוא עבודה", jobIds["avi_history_1"]!!)

            // דנה (מעסיקה)
            sendNotification(DANA_ID, "employer", "PEOPLE",
                "2 מועמדים חדשים למשרת מלצר/ית לחתונה",
                "לחץ לצפייה במועמדים", jobIds["dana_1"]!!)
            sendNotification(DANA_ID, "employer", "WORKER_CONFIRMED",
                "יוסי אישר/ה את העבודה ב\"מסעדת הים\"",
                "עבודה בעוד יומיים ב-18:00", jobIds["dana_1"]!!)
            sendNotification(DANA_ID, "employer", "RATING",
                "דרג את העובדים במשרת מלצר/ית לאירוע פרטי",
                "דירוגים עוזרים לעובדים למצוא עבודה", jobIds["dana_history_1"]!!)

            // רון (מעסיק)
            sendNotification(RON_ID, "employer", "PEOPLE",
                "3 מועמדים חדשים למשרת אבטח/ת לפסטיבל",
                "לחץ לצפייה במועמדים", jobIds["ron_1"]!!)
            sendNotification(RON_ID, "employer", "WORKER_CONFIRMED",
                "יוסי אישר/ה את העבודה ב\"אולם האירועים רויאל\"",
                "עבודה מחר ב-20:00", jobIds["ron_1"]!!)
            sendNotification(RON_ID, "employer", "RATING",
                "דרג את העובדים במשרת מלצר/ית לאירוע קורפורייט",
                "דירוגים עוזרים לעובדים למצוא עבודה", jobIds["ron_history_1"]!!)

            // מיכל (מעסיקה)
            sendNotification(MICHAL_ID, "employer", "WORKER_CONFIRMED",
                "נועה אישר/ה את העבודה ב\"TechStart\"",
                "עבודה בעוד 3 ימים ב-09:00", jobIds["michal_1"]!!)
            sendNotification(MICHAL_ID, "employer", "PEOPLE",
                "מועמד חדש למשרת מנהל/ת מדיה חברתית",
                "לחץ לצפייה במועמדים", jobIds["michal_2"]!!)
            sendNotification(MICHAL_ID, "employer", "RATING",
                "דרג את העובדים במשרת QA Tester",
                "דירוגים עוזרים לעובדים למצוא עבודה", jobIds["michal_history_1"]!!)

            // אבי (מעסיק)
            sendNotification(AVI_ID, "employer", "PEOPLE",
                "מועמד חדש למשרת שליח/ה למשלוחים",
                "לחץ לצפייה במועמדים", jobIds["avi_2"]!!)
            sendNotification(AVI_ID, "employer", "WORKER_CONFIRMED",
                "יוסי אישר/ה את העבודה ב\"SpeedEx\"",
                "עבודה מחר ב-09:00", jobIds["avi_2"]!!)
            sendNotification(AVI_ID, "employer", "RATING",
                "דרג את העובדים במשרת נהג/ת להסעות",
                "דירוגים עוזרים לעובדים למצוא עבודה", jobIds["avi_history_1"]!!)
        }
    }

    private fun loadWorkerProfiles(onComplete: (Map<String, Map<String, String>>) -> Unit) {
        val workerIds = listOf(GAL_ID, NOA_ID, YOSI_ID, AVI_ID)
        val profiles = mutableMapOf<String, Map<String, String>>()
        var loaded = 0

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
                    loaded++
                    if (loaded == workerIds.size) onComplete(profiles)
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
        val ref = db.collection("applications").document()
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
        ref.set(application).addOnSuccessListener {
            onCreated?.invoke(ref.id)
        }
    }

    private fun updateUpcomingShift(userId: String, jobId: String) {
        db.collection("candidates").document(userId)
            .update("upcomingShifts", FieldValue.arrayUnion(jobId))
    }

    private fun sendNotification(
        userId: String,
        role: String,
        type: String,
        title: String,
        dateTime: String,
        jobId: String,
        applicationId: String = ""
    ) {
        val ref = db.collection("notifications").document()
        val notification = Notification(
            id = ref.id,
            userId = userId,
            role = role,
            type = type,
            title = title,
            dateTime = dateTime,
            jobId = jobId,
            applicationId = applicationId,
            createdAt = System.currentTimeMillis() - (Math.random() * 24 * 60 * 60 * 1000).toLong()
        )
        ref.set(notification)
    }
}