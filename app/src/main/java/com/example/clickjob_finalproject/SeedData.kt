package com.example.clickjob_finalproject

import com.example.clickjob_finalproject.data.model.Application
import com.example.clickjob_finalproject.data.model.JobPost
import com.google.firebase.firestore.FirebaseFirestore

object SeedData {

    private val db = FirebaseFirestore.getInstance()

    private const val EMPLOYER_ID = "R3uNjjOtoBYivEEiCTXMPpcyN672"
    private const val WORKER1_ID = "j9JcNVuNkkOwWKcxp6BNMrrrqc32"
    private const val WORKER2_ID = "XqTEzqwkmtWJGBXT1MWWJYr1PmF2"

    fun seedAll() {
        seedJobs { jobIds ->
            seedApplicationsAndNotifications(jobIds)
        }
    }

    private fun seedJobs(onComplete: (List<String>) -> Unit) {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L

        val jobs = listOf(
            JobPost(
                title = "מלצר/ית לחתונה",
                company = "אולם האירועים רויאל",
                category = "מסעדות",
                salary = "55",
                salaryType = "hourly",
                date = now + 1 * day,
                startTime = "18:00",
                endTime = "23:00",
                workersNeeded = 5,
                workersRegistered = 2,
                description = "דרוש/ה מלצר/ית לחתונה גדולה באולם יוקרתי. ניסיון יתרון משמעותי.",
                requirements = listOf("ניסיון", "אנגלית", "מראה מטופח"),
                phone = "0501234567",
                address = "תל אביב",
                link = "https://www.royal-events.co.il",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = true
            ),
            JobPost(
                title = "אבטח/ת לפסטיבל",
                company = "SecureEvent",
                category = "אבטחה וביטחון",
                salary = "65",
                salaryType = "hourly",
                date = now + 1 * day,
                startTime = "20:00",
                endTime = "02:00",
                workersNeeded = 3,
                workersRegistered = 0,
                description = "דרוש/ה אבטח/ת לפסטיבל מוזיקה גדול. חובה רישיון אבטחה בתוקף.",
                requirements = listOf("רישיון אבטחה", "כושר גופני", "ניסיון"),
                phone = "0521234567",
                address = "רמת גן",
                link = "https://www.secureevent.co.il",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = true
            ),
            JobPost(
                title = "שליח/ה למשלוחים",
                company = "SpeedEx",
                category = "משלוחים ותחבורה",
                salary = "300",
                salaryType = "daily",
                date = now + 2 * day,
                startTime = "09:00",
                endTime = "17:00",
                workersNeeded = 2,
                workersRegistered = 0,
                description = "דרוש/ה שליח/ה עם רכב פרטי למשלוחים באזור המרכז.",
                requirements = listOf("רכב פרטי", "רישיון נהיגה"),
                phone = "0531234567",
                address = "ירושלים",
                link = "https://www.speedex.co.il",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800",
                isUrgent = true
            ),
            JobPost(
                title = "מורה לאנגלית",
                company = "EduCenter",
                category = "חינוך והוראה",
                salary = "80",
                salaryType = "hourly",
                date = now + 3 * day,
                startTime = "16:00",
                endTime = "20:00",
                workersNeeded = 1,
                workersRegistered = 0,
                description = "דרוש/ה מורה לאנגלית לתלמידי תיכון. ניסיון בהוראה חובה.",
                requirements = listOf("תואר רלוונטי", "ניסיון בהוראה", "אנגלית ברמת שפת אם"),
                phone = "0541234567",
                address = "הרצליה",
                link = "https://www.educenter.co.il",
                imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "עוזר/ת אחות",
                company = "מרפאת השרון",
                category = "בריאות ורווחה",
                salary = "70",
                salaryType = "hourly",
                date = now + 2 * day,
                startTime = "08:00",
                endTime = "14:00",
                workersNeeded = 2,
                workersRegistered = 1,
                description = "דרוש/ה עוזר/ת אחות למרפאה פרטית באזור השרון.",
                requirements = listOf("הסמכה רלוונטית", "ניסיון", "יחסי אנוש טובים"),
                phone = "0551234567",
                address = "נתניה",
                link = "https://www.sharon-clinic.co.il",
                imageUrl = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "טכנאי/ת מחשבים",
                company = "TechFix",
                category = "טכנולוגיה",
                salary = "90",
                salaryType = "hourly",
                date = now + 4 * day,
                startTime = "09:00",
                endTime = "17:00",
                workersNeeded = 1,
                workersRegistered = 0,
                description = "דרוש/ה טכנאי/ת מחשבים לתיקון ותחזוקת מערכות.",
                requirements = listOf("ניסיון בתיקון מחשבים", "ידע ברשתות", "A+ certification יתרון"),
                phone = "0561234567",
                address = "פתח תקווה",
                link = "https://www.techfix.co.il",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "מוכר/ת בחנות בגדים",
                company = "FashionStore",
                category = "מכירות ואופנה",
                salary = "40",
                salaryType = "hourly",
                date = now + 1 * day,
                startTime = "10:00",
                endTime = "18:00",
                workersNeeded = 3,
                workersRegistered = 1,
                description = "דרוש/ה מוכר/ת לחנות בגדים במרכז קניות. ניסיון במכירות יתרון.",
                requirements = listOf("שירותיות", "ניסיון במכירות", "עברית ברמה גבוהה"),
                phone = "0571234567",
                address = "ראשון לציון",
                link = "https://www.fashionstore.co.il",
                imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800",
                isUrgent = true
            ),
            JobPost(
                title = "צלמ/ת לאירוע",
                company = "PhotoPro",
                category = "קריאייטיב, עיצוב ומדיה",
                salary = "120",
                salaryType = "hourly",
                date = now + 5 * day,
                startTime = "17:00",
                endTime = "23:00",
                workersNeeded = 1,
                workersRegistered = 0,
                description = "דרוש/ה צלמ/ת מקצועי/ת לאירוע בר מצווה. נדרש ניסיון בצילום אירועים.",
                requirements = listOf("ניסיון בצילום אירועים", "ציוד מקצועי", "תיק עבודות"),
                phone = "0581234567",
                address = "חיפה",
                link = "https://www.photopro.co.il",
                imageUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "נהג/ת להסעות",
                company = "TransportPlus",
                category = "משלוחים ותחבורה",
                salary = "50",
                salaryType = "hourly",
                date = now + 2 * day,
                startTime = "07:00",
                endTime = "15:00",
                workersNeeded = 2,
                workersRegistered = 0,
                description = "דרוש/ה נהג/ת להסעת עובדים. ניסיון בנהיגה מקצועית חובה.",
                requirements = listOf("רישיון ב", "ניסיון נהיגה", "תעודת נהג מוסמך"),
                phone = "0591234567",
                address = "באר שבע",
                link = "https://www.transportplus.co.il",
                imageUrl = "https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "מאבטח/ת קניון",
                company = "SafeMall",
                category = "אבטחה וביטחון",
                salary = "55",
                salaryType = "hourly",
                date = now + 3 * day,
                startTime = "14:00",
                endTime = "22:00",
                workersNeeded = 4,
                workersRegistered = 2,
                description = "דרוש/ה מאבטח/ת לקניון. משמרות ערב. רישיון אבטחה חובה.",
                requirements = listOf("רישיון אבטחה", "ניסיון", "אמינות"),
                phone = "0501112233",
                address = "אשדוד",
                link = "https://www.safemall.co.il",
                imageUrl = "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "סייע/ת לגן ילדים",
                company = "גן הפרחים",
                category = "חינוך והוראה",
                salary = "45",
                salaryType = "hourly",
                date = now + 3 * day,
                startTime = "07:30",
                endTime = "13:30",
                workersNeeded = 1,
                workersRegistered = 0,
                description = "דרוש/ה סייע/ת לגן ילדים. אוהב/ת ילדים, סבלני/ת ואחראי/ת.",
                requirements = listOf("אהבה לילדים", "סבלנות", "תעודת סייע/ת יתרון"),
                phone = "0502223344",
                address = "כפר סבא",
                link = "https://www.ganhaparahim.co.il",
                imageUrl = "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "פועל/ת בניין",
                company = "BuildRight",
                category = "בניין, תעשייה וייצור",
                salary = "60",
                salaryType = "hourly",
                date = now + 2 * day,
                startTime = "07:00",
                endTime = "16:00",
                workersNeeded = 5,
                workersRegistered = 3,
                description = "דרוש/ה פועל/ת בניין לפרויקט בנייה חדש. ניסיון יתרון.",
                requirements = listOf("כושר גופני", "ניסיון בבניין", "אמינות"),
                phone = "0503334455",
                address = "תל אביב",
                link = "https://www.buildright.co.il",
                imageUrl = "https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "מעצב/ת גרפי/ת",
                company = "DesignHub",
                category = "קריאייטיב, עיצוב ומדיה",
                salary = "100",
                salaryType = "hourly",
                date = now + 6 * day,
                startTime = "09:00",
                endTime = "17:00",
                workersNeeded = 1,
                workersRegistered = 0,
                description = "דרוש/ה מעצב/ת גרפי/ת לפרויקט פרסום. ניסיון עם Adobe Creative Suite חובה.",
                requirements = listOf("ניסיון בעיצוב", "Adobe Suite", "תיק עבודות"),
                phone = "0504445566",
                address = "תל אביב",
                link = "https://www.designhub.co.il",
                imageUrl = "https://images.unsplash.com/photo-1561070791-2526d30994b5?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "נציג/ת שירות לקוחות",
                company = "ServicePro",
                category = "שירות לקוחות ותמיכה",
                salary = "42",
                salaryType = "hourly",
                date = now + 4 * day,
                startTime = "08:00",
                endTime = "16:00",
                workersNeeded = 3,
                workersRegistered = 0,
                description = "דרוש/ה נציג/ת שירות לקוחות למוקד טלפוני. עברית ברמה גבוהה חובה.",
                requirements = listOf("עברית ברמה גבוהה", "יחסי אנוש", "ניסיון יתרון"),
                phone = "0505556677",
                address = "רחובות",
                link = "https://www.servicepro.co.il",
                imageUrl = "https://images.unsplash.com/photo-1553775282-20af80779df7?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "טבח/ית למסעדה",
                company = "מסעדת הים",
                category = "מסעדנות",
                salary = "65",
                salaryType = "hourly",
                date = now + 2 * day,
                startTime = "12:00",
                endTime = "22:00",
                workersNeeded = 2,
                workersRegistered = 0,
                description = "דרוש/ה טבח/ית למסעדת דגים יוקרתית. ניסיון במטבח מקצועי חובה.",
                requirements = listOf("ניסיון במטבח", "תעודת בריאות", "יכולת עבודה בלחץ"),
                phone = "0506667788",
                address = "תל אביב",
                link = "https://www.hayam-restaurant.co.il",
                imageUrl = "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800",
                isUrgent = false
            ),

            // ===== משרות שעברו - להיסטוריה =====
            JobPost(
                title = "מלצר/ית לאירוע קורפורייט",
                company = "אולם האירועים רויאל",
                category = "מסעדנות",
                salary = "55",
                salaryType = "hourly",
                date = now - 3 * day,
                startTime = "18:00",
                endTime = "23:00",
                workersNeeded = 4,
                workersRegistered = 4,
                description = "אירוע קורפורייט לחברת הייטק גדולה.",
                requirements = listOf("ניסיון", "אנגלית"),
                phone = "0501234567",
                address = "תל אביב",
                link = "https://www.royal-events.co.il",
                imageUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "אבטח/ת להופעה",
                company = "SecureEvent",
                category = "אבטחה וביטחון",
                salary = "65",
                salaryType = "hourly",
                date = now - 5 * day,
                startTime = "19:00",
                endTime = "01:00",
                workersNeeded = 5,
                workersRegistered = 5,
                description = "הופעה של אמן מפורסם.",
                requirements = listOf("רישיון אבטחה"),
                phone = "0521234567",
                address = "תל אביב",
                link = "https://www.secureevent.co.il",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "שליח/ה לאירוע מיוחד",
                company = "SpeedEx",
                category = "משלוחים ותחבורה",
                salary = "45",
                salaryType = "hourly",
                date = now - 7 * day,
                startTime = "08:00",
                endTime = "16:00",
                workersNeeded = 2,
                workersRegistered = 2,
                description = "משלוחים מיוחדים לאירוע.",
                requirements = listOf("רכב פרטי", "רישיון נהיגה"),
                phone = "0531234567",
                address = "ירושלים",
                link = "https://www.speedex.co.il",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "מוכר/ת לאירוע מכירות",
                company = "FashionStore",
                category = "מכירות ואופנה",
                salary = "40",
                salaryType = "hourly",
                date = now - 2 * day,
                startTime = "10:00",
                endTime = "18:00",
                workersNeeded = 3,
                workersRegistered = 3,
                description = "אירוע מכירות מיוחד של סוף עונה.",
                requirements = listOf("ניסיון במכירות"),
                phone = "0571234567",
                address = "ראשון לציון",
                link = "https://www.fashionstore.co.il",
                imageUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800",
                isUrgent = false
            ),
            JobPost(
                title = "טבח/ית לאירוע פרטי",
                company = "מסעדת הים",
                category = "מסעדנות",
                salary = "70",
                salaryType = "hourly",
                date = now - 4 * day,
                startTime = "14:00",
                endTime = "22:00",
                workersNeeded = 2,
                workersRegistered = 2,
                description = "אירוע פרטי ליום הולדת.",
                requirements = listOf("ניסיון במטבח", "תעודת בריאות"),
                phone = "0506667788",
                address = "הרצליה",
                link = "https://www.hayam-restaurant.co.il",
                imageUrl = "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=800",
                isUrgent = false
            )
        )

        val jobIds = mutableListOf<String>()
        var savedCount = 0

        jobs.forEach { job ->
            val ref = db.collection("jobs").document()
            val jobWithId = job.copy(id = ref.id, employerId = EMPLOYER_ID)
            ref.set(jobWithId)
                .addOnSuccessListener {
                    synchronized(jobIds) {
                        jobIds.add(ref.id)
                        savedCount++
                        if (savedCount == jobs.size) {
                            onComplete(jobIds)
                        }
                    }
                }
        }
    }

