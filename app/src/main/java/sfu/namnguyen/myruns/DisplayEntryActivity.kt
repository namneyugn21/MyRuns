package sfu.namnguyen.myruns

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager

class DisplayEntryActivity : AppCompatActivity() {

    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel
    private var currentEntry: ExerciseEntry? = null
    private var isMetric: Boolean = false
    private lateinit var inputTypeTextView: TextView
    private lateinit var activityTypeTextView: TextView
    private lateinit var dateTimeTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var avgPaceTextView: TextView
    private lateinit var avgSpeedTextView: TextView
    private lateinit var climbTextView: TextView
    private lateinit var calorieTextView: TextView
    private lateinit var heartRateTextView: TextView
    private lateinit var commentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_entry)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "MyRuns"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        inputTypeTextView = findViewById(R.id.tv_input_type)
        activityTypeTextView = findViewById(R.id.tv_activity_type)
        dateTimeTextView = findViewById(R.id.tv_date_time)
        durationTextView = findViewById(R.id.tv_duration)
        distanceTextView = findViewById(R.id.tv_distance)
        avgPaceTextView = findViewById(R.id.tv_avg_pace)
        avgSpeedTextView = findViewById(R.id.tv_avg_speed)
        climbTextView = findViewById(R.id.tv_climb)
        calorieTextView = findViewById(R.id.tv_calories)
        heartRateTextView = findViewById(R.id.tv_heart_rate)
        commentTextView = findViewById(R.id.tv_comment)

        val deleteButton: Button = findViewById(R.id.button_delete)

        val dao = ExerciseEntryDatabase.getInstance(applicationContext).exerciseEntryDao
        val repository = ExerciseEntryRepository(dao)
        val viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseEntryViewModel::class.java]

        val sharedPrefs = getSharedPreferences(
            "androidx.preference_preferences",
            MODE_PRIVATE
        )
        val unitValue = sharedPrefs.getString("unit_preference", "metric")
        isMetric = unitValue == "metric"

        val entryId = intent.getLongExtra("ENTRY_ID_KEY", -1L)

        if (entryId != -1L) {
            exerciseEntryViewModel.getEntryById(entryId) { entry ->
                if (entry != null) {
                    currentEntry = entry
                    runOnUiThread {
                        displayEntryDetails(entry)
                    }
                } else {
                    Toast.makeText(this, "Error: Entry not found.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            Toast.makeText(this, "Error: Invalid entry ID.", Toast.LENGTH_SHORT).show()
            finish()
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayEntryDetails(entry: ExerciseEntry) {
        // set the Toolbar title based on the activity type
        supportActionBar?.title = ActivityTypeConverter.toName(entry.activityType)

        // display Manual Entry fields
        inputTypeTextView.text = ActivityTypeConverter.inputTypeToName(entry.inputType)
        activityTypeTextView.text = ActivityTypeConverter.toName(entry.activityType)
        dateTimeTextView.text = UnitConverter.formatDateTime(entry.dateTime)
        durationTextView.text = UnitConverter.formatDuration(entry.duration)
        distanceTextView.text = UnitConverter.formatDistance(entry.distance, isMetric)
        avgPaceTextView.text = UnitConverter.formatPace(entry.avgPace, isMetric)
        avgSpeedTextView.text = UnitConverter.formatSpeed(entry.avgSpeed, isMetric)
        climbTextView.text = UnitConverter.formatDouble(entry.climb)
        calorieTextView.text = UnitConverter.formatDouble(entry.calorie) + " cals"
        heartRateTextView.text = UnitConverter.formatDouble(entry.heartRate) + " bpm"
        commentTextView.text = entry.comment ?: "No comments"
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this exercise entry?")
            .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                currentEntry?.let {
                    exerciseEntryViewModel.deleteEntry(it)
                    Toast.makeText(this, "Entry deleted.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // when the unit preference is changed, update the display
    override fun onResume() {
        super.onResume()
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val unitValue = sharedPrefs.getString("unit_preference", "metric")
        val newIsMetric = (unitValue == "metric")

        if (isMetric != newIsMetric) {
            isMetric = newIsMetric
            currentEntry?.let {
                displayEntryDetails(it)
            }
        }
    }
}