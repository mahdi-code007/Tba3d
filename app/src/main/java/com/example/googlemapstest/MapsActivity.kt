package com.example.googlemapstest

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude = 0.0
    private var longitude = 0.0
//    lateinit var searchView: SearchView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
//        searchView = findViewById(R.id.searchView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()


        val distance = getDistanceBetweenTwoPoints(30.74963437472079,
                31.806047069266775,
                30.749635527278947,
                31.8060618214158
        )


        Log.i(BlankFragment.TAG, distance.toString())
        Toast.makeText(this, distance.toString(), Toast.LENGTH_LONG).show()

//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener)

//        searchView.setOnQueryTextListener(object : OnQueryTextListener() {
//            fun onQueryTextSubmit(query: String?): Boolean {
//                // on below line we are getting the
//                // location name from search view.
//                val location: String = searchView.getQuery().toString()
//
//                // below line is to create a list of address
//                // where we will store the list of all address.
//                var addressList: List<Address>? = null
//
//                // checking if the entered location is null or not.
//                if (location != null || location == "") {
//                    // on below line we are creating and initializing a geo coder.
//                    val geocoder = Geocoder(this@MapsActivity)
//                    try {
//                        // on below line we are getting location from the
//                        // location name and adding that location to address list.
//                        addressList = geocoder.getFromLocationName(location, 1)
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                    // on below line we are getting the location
//                    // from our list a first position.
//                    val address = addressList!![0]
//
//                    // on below line we are creating a variable for our location
//                    // where we will add our locations latitude and longitude.
//                    val latLng = LatLng(address.latitude, address.longitude)
//
//                    // on below line we are adding marker to that position.
//                    map.addMarker(MarkerOptions().position(latLng).title(location))
//
//                    // below line is to animate camera to that position.
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
//                }
//                return false
//            }
//
//            fun onQueryTextChange(newText: String?): Boolean {
//                return false
//            }
//        })
//        // at last we calling our map fragment to update.
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        setMapLongClick(map)

        setPoiClick(map)

        setMapStyle(map)



        map.isTrafficEnabled = true
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
        map.isMyLocationEnabled = true
        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        //----------------
//        val homeLatLng = LatLng(30.7494681, 31.8060361)
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 14f))
//        map.addMarker(MarkerOptions().position(homeLatLng))

//        val overlaySize = 100f
//        val androidOverlay = GroundOverlayOptions()
//            .image(BitmapDescriptorFactory.fromResource(R.drawable.image))
//            .position(homeLatLng, overlaySize)
//
//        map.addGroundOverlay(androidOverlay)
    }

    //    private fun setMapLongClick(map: GoogleMap) {
//        map.setOnMapLongClickListener { latLng ->
//            map.addMarker(
//                MarkerOptions()
//                    .position(latLng)
//            )
//        }
//    }
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A snippet is additional text that's displayed after the title.
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    latLng.latitude,
                    latLng.longitude
            )
            map.addMarker(
                    MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.dropped_pin))
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                    MarkerOptions()
                            .position(poi.latLng)
                            .title(poi.name)
            )
            poiMarker.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this,
                            R.raw.map_style
                    )
            )

            if (!success) {
                Log.e("Style", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Style", "Can't find style. Error: ", e)
        }
    }

    private fun getLastLocation() {
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
        fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    try {

                        latitude = location.latitude
                        longitude = location.longitude
                        val geocoder = Geocoder(this)
                        val currentLocation = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                        )
                        val homeLatLng = LatLng(location.latitude, location.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 14f))
                        map.addMarker(MarkerOptions().position(homeLatLng))
                        Log.i(BlankFragment.TAG, location.latitude.toString())
                        Log.i(BlankFragment.TAG, location.longitude.toString())
                        Log.i(BlankFragment.TAG, currentLocation.toString())
                    } catch (e: Exception) {
                        Log.d("location", e.localizedMessage)
                    }


                }.addOnFailureListener() {

                }
    }

    private fun getDistanceBetweenTwoPoints(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val distance = FloatArray(2)
        Location.distanceBetween(lat1, lon1,
                lat2, lon2, distance)
        return distance[0]
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}