    private fun seedApplicationsAndNotifications(jobIds: List<String>) {
        if (jobIds.size < 3) return

        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L

        db.collection("users").document(WORKER1_ID)
            .get()
            .addOnSuccessListener { userDoc ->
                val workerName = userDoc.getString("name") ?: "עובד"
                val workerPhone = userDoc.getString("phone") ?: ""
                val workerBio = userDoc.getString("bio") ?: ""

                // Application 1 - pending
                val appRef1 = db.collection("applications").document()
                appRef1.set(Application(
                    id = appRef1.id,
                    jobId = jobIds[0],
                    workerId = WORKER1_ID,
                    employerId = EMPLOYER_ID,
                    workerName = workerName,
                    workerPhone = workerPhone,
                    workerBio = workerBio,
                    status = "pending"
                ))

                // Application 2 - employer_approved
                val appRef2 = db.collection("applications").document()
                appRef2.set(Application(
                    id = appRef2.id,
                    jobId = jobIds[1],
                    workerId = WORKER1_ID,
                    employerId = EMPLOYER_ID,
                    workerName = workerName,
                    workerPhone = workerPhone,
                    workerBio = workerBio,
                    status = "employer_approved"
                ))

                // Application 3 - confirmed + upcoming shift
                val appRef3 = db.collection("applications").document()
                appRef3.set(Application(
                    id = appRef3.id,
                    jobId = jobIds[2],
                    workerId = WORKER1_ID,
                    employerId = EMPLOYER_ID,
                    workerName = workerName,
                    workerPhone = workerPhone,
                    workerBio = workerBio,
                    status = "confirmed"
                ))

                // Save upcoming shift to worker profile
                db.collection("users").document(WORKER1_ID)
                    .update("upcomingShifts",
                        com.google.firebase.firestore.FieldValue.arrayUnion(jobIds[2]))

                // Save job matches to worker profile
                db.collection("users").document(WORKER1_ID)
                    .update(
                        "jobMatches", listOf(
                            mapOf("jobId" to jobIds[3], "matchPercent" to 92),
                            mapOf("jobId" to jobIds[4], "matchPercent" to 87),
                            mapOf("jobId" to jobIds[5], "matchPercent" to 79),
                            mapOf("jobId" to jobIds[6], "matchPercent" to 74)
                        )
                    )

                // ===== התראות לעובד 1 =====
                listOf(
                    mapOf(
                        "userId" to WORKER1_ID, "role" to "worker",
                        "type" to "ALERT",
                        "title" to "נדרש אישור לעבודה ב\"SecureEvent\"",
                        "dateTime" to "עבודה מחר ב-20:00",
                        "jobId" to jobIds[1],
                        "applicationId" to appRef2.id,
                        "isRead" to false, "isRated" to false,
                        "createdAt" to (now - 10 * 60 * 1000L)
                    ),
                    mapOf(
                        "userId" to WORKER1_ID, "role" to "worker",
                        "type" to "CONFIRMED",
                        "title" to "התקבלת לעבודה ב\"SpeedEx\"",
                        "dateTime" to "עבודה מחרתיים ב-09:00",
                        "jobId" to jobIds[2],
                        "applicationId" to appRef3.id,
                        "isRead" to false, "isRated" to false,
                        "createdAt" to (now - 1 * 60 * 60 * 1000L)
                    ),
                    mapOf(
                        "userId" to WORKER1_ID, "role" to "worker",
                        "type" to "PENDING",
                        "title" to "סריקה לסיום עבודה ב\"אולם האירועים רויאל\"",
                        "dateTime" to "עבודה הסתיימה ב-23:00",
                        "jobId" to jobIds[0],
                        "applicationId" to "",
                        "isRead" to true, "isRated" to false,
                        "createdAt" to (now - 2 * 60 * 60 * 1000L)
                    ),
                    mapOf(
                        "userId" to WORKER1_ID, "role" to "worker",
                        "type" to "RATING",
                        "title" to "דרג את עבודה ב\"אולם האירועים רויאל\"",
                        "dateTime" to "דירוגים מעלים את הסיכוי למצוא עבודה",
                        "jobId" to jobIds[0],
                        "applicationId" to "",
                        "isRead" to true, "isRated" to false,
                        "createdAt" to (now - 3 * 60 * 60 * 1000L)
                    ),
                    mapOf(
                        "userId" to WORKER1_ID, "role" to "worker",
                        "type" to "PEOPLE",
                        "title" to "תזכורת לעבודה מחר ב\"SpeedEx\"",
                        "dateTime" to "עבודה מחרתיים ב-09:00",
                        "jobId" to jobIds[2],
                        "applicationId" to "",
                        "isRead" to true, "isRated" to false,
                        "createdAt" to (now - 5 * 60 * 60 * 1000L)
                    ),
                    mapOf(
                        "userId" to WORKER1_ID, "role" to "worker",
                        "type" to "CANCELLED",
                        "title" to "עבודה בוטלה ב\"FashionStore\"",
                        "dateTime" to "המשרה בוטלה על ידי המעסיק",
                        "jobId" to jobIds[6],
                        "applicationId" to "",
                        "isRead" to true, "isRated" to false,
                        "createdAt" to (now - 1 * day)
                    )
                ).forEach { notif ->
                    val ref = db.collection("notifications").document()
                    val notifWithId = notif.toMutableMap()
                    notifWithId["id"] = ref.id
                    ref.set(notifWithId)
                }

                // ===== התראות למעסיק =====
                listOf(
                    mapOf(
                        "userId" to EMPLOYER_ID, "role" to "employer",
                        "type" to "PEOPLE",
                        "title" to "5 מועמדים חדשים למשרת מלצר/ית לחתונה",
                        "dateTime" to "לפני שעה",
                        "jobId" to jobIds[0],
                        "applicationId" to "",
                        "isRead" to false, "isRated" to false,
                        "createdAt" to (now - 1 * 60 * 60 * 1000L)
                    ),
                    mapOf(
                        "userId" to EMPLOYER_ID, "role" to "employer",
                        "type" to "CONFIRMED",
                        "title" to "$workerName אישר/ה את העבודה ב\"SpeedEx\"",
                        "dateTime" to "לפני 2 שעות",
                        "jobId" to jobIds[2],
                        "applicationId" to appRef3.id,
                        "isRead" to false, "isRated" to false,
                        "createdAt" to (now - 2 * 60 * 60 * 1000L)
                    ),
                    mapOf(
                        "userId" to EMPLOYER_ID, "role" to "employer",
                        "type" to "RATING",
                        "title" to "דרג את העובדים במשרת מלצר/ית לאירוע קורפורייט",
                        "dateTime" to "דירוגים עוזרים לעובדים למצוא עבודה",
                        "jobId" to jobIds[15],
                        "applicationId" to "",
                        "isRead" to true, "isRated" to false,
                        "createdAt" to (now - 3 * day)
                    )
                ).forEach { notif ->
                    val ref = db.collection("notifications").document()
                    val notifWithId = notif.toMutableMap()
                    notifWithId["id"] = ref.id
                    ref.set(notifWithId)
                }
            }
    }
}