package com.example.googlemapsapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.example.googlemapsapp.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var latitud: EditText
    private lateinit var longitud: EditText
    private lateinit var searchButton: Button

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latitud = findViewById(R.id.lat_input)
        longitud = findViewById(R.id.lng_input)
        searchButton = findViewById(R.id.search_button)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchButton.setOnClickListener {
            val lat = latitud.text.toString().toDoubleOrNull()
            val long = longitud.text.toString().toDoubleOrNull()

            if (lat != null && long != null) {
                moveMapToLocation(lat, long)
            } else {
                Toast.makeText(this, "Por favor, ingrese latitud y longitud válidas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        enableUserLocation()
    }

    /**
     * Mover el mapa a una ubicación específica usando latitud y longitud
     */
    private fun moveMapToLocation(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(location).title("Nueva ubicación"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f), 4000, null)
    }

    /**
     * Habilitar la ubicación del usuario y obtener la última ubicación conocida
     */
    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos de ubicación
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f), 4000, null)

                if (userMarker == null) {
                    userMarker = mMap.addMarker(MarkerOptions().position(userLatLng).title("Mi ubicación"))
                } else {
                    userMarker?.position = userLatLng
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableUserLocation()
                } else {
                    Snackbar.make(findViewById(R.id.map), "Permiso de ubicación denegado", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
