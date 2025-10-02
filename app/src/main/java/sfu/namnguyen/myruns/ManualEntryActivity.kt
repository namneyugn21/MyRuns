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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "MyRuns"

        // Find all views
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

        // Date Picker Listener
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

        // Time Picker Listener
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

        // Set up click listeners for the data entry fields to show the custom dialog
        setupEditDialog(durationEditText, MyDialogFragment.DURATION_DIALOG)
        setupEditDialog(distanceEditText, MyDialogFragment.DISTANCE_DIALOG)
        setupEditDialog(caloriesEditText, MyDialogFragment.CALORIES_DIALOG)
        setupEditDialog(heartRateEditText, MyDialogFragment.HEART_RATE_DIALOG)
        setupEditDialog(commentEditText, MyDialogFragment.COMMENT_DIALOG)

        // Save and Cancel buttons
        saveButton.setOnClickListener {
            // Check if all fields are filled
            val dateSet = dateTextView.text.toString() != "Date:"
            val timeSet = timeTextView.text.toString() != "Time:"
            val duration = durationEditText.text.toString()
            val distance = distanceEditText.text.toString()
            val calories = caloriesEditText.text.toString()
            val heartRate = heartRateEditText.text.toString()

            if (!dateSet || !timeSet ||
                duration.isBlank() || distance.isBlank() ||
                calories.isBlank() || heartRate.isBlank()
            ) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Entry saved!", Toast.LENGTH_LONG).show()
            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }
    private fun setupEditDialog(editText: EditText, dialogKey: Int) {
        editText.setOnClickListener {
            val dialog = MyDialogFragment()
            val bundle = Bundle()
            // Pass the key to the dialog so it knows what field it's editing
            bundle.putInt(MyDialogFragment.DIALOG_KEY, dialogKey)
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