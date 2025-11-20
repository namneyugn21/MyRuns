package sfu.namnguyen.myruns

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.sqrt

// Referenced: XD's lecture demo
class TrackingService : Service(), LocationListener, SensorEventListener {
    private val binder = MyBinder()
    private lateinit var locationManager: LocationManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private val NOTIFICATION_ID = 777
    private val CHANNEL_ID = "tracking_channel"
    private val BLOCK_SIZE = 64

    val locationList = MutableLiveData<ArrayList<LatLng>>()
    val currentSpeed = MutableLiveData<Float>()
    val currentDist = MutableLiveData<Float>()
    val currentClimb = MutableLiveData<Double>()
    val currentCalorie = MutableLiveData<Double>()
    val currentDuration = MutableLiveData<Double>()
    val detectedActivity = MutableLiveData<Int>() // livedata for activity recognition (0 = Standing, 1 = Walking, 2 = Running, 3 = Other)

    private val points = ArrayList<LatLng>()
    private var lastLocation: Location? = null
    private var totalDistance = 0f
    private var totalClimb = 0.0
    private var startTime = 0L

    private val mAccBuffer = LinkedBlockingQueue<Double>(2048)
    private var isRunning = false
    private lateinit var classifyThread: Thread
    private val fft = FFT(BLOCK_SIZE)

    inner class MyBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // initialize notification manager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        // initialize location manager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
        }

        val pendingIntent = Intent(this, MapDisplayActivity::class.java).let {
            it.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MyRuns Tracking Service")
            .setContentText("Recording your path...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        val inputType = intent?.getStringExtra("INPUT_TYPE")

        if (inputType == "Automatic") {
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (sensor != null) {
                accelerometer = sensor
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)

                isRunning = true
                classifyThread = Thread { runClassifier() }
                classifyThread.start()
            } else {
                System.err.println("Accelerometer not found!")
            }
        }

        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()

            // calculate magnitude
            val mag = sqrt(x * x + y * y + z * z)

            try {
                mAccBuffer.add(mag)
            } catch (e: IllegalStateException) {
                mAccBuffer.poll()
                mAccBuffer.add(mag)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun runClassifier() {
        val re = DoubleArray(BLOCK_SIZE)
        val im = DoubleArray(BLOCK_SIZE)

        val finalInput = arrayOfNulls<Any>(BLOCK_SIZE + 1)

        while (isRunning) {
            if (mAccBuffer.size >= BLOCK_SIZE) {
                var maxVal = -1.0

                for (i in 0 until BLOCK_SIZE) {
                    val value = mAccBuffer.poll() ?: 0.0
                    re[i] = value
                    im[i] = 0.0
                    if (value > maxVal) maxVal = value
                }

                fft.fft(re, im)

                for (i in 0 until BLOCK_SIZE) {
                    val mag = sqrt(re[i] * re[i] + im[i] * im[i])
                    finalInput[i] = mag
                }

                finalInput[BLOCK_SIZE] = maxVal

                try {
                    val result = WekaClassifier.classify(finalInput)
                    detectedActivity.postValue(result.toInt())
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        points.add(latLng)
        locationList.postValue(points)

        if (lastLocation != null) {
            val distanceChanged = lastLocation!!.distanceTo(location)
            totalDistance += distanceChanged

            val speed = location.speed
            val altitudeChanged = location.altitude - lastLocation!!.altitude
            if (altitudeChanged > 0) {
                totalClimb += altitudeChanged
            }
            val calories = totalDistance * 0.06 // I asked Google Gemini to help me calculate calories (approximately)

            val durationSeconds = (System.currentTimeMillis() - startTime) / 1000

            currentDuration.postValue(durationSeconds.toDouble())
            currentDist.postValue(totalDistance)
            currentSpeed.postValue(speed)
            currentClimb.postValue(totalClimb)
            currentCalorie.postValue(calories)
        }
        lastLocation = location
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MyRuns Tracking Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cleanupTasks()
        stopSelf()
    }

    private fun cleanupTasks() {
        notificationManager.cancel(NOTIFICATION_ID)

        // stop gps
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }

        // stop sensor
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }

        // stop thread
        isRunning = false
        if (::classifyThread.isInitialized) {
            classifyThread.interrupt()
        }
    }
}