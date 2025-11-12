package sfu.namnguyen.myruns

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Reference:
// https://developer.android.com/training/data-storage/room/referencing-data
class Converters {
    // we will convert calendar data into milliseconds since epoch
    @TypeConverter
    fun calendarToDatestamp(calendar: Calendar): Long {
        return calendar.timeInMillis
    }

    // then, we will convert the milliseconds back to calendar
    @TypeConverter
    fun datestampToCalendar(value: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = value
        return calendar
    }

    // convert arraylist to string and vice versa
    @TypeConverter
    fun locationListToString(locationList: ArrayList<LatLng>?): String? {
        if (locationList == null) return null
        return Gson().toJson(locationList)
    }

    @TypeConverter
    fun stringToLocationList(locationListString: String?): ArrayList<LatLng>? {
        if (locationListString == null) return null
        val type = object : TypeToken<ArrayList<LatLng>>() {}.type
        return Gson().fromJson(locationListString, type)
    }
}