package com.example.coronasafety

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        showToast(context!!, "receiver aaya")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("BroadcastReceiver", errorMessage)
            showToast(context!!, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER  ) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                this,
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            sendNotification(context , 1)
//            Toast.makeText(context, "bahar aa gya", Toast.LENGTH_LONG).show()
            showToast(context!!, "Andar aaya")
            Log.i("BroadcastReceiver", geofenceTransitionDetails)
        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                this,
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            sendNotification(context , 2)
//            Toast.makeText(context, "bahar aa gya", Toast.LENGTH_LONG).show()
            showToast(context!!, "bahar aa gya")
            Log.i("BroadcastReceiver", geofenceTransitionDetails)
        }
        else {
            // Log the error.
//            Log.e("BroadcastReceiver", getString(R.string.geofence_transition_invalid_type,
//                geofenceTransition))
            Log.e("BroadcastReceiver", "error2")
            showToast(context!!, "error2")
        }

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            for (event in result.transitionEvents) {
                // chronological sequence of events....
                showToast(context , "$event ${event.transitionType}")
                sendNotification(context , 4)
            }
        }
    }



    private fun getGeofenceTransitionDetails(
        geofenceBroadcastReceiver: GeofenceBroadcastReceiver,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        return "acha"
    }
}