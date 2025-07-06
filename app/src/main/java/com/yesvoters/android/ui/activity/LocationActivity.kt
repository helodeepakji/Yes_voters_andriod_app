package com.yesvoters.android.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yesvoters.android.R
import com.yesvoters.android.database.AppSharedPreferences
import com.yesvoters.android.databinding.ActivityLocationBinding
import com.yesvoters.android.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
class LocationActivity : BaseActivity() {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnAllowGPSLocation.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkIfLocationEnabledAndProceed()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showToast(getString(R.string.location_permission_required))
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun checkIfLocationEnabledAndProceed() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (isEnabled) {
            getLastKnownLocation()
        } else {
            showToast(getString(R.string.enable_location_services))
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    AppSharedPreferences.setLatitude(latitude.toString())
                    AppSharedPreferences.setLongitude(longitude.toString())

                    getAddressFromLocation(latitude, longitude)
                } else {
                    showToast(
                        getString(R.string.location_unavailable)
                    )
                    loadNextScreen()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.location_fetch_failed), Toast.LENGTH_SHORT)
                    .show()
                loadNextScreen()
            }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())

            val addresses: List<Address> = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1)
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                } ?: emptyList()
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }

            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressText = """
                    Address: ${address.getAddressLine(0)}
                    City: ${address.locality ?: "N/A"}
                    State: ${address.adminArea ?: "N/A"}
                    Country: ${address.countryName ?: "N/A"}
                    Postal Code: ${address.postalCode ?: "N/A"}
                """.trimIndent()

                showToast(addressText)
            } else {
                showToast(getString(R.string.no_address_found))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showToast(getString(R.string.address_retrieval_failed))
        } finally {
            loadNextScreen()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkIfLocationEnabledAndProceed()
            } else {
                val permanentlyDenied = !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (permanentlyDenied) {
                    showToast(getString(R.string.location_permission_permanently_denied))
                    openAppSettings()
                } else {
                    showToast(getString(R.string.location_permission_denied))
                    finish()
                }
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    private fun loadNextScreen() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
