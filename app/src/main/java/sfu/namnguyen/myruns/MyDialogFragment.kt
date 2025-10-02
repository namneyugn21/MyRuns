package sfu.namnguyen.myruns

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.widget.EditText

class MyDialogFragment : DialogFragment() {
    interface MyEditDialogListener {
        fun onDialogPositiveClick(dialogKey: Int?, input: String)
    }
    companion object {
        const val DIALOG_KEY = "dialog_key"
        const val DURATION_DIALOG = 0
        const val DISTANCE_DIALOG = 1
        const val CALORIES_DIALOG = 2
        const val HEART_RATE_DIALOG = 3
        const val COMMENT_DIALOG = 4
    }

    private lateinit var dialogTitle: String
    private lateinit var inputUnit: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogKey = arguments?.getInt(DIALOG_KEY)
        var keyboardInputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        when (dialogKey) {
            DURATION_DIALOG -> {
                dialogTitle = "Duration"
                inputUnit = "minutes"
            }
            DISTANCE_DIALOG -> {
                dialogTitle = "Distance"
                inputUnit = "km"
            }
            CALORIES_DIALOG -> {
                dialogTitle = "Calories"
                inputUnit = "kcal"
            }
            HEART_RATE_DIALOG -> {
                dialogTitle = "Heart Rate"
                inputUnit = "bpm"
            }
            COMMENT_DIALOG -> {
                dialogTitle = "Comment"
                inputUnit = "text"
                keyboardInputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }
            else -> {
                dialogTitle = "Error"
                inputUnit = ""
            }
        }

        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.fragment_my_dialog, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextDialog)
        editText.inputType = keyboardInputType

        if (dialogKey == COMMENT_DIALOG) {
            editText.hint = "Enter your comment here"
        }

        builder.setView(dialogView)
            .setTitle(dialogTitle)
            .setMessage("Enter $dialogTitle" + if (inputUnit.isNotEmpty()) " ($inputUnit)" else "")
            .setPositiveButton("Confirm") { _, _ ->
                val input = editText.text.toString()
                (activity as MyEditDialogListener).onDialogPositiveClick(dialogKey, input)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        return builder.create()
    }
}