package sfu.namnguyen.myruns

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import java.util.Calendar

class ManualEntryActivity : AppCompatActivity(),
    TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener,
    MyDialogFragment.MyEditDialogListener
{
    private val calendar = Calendar.getInstance()
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var durationEditText: EditText
    private lateinit var distanceEditText: EditText
    private lateinit var caloriesEditText: EditText
    private lateinit var heartRateEditText: EditText
    private lateinit var commentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var selectedInputType: String = ""
    private var selectedActivityType: String = ""
    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel
    private var isMetric: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        selectedInputType = intent.getStringExtra(StartFragment.INPUT_TYPE_KEY) ?: "Manual Entry"
        selectedActivityType = intent.getStringExtra(StartFragment.ACTIVITY_TYPE_KEY) ?: "Running"

        // initialize room components
        val dao = ExerciseEntryDatabase.getInstance(this).exerciseEntryDao
        val repository = ExerciseEntryRepository(dao)
        val viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseEntryViewModel::class.java]

        // toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "MyRuns"

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val unitValue = sharedPrefs.getString("unit_preference", "metric")
        isMetric = unitValue.equals("metric", ignoreCase = true) // Use this

        // find all views
        dateButton = findViewById(R.id.dateButton)
        timeButton = findViewById(R.id.timeButton)
        dateTextView = findViewById(R.id.date)
        timeTextView = findViewById(R.id.time)
        durationEditText = findViewById(R.id.et_duration)
        distanceEditText = findViewById(R.id.et_distance)
        caloriesEditText = findViewById(R.id.et_calories)
        heartRateEditText = findViewById(R.id.et_heart_rate)
        commentEditText = findViewById(R.id.et_comment)

        saveButton = findViewById(R.id.button_save)
        cancelButton = findViewById(R.id.button_cancel)

        // date picker listener
        dateButton.setOnClickListener {
            val datePickerFragment = DatePickerDialog(
                this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerFragment.show()
        }

        // time picker listener
        timeButton.setOnClickListener {
            val timePickerFragment = TimePickerDialog(
                this,
                this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerFragment.show()
        }

        // set up click listeners for the data entry fields to show the custom dialog
        setupEditDialog(durationEditText, MyDialogFragment.DURATION_DIALOG)
        setupEditDialog(distanceEditText, MyDialogFragment.DISTANCE_DIALOG)
        setupEditDialog(caloriesEditText, MyDialogFragment.CALORIES_DIALOG)
        setupEditDialog(heartRateEditText, MyDialogFragment.HEART_RATE_DIALOG)
        setupEditDialog(commentEditText, MyDialogFragment.COMMENT_DIALOG)

        // save and Cancel buttons
        saveButton.setOnClickListener {
            // check if all fields are filled
            val dateSet = dateTextView.text.toString() != "Date:"
            val timeSet = timeTextView.text.toString() != "Time:"
            val durationText = durationEditText.text.toString()
            val distanceText = distanceEditText.text.toString()
            val caloriesText = caloriesEditText.text.toString()
            val heartRateText = heartRateEditText.text.toString()
            val commentText = commentEditText.text.toString()

            if (!dateSet || !timeSet ||
                durationText.isBlank() || distanceText.isBlank() ||
                caloriesText.isBlank() || heartRateText.isBlank()
            ) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                // convert duration input into total seconds
                val totalDurationSeconds = parseDurationToSeconds(durationText)
                val distance = distanceText.toDouble()
                val distanceInMiles = if (isMetric) {
                    distance / UnitConverter.MILES_TO_KM
                } else {
                    distance
                }
                val calories = caloriesText.toDouble()
                val heartRate = heartRateText.toDouble()
                val avgSpeed = if (totalDurationSeconds > 0) {
                    distanceInMiles * 3600.0 / totalDurationSeconds
                } else {
                    0.0
                }
                val avgPace = if (distanceInMiles > 0) {
                    totalDurationSeconds / 60.0 / distanceInMiles
                } else {
                    0.0
                }
                val inputType = 0
                val activityType = ActivityTypeConverter.toInt(selectedActivityType)
                val climb = 0.0

                val newEntry = ExerciseEntry(
                    inputType = inputType,
                    activityType = activityType,
                    dateTime = calendar,
                    duration = totalDurationSeconds,
                    distance = distanceInMiles,
                    avgPace = avgPace,
                    avgSpeed = avgSpeed,
                    calorie = calories,
                    climb = climb,
                    heartRate = heartRate,
                    comment = commentText.ifBlank { null },
                    locationList = null
                )

                exerciseEntryViewModel.insertEntry(newEntry)
                Toast.makeText(this, "Manual Entry saved!", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid input format.", Toast.LENGTH_LONG).show()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun parseDurationToSeconds(durationInput: String): Double {
        val totalMinutes = durationInput.toDouble()
        val minutesPart = totalMinutes.toInt()
        val secondsFromDecimal = (totalMinutes - minutesPart) * 60.0

        return (minutesPart * 60.0) + secondsFromDecimal
    }

    private fun setupEditDialog(editText: EditText, dialogKey: Int) {
        editText.setOnClickListener {
            val dialog = MyDialogFragment()
            val bundle = Bundle()
            // Pass the key to the dialog so it knows what field it's editing
            bundle.putInt(MyDialogFragment.DIALOG_KEY, dialogKey)

            if (dialogKey == MyDialogFragment.DISTANCE_DIALOG) {
                bundle.putBoolean(MyDialogFragment.IS_METRIC_KEY, isMetric)
            }

            dialog.arguments = bundle
            // Show the dialog
            dialog.show(supportFragmentManager, "edit_dialog_$dialogKey")
        }
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val time = String.format("%02d:%02d", hourOfDay, minute)
        timeTextView.text = "Time: $time"
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        dateTextView.text = "Date: ${monthOfYear + 1} / $dayOfMonth / $year"
        calendar.set(year, monthOfYear, dayOfMonth)
    }

    override fun onDialogPositiveClick(dialogKey: Int?, input: String) {
        when (dialogKey) {
            MyDialogFragment.DURATION_DIALOG -> durationEditText.setText(input)
            MyDialogFragment.DISTANCE_DIALOG -> distanceEditText.setText(input)
            MyDialogFragment.CALORIES_DIALOG -> caloriesEditText.setText(input)
            MyDialogFragment.HEART_RATE_DIALOG -> heartRateEditText.setText(input)
            MyDialogFragment.COMMENT_DIALOG -> commentEditText.setText(input)
        }

        currentFocus?.clearFocus()
    }
}