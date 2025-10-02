package sfu.namnguyen.myruns

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MapActivity : AppCompatActivity() {
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Map"

        saveButton = findViewById(R.id.button_save)
        cancelButton = findViewById(R.id.button_cancel)

        // Save and Cancel buttons
        saveButton.setOnClickListener {
            finish()
        }
        cancelButton.setOnClickListener {
            finish()
        }

    }
}