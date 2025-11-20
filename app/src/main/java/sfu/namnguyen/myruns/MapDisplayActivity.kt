package sfu.namnguyen.myruns

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Calendar
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.Toast

class MapDisplayActivity : AppCompatActivity(), OnMapReadyCallback, ServiceConnection {

    private lateinit var mMap: GoogleMap
    private lateinit var trackingService: TrackingService
    private lateinit var viewModel: ExerciseEntryViewModel
    private var isBound = false
    private var isMetric = true
    private lateinit var tvType: TextView
    private lateinit var tvAvgSpeed: TextView
    private lateinit var tvCurSpeed: TextView
    private lateinit var tvClimb: TextView
    private lateinit var tvCalorie: TextView
    private lateinit var tvDistance: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnDelete: Button

    private var isHistoryMode = false
    private var entryId: Long = -1
    private var activityType = "Running"
    private var inputType = "GPS"
    private var currentEntry: ExerciseEntry? = null

    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_display)

        // config the toolbar to navigate back from the map view
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Map Display"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val dao = ExerciseEntryDatabase.getInstance(this).exerciseEntryDao
        val repo = ExerciseEntryRepository(dao)
        val factory = ExerciseEntryViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[ExerciseEntryViewModel::class.java]

        // get preferences
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val unitValue = sharedPrefs.getString("unit_preference", "metric")
        isMetric = unitValue.equals("metric", ignoreCase = true)

        // initialize views
        tvType = findViewById(R.id.tv_type)
        tvAvgSpeed = findViewById(R.id.tv_avg_speed)
        tvCurSpeed = findViewById(R.id.tv_cur_speed)
        tvClimb = findViewById(R.id.tv_climb)
        tvCalorie = findViewById(R.id.tv_calorie)
        tvDistance = findViewById(R.id.tv_distance)

        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnDelete = findViewById(R.id.btnDelete)

        // initialize map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val intentInputType = intent.getStringExtra(StartFragment.INPUT_TYPE_KEY)
        if (intentInputType == "History") {
            isHistoryMode = true
            entryId = intent.getLongExtra("ENTRY_ID_KEY", -1)

            btnSave.visibility = View.GONE
            btnCancel.visibility = View.GONE
            btnDelete.visibility = View.VISIBLE
        } else {
            isHistoryMode = false
            activityType = intent.getStringExtra(StartFragment.ACTIVITY_TYPE_KEY) ?: "Running"
            inputType = intentInputType ?: "GPS"

            btnSave.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
            btnDelete.visibility = View.GONE

            checkPermissions()
        }
        tvType.text = "Type: $activityType"
        btnSave.setOnClickListener { onSaveClicked() }
        btnCancel.setOnClickListener { onCancelClicked() }
        btnDelete.setOnClickListener { onDeleteClicked() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun onDeleteClicked() {
        if (currentEntry != null) {
            AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this exercise entry?")
                .setPositiveButton("Yes") { dialog, _ ->
                    viewModel.deleteEntry(currentEntry!!)
                    Toast.makeText(this, "Entry deleted.", Toast.LENGTH_SHORT).show()
                    finish()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            startTrackingService()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            startTrackingService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingService()
            } else {
                finish()
            }
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java)
        intent.putExtra("INPUT_TYPE", inputType)
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as TrackingService.MyBinder
        trackingService = binder.getService()
        isBound = true

        trackingService.locationList.observe(this) { points ->
            updateMap(points)
        }

        trackingService.currentDist.observe(this) { updateStatsText() }
        trackingService.currentSpeed.observe(this) { updateStatsText() }
        trackingService.currentClimb.observe(this) { updateStatsText() }
        trackingService.currentCalorie.observe(this) { updateStatsText() }

        trackingService.detectedActivity.observe(this) { typeInt ->
            val label = when(typeInt) {
                0 -> "Standing"
                1 -> "Walking"
                2 -> "Running"
                else -> "Other"
            }

            tvType.text = "Type: $label"

            activityType = label
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        isBound = false
    }

    private fun updateStatsText() {
        if (!isBound) return

        val dist = trackingService.currentDist.value ?: 0f
        val speed = trackingService.currentSpeed.value ?: 0f
        val calorie = trackingService.currentCalorie.value ?: 0.0
        val climb = trackingService.currentClimb.value ?: 0.0

        val distMiles = dist / 1609.34
        val speedMph = speed * 2.23694
        val climbMiles = climb / 1609.34

        tvAvgSpeed.text = "Avg speed: ${UnitConverter.formatSpeed(speedMph, isMetric)}"
        tvCurSpeed.text = "Cur speed: ${UnitConverter.formatSpeed(speedMph, isMetric)}"
        tvClimb.text = "Climb: ${UnitConverter.formatDistance(climbMiles, isMetric)}"
        tvCalorie.text = "Calorie: ${UnitConverter.formatDouble(calorie)}"
        tvDistance.text = "Distance: ${UnitConverter.formatDistance(distMiles, isMetric)}"
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        if (isHistoryMode) {
            viewModel.getEntryById(entryId) { entry ->
                runOnUiThread {
                    if (entry != null) {
                        currentEntry = entry

                        if (entry.locationList != null) {
                            updateMap(entry.locationList!!)
                        }

                        tvType.text = "Type: ${ActivityTypeConverter.toName(entry.activityType)}"
                        tvAvgSpeed.text = "Avg speed: ${UnitConverter.formatSpeed(entry.avgSpeed, isMetric)}"
                        tvCurSpeed.text = "Cur speed: N/A"
                        tvClimb.text = "Climb: ${UnitConverter.formatDistance(entry.climb, isMetric)}"
                        tvCalorie.text = "Calorie: ${UnitConverter.formatDouble(entry.calorie)}"
                        tvDistance.text = "Distance: ${UnitConverter.formatDistance(entry.distance, isMetric)}"
                    }
                }
            }
        }
    }

    private fun updateMap(points: ArrayList<LatLng>) {
        if (points.isEmpty()) return

        mMap.clear()

        val polylineOptions = PolylineOptions()
            .color(Color.BLUE)
            .width(10f)
            .addAll(points)
        mMap.addPolyline(polylineOptions)

        mMap.addMarker(MarkerOptions()
            .position(points.first())
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            .title("Start"))

        mMap.addMarker(MarkerOptions()
            .position(points.last())
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            .title("Finish"))

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.last(), 17f))
    }

    private fun onSaveClicked() {
        if (!isHistoryMode && isBound) {
            val points = trackingService.locationList.value ?: ArrayList()

            if (points.isEmpty()) {
                cleanupAndFinish()
                return
            }

            val distMeters = trackingService.currentDist.value ?: 0f
            val speedMs = trackingService.currentSpeed.value ?: 0f
            val calorie = trackingService.currentCalorie.value ?: 0.0
            val climb = trackingService.currentClimb.value ?: 0.0
            val duration = trackingService.currentDuration.value ?: 0.0

            val entry = ExerciseEntry()
            entry.inputType = ActivityTypeConverter.getInputTypeInt(inputType)
            entry.activityType = ActivityTypeConverter.toInt(activityType)
            entry.dateTime = Calendar.getInstance()
            entry.duration = duration
            entry.distance = distMeters / 1609.34
            entry.avgSpeed = speedMs * 2.23694
            entry.calorie = calorie
            entry.climb = climb / 1609.34
            entry.locationList = points

            viewModel.insertEntry(entry)
            Toast.makeText(this, "Entry saved.", Toast.LENGTH_SHORT).show()

            cleanupAndFinish()
        }
    }

    private fun onCancelClicked() {
        Toast.makeText(this, "No entry saved.", Toast.LENGTH_SHORT).show()

        cleanupAndFinish()
    }

    private fun cleanupAndFinish() {
        if (isBound) {
            unbindService(this)
            isBound = false
        }
        val intent = Intent(this, TrackingService::class.java)
        stopService(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(this)
            isBound = false
        }
    }
}