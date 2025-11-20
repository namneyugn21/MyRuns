package sfu.namnguyen.myruns

object ActivityTypeConverter {
    private val activityTypes = arrayOf(
        "Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing",
        "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming",
        "Mountain Biking", "Wheelchair", "Elliptical", "Other"
    )

    fun toInt(activityName: String): Int {
        return activityTypes.indexOf(activityName).let {
            // return index or -1 if not found for safety
            if (it != -1) it else 0
        }
    }

    fun toName(activityCode: Int): String {
        return if (activityCode >= 0 && activityCode < activityTypes.size) {
            activityTypes[activityCode]
        } else {
            "Unknown"
        }
    }

    fun inputTypeToName(inputCode: Int): String {
        return when(inputCode) {
            0 -> "Manual Entry"
            1 -> "GPS"
            2 -> "Automatic"
            else -> "Unknown"
        }
    }

    fun getInputTypeInt(inputName: String): Int {
        return when(inputName) {
            "Manual Entry" -> 0
            "GPS" -> 1
            "Automatic" -> 2
            else -> 0
        }
    }
}