package com.example.clickjob_finalproject

import com.google.firebase.firestore.FirebaseFirestore

object SeedUsers {

    private val db = FirebaseFirestore.getInstance()

    // ===== Real Firebase Auth users =====
    private const val GAL_ID    = "Bqp5ddpJXjd8A9XIXmYQnyTg0O03"
    private const val NOA_ID    = "b4DqRCqjFRaRCvw9Dww7hjjqW6t2"
    private const val YOSI_ID   = "deRhC5qnvUaoGHvL07OiqkfW5zV2"
    private const val DANA_ID   = "NAY1uD5jMEhoZpHWT5pZAe0DBMA2"
    private const val RON_ID    = "Zzj4RQROP3hslycmWP7UDEsuPH83"
    private const val MICHAL_ID = "W2hNEiyQE6UUu1wIn5qVAWdEUjf1"
    private const val AVI_ID    = "e2O0D8DhwaV8BdS4cgNF5RnKsku1"

    // ===== Filler workers (Firestore profiles only, no Auth login needed) =====
    private const val TOMER_ID = "seed_worker_tomer"
    private const val LIHI_ID  = "seed_worker_lihi"
    private const val OR_ID    = "seed_worker_or"

    fun seedAll() {
        val users = listOf(

            // ===== גל דרעי - the demo star: worker + employer (device A) =====
            mapOf(
                "id"              to GAL_ID,
                "name"            to "גל דרעי",
                "phone"           to "0545595563",
                "email"           to "gal@tast.com",
                "address"         to "תל אביב",
                "city"            to "תל אביב",
                "searchRadius"    to 10,
                "availableDays"   to listOf("ראשון", "שני", "רביעי"),
                "languages"       to listOf("עברית", "אנגלית"),
                "licenses"        to listOf("רישיון B", "רישיון D"),
                "certificates"    to listOf("בגרות", "תואר ראשון", "תעודת הצלה"),
                "software"        to listOf("Excel", "Word", "PowerPoint"),
                "jobCategories"   to listOf("חינוך והוראה", "בעלי חיים", "רפואה ובריאות", "טכנולוגיה"),
                "softSkills"      to listOf("אסרטיביות", "עבודת צוות", "יצירתיות", "פתרון בעיות", "תקשורת בינאישית", "מנהיגות"),
                "bio"             to "שמי גל דרעי. מחפשת עבודה בתחומים: חינוך והוראה, בעלי חיים, רפואה ובריאות, טכנולוגיה. זמינה בימים: ראשון, שני, רביעי.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=גל+דרעי&background=CE3E8B&color=fff&size=200",
                "rating"          to 4.7,
                "ratingsCount"    to 12,
                "hasPostedJob"    to true,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== נועה טל - filler worker (real Auth) =====
            mapOf(
                "id"              to NOA_ID,
                "name"            to "נועה טל",
                "phone"           to "0521234567",
                "email"           to "noa@tast.com",
                "address"         to "רמת גן",
                "city"            to "רמת גן",
                "searchRadius"    to 15,
                "availableDays"   to listOf("ראשון", "שלישי", "חמישי"),
                "languages"       to listOf("עברית", "אנגלית", "צרפתית"),
                "licenses"        to listOf("רישיון B"),
                "certificates"    to listOf("בגרות", "תואר ראשון במדעי המחשב"),
                "software"        to listOf("Excel", "Figma", "Photoshop"),
                "jobCategories"   to listOf("טכנולוגיה", "עיצוב וקריאייטיב", "שירות לקוחות"),
                "softSkills"      to listOf("יצירתיות", "עבודת צוות", "דיוק", "תקשורת בינאישית"),
                "bio"             to "שמי נועה טל. מחפשת עבודה בתחומי טכנולוגיה ועיצוב. זמינה בימים: ראשון, שלישי, חמישי.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=נועה+טל&background=68BAC6&color=fff&size=200",
                "rating"          to 4.9,
                "ratingsCount"    to 8,
                "hasPostedJob"    to false,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== יוסי כהן - filler worker (real Auth) =====
            mapOf(
                "id"              to YOSI_ID,
                "name"            to "יוסי כהן",
                "phone"           to "0531234567",
                "email"           to "yosi@tast.com",
                "address"         to "פתח תקווה",
                "city"            to "פתח תקווה",
                "searchRadius"    to 20,
                "availableDays"   to listOf("שני", "רביעי", "שישי"),
                "languages"       to listOf("עברית", "אנגלית", "ערבית"),
                "licenses"        to listOf("רישיון B", "רישיון C"),
                "certificates"    to listOf("בגרות", "תעודת אבטחה"),
                "software"        to listOf("Excel", "Word"),
                "jobCategories"   to listOf("אבטחה וביטחון", "משלוחים ותחבורה", "מסעדות"),
                "softSkills"      to listOf("אחריות", "אמינות", "עבודת צוות", "כושר גופני"),
                "bio"             to "שמי יוסי כהן. מחפש עבודה בתחומי אבטחה ומשלוחים. זמין בימים: שני, רביעי, שישי.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=יוסי+כהן&background=CE3E8B&color=fff&size=200",
                "rating"          to 4.5,
                "ratingsCount"    to 15,
                "hasPostedJob"    to false,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== דנה נחום - מסעדת הים - the demo employer (device B) =====
            mapOf(
                "id"              to DANA_ID,
                "name"            to "דנה נחום",
                "phone"           to "0541234567",
                "email"           to "dana@tast.com",
                "address"         to "תל אביב",
                "city"            to "תל אביב",
                "searchRadius"    to 10,
                "availableDays"   to listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי"),
                "languages"       to listOf("עברית", "אנגלית"),
                "licenses"        to listOf("רישיון B"),
                "certificates"    to listOf("בגרות", "תואר ראשון בניהול מסעדות"),
                "software"        to listOf("Excel", "Word"),
                "jobCategories"   to listOf("מסעדות", "רפואה ובריאות", "הפקה ואירועים"),
                "softSkills"      to listOf("ניהול", "תקשורת בינאישית", "יצירתיות", "עבודת צוות"),
                "bio"             to "שמי דנה נחום. בעלת מסעדת הים בתל אביב. מחפשת עובדים מוכשרים.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=דנה+נחום&background=68BAC6&color=fff&size=200",
                "rating"          to 4.8,
                "ratingsCount"    to 20,
                "hasPostedJob"    to true,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== רון ביטון - employer, אולם רויאל =====
            mapOf(
                "id"              to RON_ID,
                "name"            to "רון ביטון",
                "phone"           to "0551234567",
                "email"           to "ron@gmail.com",
                "address"         to "רמת גן",
                "city"            to "רמת גן",
                "searchRadius"    to 15,
                "availableDays"   to listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי"),
                "languages"       to listOf("עברית", "אנגלית"),
                "licenses"        to listOf("רישיון B"),
                "certificates"    to listOf("בגרות", "תואר ראשון בניהול עסקים"),
                "software"        to listOf("Excel", "Word", "PowerPoint"),
                "jobCategories"   to listOf("הפקה ואירועים", "אבטחה וביטחון", "מסעדות"),
                "softSkills"      to listOf("ניהול", "ארגון", "תקשורת בינאישית", "מנהיגות"),
                "bio"             to "שמי רון ביטון. מנהל אולם האירועים רויאל ברמת גן.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=רון+ביטון&background=CE3E8B&color=fff&size=200",
                "rating"          to 4.6,
                "ratingsCount"    to 18,
                "hasPostedJob"    to true,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== מיכל ינאי - employer, TechStart =====
            mapOf(
                "id"              to MICHAL_ID,
                "name"            to "מיכל ינאי",
                "phone"           to "0561234567",
                "email"           to "michal@tast.com",
                "address"         to "הרצליה",
                "city"            to "הרצליה",
                "searchRadius"    to 20,
                "availableDays"   to listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי"),
                "languages"       to listOf("עברית", "אנגלית"),
                "licenses"        to listOf("רישיון B"),
                "certificates"    to listOf("בגרות", "תואר ראשון במדעי המחשב", "MBA"),
                "software"        to listOf("Excel", "Jira", "Slack", "Figma"),
                "jobCategories"   to listOf("טכנולוגיה", "עיצוב וקריאייטיב", "שירות לקוחות"),
                "softSkills"      to listOf("ניהול", "חשיבה אנליטית", "תקשורת בינאישית", "יצירתיות"),
                "bio"             to "שמי מיכל ינאי. מנהלת חברת TechStart בהרצליה.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=מיכל+ינאי&background=68BAC6&color=fff&size=200",
                "rating"          to 4.3,
                "ratingsCount"    to 10,
                "hasPostedJob"    to true,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== אבי לוי - employer (multi-business) that also takes shifts =====
            mapOf(
                "id"              to AVI_ID,
                "name"            to "אבי לוי",
                "phone"           to "0571234567",
                "email"           to "avi@tast.com",
                "address"         to "תל אביב",
                "city"            to "תל אביב",
                "searchRadius"    to 25,
                "availableDays"   to listOf("ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי"),
                "languages"       to listOf("עברית", "אנגלית", "רוסית"),
                "licenses"        to listOf("רישיון B", "רישיון C", "רישיון D"),
                "certificates"    to listOf("בגרות", "תואר ראשון בחינוך"),
                "software"        to listOf("Excel", "Word"),
                "jobCategories"   to listOf("חינוך והוראה", "בעלי חיים", "משלוחים ותחבורה"),
                "softSkills"      to listOf("סבלנות", "אחריות", "אמינות", "תקשורת בינאישית"),
                "bio"             to "שמי אבי לוי. בעלים של EduCenter, PetCare ו-SpeedEx.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=אבי+לוי&background=CE3E8B&color=fff&size=200",
                "rating"          to 4.9,
                "ratingsCount"    to 25,
                "hasPostedJob"    to true,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== תומר שגב - filler worker (no Auth, applications only) =====
            mapOf(
                "id"              to TOMER_ID,
                "name"            to "תומר שגב",
                "phone"           to "0526673481",
                "email"           to "tomer@tast.com",
                "address"         to "גבעתיים",
                "city"            to "גבעתיים",
                "searchRadius"    to 15,
                "availableDays"   to listOf("ראשון", "שני", "חמישי", "שישי"),
                "languages"       to listOf("עברית", "אנגלית"),
                "licenses"        to listOf("רישיון B"),
                "certificates"    to listOf("בגרות"),
                "software"        to listOf("Excel"),
                "jobCategories"   to listOf("מסעדות", "הפקה ואירועים"),
                "softSkills"      to listOf("שירותיות", "עבודת צוות", "אנרגטיות"),
                "bio"             to "שמי תומר שגב. ברמן ומלצר עם ניסיון של 3 שנים באירועים ובבארים.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=תומר+שגב&background=CE3E8B&color=fff&size=200",
                "rating"          to 4.4,
                "ratingsCount"    to 9,
                "hasPostedJob"    to false,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== ליהי ברק - filler worker (no Auth, applications only) =====
            mapOf(
                "id"              to LIHI_ID,
                "name"            to "ליהי ברק",
                "phone"           to "0546218854",
                "email"           to "lihi@tast.com",
                "address"         to "חולון",
                "city"            to "חולון",
                "searchRadius"    to 20,
                "availableDays"   to listOf("שני", "שלישי", "רביעי"),
                "languages"       to listOf("עברית", "אנגלית", "ספרדית"),
                "licenses"        to listOf("רישיון B"),
                "certificates"    to listOf("בגרות", "קורס ברמנות"),
                "software"        to listOf("Excel", "Word"),
                "jobCategories"   to listOf("מסעדות", "שירות לקוחות", "מכירות ואופנה"),
                "softSkills"      to listOf("שירותיות", "תקשורת בינאישית", "אחריות"),
                "bio"             to "שמי ליהי ברק. מלצרית וברמנית, אוהבת עבודה עם אנשים ואווירה טובה.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=ליהי+ברק&background=68BAC6&color=fff&size=200",
                "rating"          to 4.8,
                "ratingsCount"    to 11,
                "hasPostedJob"    to false,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            ),

            // ===== אור מזרחי - filler worker (no Auth, applications only) =====
            mapOf(
                "id"              to OR_ID,
                "name"            to "אור מזרחי",
                "phone"           to "0538812947",
                "email"           to "or@tast.com",
                "address"         to "בת ים",
                "city"            to "בת ים",
                "searchRadius"    to 25,
                "availableDays"   to listOf("ראשון", "רביעי", "חמישי", "שישי"),
                "languages"       to listOf("עברית"),
                "licenses"        to listOf("רישיון B", "רישיון A2"),
                "certificates"    to listOf("בגרות"),
                "software"        to listOf("Word"),
                "jobCategories"   to listOf("מסעדות", "משלוחים ותחבורה"),
                "softSkills"      to listOf("זריזות", "אמינות", "עצמאות"),
                "bio"             to "שמי אור מזרחי. שליח ומלצר, זמין למשמרות ערב וסופי שבוע.",
                "profileImageUrl" to "https://ui-avatars.com/api/?name=אור+מזרחי&background=CE3E8B&color=fff&size=200",
                "rating"          to 4.2,
                "ratingsCount"    to 6,
                "hasPostedJob"    to false,
                "jobMatches"      to emptyList<Any>(),
                "upcomingShifts"  to emptyList<Any>(),
                "createdAt"       to System.currentTimeMillis()
            )
        )

        users.forEach { user ->
            val userId = user["id"] as String
            db.collection("candidates").document(userId)
                .set(user)
                .addOnSuccessListener {
                    android.util.Log.d("SeedUsers", "User saved: ${user["name"]}")
                }
                .addOnFailureListener {
                    android.util.Log.e("SeedUsers", "Failed to save: ${user["name"]} - ${it.message}")
                }
        }
    }
}