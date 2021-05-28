package com.example.googlemapstest

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.googlemapstest.databinding.FragmentBlankBinding
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task


class BlankFragment : Fragment() {

    companion object {
        const val LOCATION_RC = 1001
        const val TAG = "LocationTest"
    }

    private lateinit var binding: FragmentBlankBinding
    private lateinit var navController: NavController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlankBinding.inflate(inflater)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest.create()
        locationRequest.interval = 4000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

//        val distance = getDistanceBetweenTwoPoints(
//                30.74963437472079,
//                31.806047069266775,
//                30.749635527278947,
//                31.8060618214158
//        )
//        Log.i(TAG, distance.toString())
//        Toast.makeText(requireContext(), distance.toString(), Toast.LENGTH_LONG).show()

        binding.button.setOnClickListener() {
            requestPermissions()

        }

        return binding.root
    }

    private fun getDistanceBetweenTwoPoints(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
    ): Float {
        val distance = FloatArray(2)
        Location.distanceBetween(
                lat1, lon1,
                lat2, lon2, distance
        )
        return distance[0]
    }

    private fun checkSettingsAndStartLocationUpdates() {

        Toast.makeText(requireContext(), "checkSettingsAndStartLocationUpdates", Toast.LENGTH_LONG).show()

        val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()

        val settingsClient: SettingsClient = LocationServices.getSettingsClient(requireActivity())

        val locationSettingsResponseTask: Task<LocationSettingsResponse> =
                settingsClient.checkLocationSettings(locationSettingsRequest)

        locationSettingsResponseTask.addOnSuccessListener {
            Toast.makeText(requireContext(), "startLocationUpdate", Toast.LENGTH_LONG).show()
            Log.i(TAG, "startLocationUpdate")
            startLocationUpdate()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()
            Log.i(TAG, it.message.toString())
        }
    }

    private fun startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
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
        Toast.makeText(requireContext(), "startLocationUpdate 2", Toast.LENGTH_LONG).show()

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
                Log.i(TAG, result.toString())
                Log.i(TAG, result.latitude.toString() + result.longitude.toString())
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    try {
                        val geocoder = Geocoder(requireContext())
                        val currentLocation = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                        )
                        Log.i(TAG, location.latitude.toString())
                        Log.i(TAG, location.longitude.toString())
                        Log.i(TAG, currentLocation.toString())
                    } catch (e: Exception) {
                        Log.d("location", e.localizedMessage)
                    }


                }.addOnFailureListener() {

                }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

    }


    private fun hasAccessFindLocationPermission() =
            ActivityCompat.checkSelfPermission(
                    requireContext(),
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
            requestPermissions(
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
            Toast.makeText(requireContext(), "Permission Granted.", Toast.LENGTH_SHORT).show()
            checkSettingsAndStartLocationUpdates()
        } else {
            requestPermissions()
        }
    }
}

//    private fun hasAccessForegroundLocationPermission() =
//            ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//
//    private fun hasAccessBackgroundLocationPermission() =
//            ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED