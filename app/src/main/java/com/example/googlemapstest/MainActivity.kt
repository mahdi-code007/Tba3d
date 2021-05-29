package com.example.googlemapstest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import com.example.googlemapstest.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    companion object {
        const val LOCATION_RC = 1001
        const val TAG = "LocationTest"
    }

    private lateinit var navController: NavController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var firebase: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    private var myLatitude : String = "0.0"
    private var myLongitude : String = "0.0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        firebase = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        signInAnonymously()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        getLocations()


        val serviceIntent = Intent(this, ForegroundLocationServices::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun getDistanceBetweenTwoPoints(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val distance = FloatArray(2)
        Location.distanceBetween(lat1, lon1,
                lat2, lon2, distance)
        return distance[0]
    }

    private fun getLocations() {
        firebase.collection("Locations")
//                .whereNotEqualTo("Uid" , auth.currentUser?.uid)
                .addSnapshotListener() { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                    if (querySnapshot != null) {
                        for (locations  in querySnapshot.documents) {

                            val locationsLoaded = locations.toObject<Locations>()

                            val distance = getDistanceBetweenTwoPoints(myLatitude.toDouble(),
                                    myLongitude.toDouble(),
                                    locationsLoaded?.latitude!!.toDouble(),
                                    locationsLoaded?.longitude!!.toDouble()
                            )

                            binding.tvDistance.text = distance.toString() + " M"
//                            if (distance <= 5.0F){
//                                binding.tvDistance.text = distance.toString() + " M"
//                            }
                            Log.i(TAG, distance.toString())
                            Log.i(TAG, "getLocations: ${locationsLoaded?.latitude.toString()}")
                            Log.i(TAG, "getLocations: ${locationsLoaded?.longitude.toString()}")

                        }
                    }
                }
    }





    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            if (p0 == null) {
                Log.i(TAG, "null")
                return
            }
            for (result in p0.locations) {
                Log.i(TAG, result.latitude.toString() + result.longitude.toString())

                binding.tvLatitude.text = result.latitude.toString()
                binding.tvLongitude.text = result.longitude.toString()

                myLatitude = result.latitude.toString()
                myLongitude = result.longitude.toString()

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

    private fun hasAccessFindLocationPermission() =
            ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        var permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasAccessFindLocationPermission()) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toTypedArray(),
                    LOCATION_RC
            )
        }
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_RC && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "${permissions[i]} granted.")
                }

            }
            checkSettingsAndStartLocationUpdates()
        }
    }


    private fun checkSettingsAndStartLocationUpdates() {

        Toast.makeText(this, "checkSettingsAndStartLocationUpdates", Toast.LENGTH_LONG).show()

        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(this)

        val locationSettingsResponseTask: Task<LocationSettingsResponse> =
                settingsClient.checkLocationSettings(locationSettingsRequest)

        locationSettingsResponseTask.addOnSuccessListener {
            Toast.makeText(this, "startLocationUpdate", Toast.LENGTH_LONG).show()
            Log.i(BlankFragment.TAG, "startLocationUpdate")
            startLocationUpdate()
        }.addOnFailureListener {
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_LONG).show()
            Log.i(BlankFragment.TAG, it.message.toString())
        }
    }

    private fun startLocationUpdate() {
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

        Log.i(TAG, "startLocationUpdate 2")
        Toast.makeText(this, "startLocationUpdate 2", Toast.LENGTH_LONG).show()

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        )
    }

    private fun signInAnonymously() {
        auth.signInAnonymously().addOnSuccessListener {
            Toast.makeText(this, "User Created", Toast.LENGTH_LONG).show()
            Log.i(TAG, "User Created")
        }.addOnFailureListener {
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            Log.i(TAG, "User Created")
        }
    }

    private fun stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdate()
    }

    override fun onStart() {
        super.onStart()
        if (hasAccessFindLocationPermission()) {
            Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show()
            checkSettingsAndStartLocationUpdates()
        } else {
            requestPermissions()
        }
    }

}
