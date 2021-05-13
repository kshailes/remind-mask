package com.example.coronasafety

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.Ringtone
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {

    val GEOFENCE_KEY = "GEOFENCE_KEY"

    lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null
    lateinit var geofencingClient: GeofencingClient
    private val geofenceList = mutableListOf<Geofence>()

    private lateinit var textview: TextView
    private lateinit var actionTextView: AppCompatCheckBox
    private lateinit var imageViewGif: ImageView

    private var movementType: Int = 0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textview = findViewById(R.id.text_view)
        imageViewGif = findViewById(R.id.iv_gif)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val type = intent?.extras?.get("flag") ?: 0
        type as Int
        this.movementType = type
        if (type != 0) {
            check(type)
            return
        }

        if (!isLocationEnabled(this)) {
            buildAlertMessage(this, "Your GPS seems to be disabled, do you want to enable it?",
                { dialog, id ->
                    run {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }, { dialog, id ->
                    run {
                        dialog.cancel()
                        this.finish()
                    }
                })
        } else {
            checkLocationPermission(this, 100)
        }

        addLocation()

        transitions()

    }

    @SuppressLint("MissingPermission")
    fun addLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                this.location = location
                if (location == null) {
                    showToast(this, "fail to add Geofences")
                }
                location?.let {
                    showToast(this, "${location.latitude} , ${location.longitude}")
                    textview.setText("${location.latitude} , ${location.longitude}")
                    temp(location)
                }
            }
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "No"
            ) { dialog, id ->
                run {
                    dialog.cancel()
                    this.finish()
                }
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty())
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        showToast(this, "Permission Given")
                        addLocation()
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        showToast(this, "Permission Not Given")
                        buildAlertMessage(this,
                            "Location Permission is necessary for working of this app.",
                            { dialog, id ->
                                run {
                                    checkLocationPermission(this, 100)
                                }
                            },
                            { dialog, id ->
                                run {
                                    dialog.cancel()
                                    this.finish()
                                }
                            })
                    }
            }
        }
    }

    fun transitions() {
        val transitions = mutableListOf<ActivityTransition>()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()

        transitions +=
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()

        val request = ActivityTransitionRequest(transitions)

        val task = ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, geofencePendingIntent)

        task.addOnSuccessListener {
            // Handle success
            showToast(this, "transition added")
        }

        task.addOnFailureListener { e: Exception ->
            // Handle error
            showToast(this, "transition failed to add")
        }
    }

    private var ringtone: Ringtone? = null

    fun check(movementType: Int) {
        actionTextView = findViewById(R.id.tv_action)
        actionTextView.visibility = View.VISIBLE
        textview.visibility = View.GONE
        imageViewGif.visibility = View.VISIBLE
        if (movementType == 1) {
            actionTextView.setText("Wash Your hands")
        } else if (movementType == 2 || movementType == 4 || movementType == 3) {
            actionTextView.setText("Take Your MASK")
        } else {
            actionTextView.setText("Do Something bro")
        }
        ringtone = startRingTone(this)
    }

    fun temp(location: Location) {
        geofencingClient = LocationServices.getGeofencingClient(this)

        geofenceList.add(
            Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(GEOFENCE_KEY)

                // Set the circular region of this geofence.
                .setCircularRegion(
                    location.latitude,
                    location.longitude,
                    0.1F
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(1000 * 60 * 60)
                .setLoiteringDelay(100000)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or
                            Geofence.GEOFENCE_TRANSITION_EXIT
                )

                // Create the geofence.
                .build()
        )
        run()
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER or
                        GeofencingRequest.INITIAL_TRIGGER_EXIT
            )
            addGeofences(geofenceList)
        }.build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    private fun run() {

        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent).run {
            addOnSuccessListener {
                // Geofences added
                // ...
                showToast(this@MainActivity, "Geofences added")
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
                showToast(this@MainActivity, "fail to add Geofences")
            }
        }
    }

    //    private lateinit var receiver : GeofenceBroadcastReceiver


    @SuppressLint("MissingPermission")
    fun click(v: View) {
        if (this.movementType == 0) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    showToast(this, "${location?.distanceTo(this.location)}")
                    showToast(this, "${location?.latitude} , ${location?.longitude}")
                }

            sendNotification(this, 3)
        } else {
            ringtone?.stop()
        }
    }

//
//    override fun onStop() {
//        unregisterReceiver(receiver)
//        super.onStop()
//    }

}