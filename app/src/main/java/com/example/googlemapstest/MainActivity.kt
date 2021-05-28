package com.example.googlemapstest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.googlemapstest.databinding.ActivityMainBinding

import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    companion object {
        const val LOCATION_RC = 1001
        const val TAG = "LocationTest"
    }

    private lateinit var navController: NavController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var firebase : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)

        firebase = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create()
        locationRequest.interval = 4000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        binding.lifecycleOwner = this
//        navController = findNavController(R.id.NavHostFragment)

//        firebase.collection("qqq")
//                .document("test")
//                .set()

        // Create a new user with a first and last name
        val user = hashMapOf(
                "first" to "Ada",
                "last" to "Lovelace",
                "born" to 1815
        )

// Add a new document with a generated ID
        firebase.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }

    }

    private fun hasAccessFindLocationPermission() =
            ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        var permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
//            getLastLocation()
            checkSettingsAndStartLocationUpdates()
        }
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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

    private fun stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            Log.i(TAG, "call back")
            if (p0 == null) {
                Log.i(TAG, "null")
                return
            }
            for (result in p0.locations) {
//                Log.i(TAG, result.toString())

                Log.i(TAG, result.latitude.toString() + result.longitude.toString())

                firebase.collection("Locations")
                        .document("test")
                        .set(result)
            }
        }
    }

}
