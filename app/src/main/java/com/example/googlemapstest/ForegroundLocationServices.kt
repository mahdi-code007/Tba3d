package com.example.googlemapstest


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.googlemapstest.MainActivity.Companion.TAG
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import javax.annotation.Nullable


class ForegroundLocationServices : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var firebase: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var myLatitude: String = "0.0"
    private var myLongitude: String = "0.0"
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var manager: NotificationManager
    private var listOfDistance = arrayListOf<Float>()

    override fun onCreate() {
        super.onCreate()
        firebase = FirebaseFirestore.getInstance()
        auth = Firebase.auth
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.i(TAG, "onTaskRemoved: ")
        super.onTaskRemoved(rootIntent)
        Log.i(TAG, "onTaskRemoved: ")
//        firebase.collection("Locations")
//            .document(auth.currentUser!!.uid)
//            .delete().addOnSuccessListener {
//                Log.i(TAG, "onTaskRemoved  delete()")
//                stopForeground(true)
//                stopSelf()
//            }
    }

    private fun getDistanceBetweenTwoPoints(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val distance = FloatArray(2)
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance)
        return distance[0]
    }

    private fun getLocations() {
        firebase.collection("Locations")
            .addSnapshotListener() { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (querySnapshot != null) {
                    for (locations in querySnapshot.documents) {

                        val locationsLoaded = locations.toObject<Locations>()

                        if (!(locationsLoaded?.Uid.equals(auth.currentUser?.uid))) {

                            Log.i(TAG, "auth ${auth.currentUser?.uid}")

                            val distance = getDistanceBetweenTwoPoints(
                                myLatitude.toDouble(),
                                myLongitude.toDouble(),
                                locationsLoaded?.latitude!!.toDouble(),
                                locationsLoaded.longitude!!.toDouble()
                            )
                            listOfDistance.add(distance)

                            Log.i(TAG, "listOfDistance : ${listOfDistance.size} ")
                            if (getMinDistance() <= 20.0) {
                                var min = getMinDistance()
                                notificationBuilder.setContentText(
                                    "${String.format("%.2f", min)} متر"
                                )
                                manager.notify(1, notificationBuilder.build())
                                val notification: Uri =
                                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                                val r = RingtoneManager.getRingtone(
                                    applicationContext,
                                    notification
                                )
                                r.play()
                            }

                            // Vibrate for 500 milliseconds
                            // Vibrate for 500 milliseconds
                            //                                ContextCompat.getSystemService(this, Context.VIBRATOR_SERVICE).vibrate(500)

                            Log.i(TAG, distance.toString())
                            Log.i(TAG, "getLocations: ${locationsLoaded?.latitude.toString()}")
                            Log.i(
                                TAG, "getLocations: ${locationsLoaded?.longitude.toString()}"
                            )
                        }

                    }
                }
            }
    }

    private fun getMinDistance(): Float {
        var min = listOfDistance[0]
        for (i in 0 until listOfDistance.size) {
            if (listOfDistance[i] < min) {
                min = listOfDistance[i]
            }
        }
        return min
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()

        notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.maps_sv_error_icon)
                .setContentTitle("تتبع المسافة الامنة يعمل الان")
                .setContentText("0.0 متر")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setAutoCancel(false)
                .setOngoing(true)

        startForeground(1, notificationBuilder.build())

        startLocationTracking()

        getLocations()
        Log.i(TAG, "onStartCommand")
        return START_NOT_STICKY
    }

    private fun startLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            if (p0 == null) {
                Log.i(TAG, "null")
                return
            }

            for (result in p0.locations) {

                myLatitude = result.latitude.toString()
                myLongitude = result.longitude.toString()
                Log.d(TAG, "onLocationResult: last known location is ${p0.lastLocation}")
                val location = hashMapOf(
                    "latitude" to result.latitude.toString(),
                    "longitude" to result.longitude.toString(),
                    "Uid" to auth.currentUser!!.uid.toString()
                )
                firebase.collection("Locations")
                    .document(auth.currentUser!!.uid)
                    .set(location)

            }
        }
    }


    override fun onDestroy() {
//        firebase.collection("Locations")
//            .document(auth.currentUser!!.uid)
//            .delete().addOnSuccessListener {
//                Log.i(TAG, "onDestroy Service: delete()")
//
//            }
        super.onDestroy()


    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }
}