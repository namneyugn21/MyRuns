package sfu.namnguyen.myruns

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

//    id: Long                           // Primary Key
//    inputType: Int                     // Manual, GPS or automatic
//    activityType: Int                  // Running, cycling etc.
//    dateTime: Calendar                 // When does this entry happen
//    duration: Double                   // Exercise duration in seconds
//    distance: Double                   // Distance traveled. Either in meters or feet.
//    avgPace: Double                    // Average pace
//    avgSpeed: Double                   // Average speed
//    calorie: Double                    // Calories burnt
//    climb: Double                      // Climb. Either in meters or feet.
//    heartRate: Double                  // Heart rate
//    comment: String                    // Comments
//    locationList: ArrayList <LatLng>   // Location list

@Entity(tableName = "exercise_entries_table")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "input_type")
    var inputType: Int = 0, // 0: Manual, 1: GPS, 2: Automatic

    @ColumnInfo(name = "activity_type")
    var activityType: Int = 0,

    @ColumnInfo(name = "date_time")
    var dateTime: Calendar = Calendar.getInstance(),

    @ColumnInfo(name = "duration")
    var duration: Double = 0.0,

    @ColumnInfo(name = "distance")
    var distance: Double = 0.0,

    @ColumnInfo(name = "avg_pace")
    var avgPace: Double = 0.0,

    @ColumnInfo(name = "avg_speed")
    var avgSpeed: Double = 0.0,

    @ColumnInfo(name = "calorie")
    var calorie: Double = 0.0,

    @ColumnInfo(name = "climb")
    var climb: Double = 0.0,

    @ColumnInfo(name = "heart_rate")
    var heartRate: Double = 0.0,

    @ColumnInfo(name = "comment")
    var comment: String? = null,

    @ColumnInfo(name = "location_list")
    var locationList: ArrayList<LatLng>? = null
)

