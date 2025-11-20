package sfu.namnguyen.myruns

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.fragment.app.Fragment

class StartFragment : Fragment() {
    private lateinit var spinnerInput: AutoCompleteTextView
    private lateinit var spinnerActivity: AutoCompleteTextView
    private lateinit var startButton: Button
    private val inputTypes = arrayOf("Manual Entry", "GPS", "Automatic")
    private val activityTypes = arrayOf("Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing", "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other")

    companion object {
        const val INPUT_TYPE_KEY = "input_type"
        const val ACTIVITY_TYPE_KEY = "activity_type"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        // For better styling, I have used the GitHub docs (cited in fragment_start.xml) for the dropdown menus.
        // Basically, instead of using Spinner, I used AutoCompleteTextView.
        spinnerInput = view.findViewById(R.id.exposed_dropdown_input_type)
        val adapterInput = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, inputTypes)
        spinnerInput.setAdapter(adapterInput)

        // Set the default value to "Manual Entry"
        spinnerInput.setText(inputTypes[0], false)

        spinnerActivity = view.findViewById(R.id.exposed_dropdown_activity_type)
        val adapterActivity = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, activityTypes)
        spinnerActivity.setAdapter(adapterActivity)

        // Set the default value to "Running"
        spinnerActivity.setText(activityTypes[0], false)

        startButton = view.findViewById(R.id.button_start)
        startButton.setOnClickListener {
            val inputType = spinnerInput.text.toString()
            val activityType = spinnerActivity.text.toString()

            val intent: Intent = when(inputType) {
                "Manual Entry" -> {
                    Intent(requireContext(), ManualEntryActivity::class.java)
                }

                "GPS", "Automatic" -> {
                    Intent(requireContext(), MapDisplayActivity::class.java)
                }

                else -> return@setOnClickListener
            }

            intent.putExtra(INPUT_TYPE_KEY, inputType)
            intent.putExtra(ACTIVITY_TYPE_KEY, activityType)
            startActivity(intent)
        }

        return view
    }
}