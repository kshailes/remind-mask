package com.example.coronasafety

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat


const val THRESHOLD = 1000 * 60 * 5L
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    Log.d("Logger", message)
}

//fun sendNotification(context: Context, movementType: Int) {
//    val intent = Intent(context, MainActivity::class.java).apply {
//        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//    }
//    var message = "default"
//    intent.putExtra("flag", movementType)
//    if (movementType == 1) {
//        message = "Andar aaya"
//    } else if (movementType == 2) {
//        message = "Bahar gaya"
//    } else if (movementType == 4) {
//        message = "Walking"
//    }
//    val pIntent = PendingIntent.getActivity(
//        context,
//        System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT
//    )
//
//    createNotificationChannel(context)
//
//    var builder = NotificationCompat.Builder(context, "CHANNEL_ID")
//        .setSmallIcon(R.drawable.ic_baseline_check_circle_24)
//        .setContentTitle(message)
//        .setContentText("Check what you must do before proceeding")
//        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//        .setContentIntent(pIntent)
//        .setVibrate(longArrayOf(1000, 1000))
//        .setLights(Color.RED, 3000, 3000)
//        .setAutoCancel(true)
//
////    with(NotificationManagerCompat.from(context)) {
////        // notificationId is a unique int for each notification that you must define
////        notify(0, builder.build())
////    }
//
//    val notificationManager =
//        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
//    notificationManager!!.notify(1, builder.build())
//}

fun sendNotification(context: Context, movementType: Int) {
    if (movementType == 3) {
        showNotification(context, movementType)
        return
    }
    val lastTime = getPreference(context).getLong(LAST_NOTIFIED_TIME)
    val currentTime = System.currentTimeMillis()

    if (lastTime != -1L) {
        if (currentTime - lastTime > THRESHOLD) {
            getPreference(context).putLong(LAST_NOTIFIED_TIME, currentTime)
            showNotification(context, movementType)
        }
    } else {
        getPreference(context).putLong(LAST_NOTIFIED_TIME, currentTime)
        showNotification(context, movementType)
    }
}

fun showNotification(context: Context, movementType: Int) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    intent.putExtra("flag", movementType)
    startActivity(context, intent, null)
    startVibrate(context, -1)
}

fun createNotificationChannel(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun checkLocationPermission(activity: AppCompatActivity, requestCode: Int) {
    when {
        (ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) -> {
            // You can use the API that requires the permission.
        }
        shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
        }
        else -> {
            // You can directly ask for the permission.
            askForLocationPermission(activity, requestCode)
        }
    }
}

fun askForLocationPermission(activity: AppCompatActivity, requestCode: Int) {
    requestPermissions(
        activity,
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ),
        requestCode
    )
}

fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(locationManager)
}

fun startVibrate(context: Context, repeat: Int) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val dot = 200 // Length of a Morse Code "dot" in milliseconds
    val dash = 500 // Length of a Morse Code "dash" in milliseconds
    val short_gap = 200 // Length of Gap Between dots/dashes
    val medium_gap = 500 // Length of Gap Between Letters
    val long_gap = 1000 // Length of Gap Between Words
    val pattern = longArrayOf(
        0,  // Start immediately
        dot.toLong(),
        short_gap.toLong(),
        dot.toLong(),
        short_gap.toLong(),
        dot.toLong(),
        medium_gap.toLong(),  // S
        dash.toLong(),
        short_gap.toLong(),
        dash.toLong(),
        short_gap.toLong(),
        dash.toLong(),
        medium_gap.toLong(),  // O
        dot.toLong(),
        short_gap.toLong(),
        dot.toLong(),
        short_gap.toLong(),
        dot.toLong(),
        long_gap // S
            .toLong()
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
    } else {
        vibrator.vibrate(pattern, repeat)
    }
}

fun startRingTone(context: Context): Ringtone {
    val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    val r = RingtoneManager.getRingtone(
        context,
        notification
    )
    r.play()
    return r
}

fun stopRingTone(ringtone: Ringtone) {
    ringtone.stop()
}

fun getPreference(context: Context): Preference {
    return Preference(context)
}

fun buildAlertMessage(
    context: Context, message: String, positiveCallback: DialogInterface.OnClickListener,
    negativeCallback: DialogInterface.OnClickListener
) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    builder.setMessage(message)
        .setCancelable(false)
        .setPositiveButton(
            "Yes"
        ) { dialog, id -> positiveCallback.onClick(dialog, id) }
        .setNegativeButton(
            "No"
        ) { dialog, id ->
            negativeCallback.onClick(dialog, id)
        }
    val alert: AlertDialog = builder.create()
    alert.show()
}