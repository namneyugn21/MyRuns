package sfu.namnguyen.myruns

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object UnitConverter {
    const val MILES_TO_KM = 1.60934

    fun formatDistance(distanceInMiles: Double, isMetric: Boolean): String {
        return if (isMetric) {
            val km = distanceInMiles * MILES_TO_KM
            String.format(Locale.getDefault(), "%.2f Kilometers", km)
        } else {
            String.format(Locale.getDefault(), "%.2f Miles", distanceInMiles)
        }
    }

    fun formatSpeed(speedInMph: Double, isMetric: Boolean): String {
        return if (isMetric) {
            val kph = speedInMph * MILES_TO_KM
            String.format(Locale.getDefault(), "%.2f km/h", kph)
        } else {
            String.format(Locale.getDefault(), "%.2f mph", speedInMph)
        }
    }

    fun formatPace(paceInMinPerMile: Double, isMetric: Boolean): String {
        return if (isMetric) {
            val minPerKm = paceInMinPerMile / MILES_TO_KM
            String.format(Locale.getDefault(), "%.2f min/km", minPerKm)
        } else {
            String.format(Locale.getDefault(), "%.2f min/mile", paceInMinPerMile)
        }
    }

    fun formatDuration(durationInSeconds: Double): String {
        val seconds = durationInSeconds.toInt()
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return when {
            hours > 0 -> String.format(Locale.getDefault(), "%d hr %d mins %d secs", hours, minutes, remainingSeconds)
            minutes > 0 -> String.format(Locale.getDefault(), "%d mins %d secs", minutes, remainingSeconds)
            else -> String.format(Locale.getDefault(), "%d secs", remainingSeconds)
        }
    }

    fun formatDateTime(calendar: Calendar): String {
        val format = SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.getDefault())
        return format.format(calendar.time)
    }

    fun formatDouble(value: Double): String {
        return String.format(Locale.getDefault(), "%.2f", value)
    }
}