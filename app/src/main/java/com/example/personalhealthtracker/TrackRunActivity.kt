package com.example.personalhealthtracker

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.personalhealthtracker.databinding.ActivityTrackRunBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrackRunActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityTrackRunBinding
    private var myMap: GoogleMap? = null

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private var currentLocation: LatLng? = null
    private var prevLocation: LatLng? = null
    private var totalDistance = 0.0
    private var totalEnergyConsumption = 0.0
    private var totalSteps = 0
    private var stopTimer: Long = 0
    private var averageSpeed = 0.0
    private var elapsedSecond = 0
    private var formattedCalories = "0"
    private var formattedDistance = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission", "SetTextI18n", "CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize view binding
        binding = ActivityTrackRunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initial UI setup
        binding.totalDistance.text = "0 km"
        binding.averagePace.text = "0"
        binding.energyConsump.text = "0"
        binding.totalStepNum.text = "0"

        // Obtain the SupportMapFragment and set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Hide BottomNavigationView (if applicable)
        val navigationBar = findViewById<BottomNavigationView>(R.id.bottomNavigationView2)
        navigationBar?.visibility = View.GONE

        // Setup button actions for controlling the timer
        setupChronometer()

        supportActionBar?.hide()  // Hide the action bar

        // Request location permissions if not granted
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(ACCESS_FINE_LOCATION), 1
            )
        }
    }

    // Handle chronometer start/stop logic
    private fun setupChronometer() {
        binding.buttonToggleStart.setOnClickListener {
            binding.tvTimer.base = SystemClock.elapsedRealtime() + stopTimer
            binding.tvTimer.start()
            binding.buttonToggleStart.visibility = View.GONE
            binding.btnFinishRun.visibility = View.VISIBLE
            binding.btnToggleStop.visibility = View.VISIBLE
        }

        binding.btnToggleStop.setOnClickListener {
            stopTimer = binding.tvTimer.base - SystemClock.elapsedRealtime()
            binding.tvTimer.stop()
            binding.buttonToggleStart.visibility = View.VISIBLE
            binding.btnFinishRun.visibility = View.GONE
            binding.btnToggleStop.visibility = View.GONE
        }

        binding.btnFinishRun.setOnClickListener {
            // Save activity data to SharedPreferences and navigate to result screen
            saveActivityData()
            val intent = Intent(this, AddActivitiesAndShowToUser::class.java)
            intent.putExtra("sourceActivity", "Running Activity")
            startActivity(intent)
            finish()
        }
    }

    // Save activity data to SharedPreferences
    private fun saveActivityData() {
        val sharedPreferences = getSharedPreferences("Bilgiler", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("activityType", "Running Activity")
        editor.putString("roadTravelled", formattedDistance)
        editor.putString("timeElapsed", elapsedSecond.toString())
        editor.putString("caloriesBurned", formattedCalories)
        editor.apply()
    }

    // Map setup and location updates handling
    @SuppressLint("SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Location listener logic
        locationListener = object : LocationListener {
            @SuppressLint("MissingPermission")
            override fun onLocationChanged(location: Location) {
                myMap?.clear()

                currentLocation = LatLng(location.latitude, location.longitude)
                Log.d("TrackRunActivity", "Location: $currentLocation")

                // Update map with the current location
                currentLocation?.let {
                    myMap?.addMarker(MarkerOptions().position(it).title("Current Location"))
                    myMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }

                val distance = if (prevLocation != null) {
                    calculateDistance(prevLocation!!.latitude, prevLocation!!.longitude,
                        currentLocation!!.latitude, currentLocation!!.longitude)
                } else 0.0

                // Update previous location for next comparison
                prevLocation = currentLocation

                // Total Distance Calculation
                totalDistance += distance
                formattedDistance = DecimalFormat("#.###").format(totalDistance)

                // Step Calculation (customized - you might need a separate pedometer integration)
                totalSteps = (totalDistance / 0.8).toInt()

                // Timer handling
                elapsedSecond = ((SystemClock.elapsedRealtime() - binding.tvTimer.base) / 1000).toInt()

                // Calories burned calculation
                totalEnergyConsumption += calculateCaloriesBurned(totalSteps, elapsedSecond, 25)
                formattedCalories = DecimalFormat("#.###").format(totalEnergyConsumption)

                // Average speed calculation
                averageSpeed = calculateAverageSpeed(elapsedSecond)

                // Update the UI with the calculated info
                updateUI()
            }

            private fun updateUI() {
                binding.totalDistance.text = "$formattedDistance km"
                binding.averagePace.text = averageSpeed.toString()
                binding.energyConsump.text = formattedCalories
                binding.totalStepNum.text = totalSteps.toString()
            }

            override fun onProviderDisabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    showEnableGPSDialog()
                }
            }
        }

        // Request location updates if permission was granted
        if (ContextCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1f,
                locationListener)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), 1)
        }
    }

    // Show dialog to enable GPS if disabled
    private fun showEnableGPSDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("GPS is Disabled")
        builder.setMessage("Please enable GPS to use this feature.")
        builder.setPositiveButton("Enable GPS") { _, _ ->
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun calculateAverageSpeed(elapsedTime: Int): Double {
        // Average speed in meters per minute (distance in meters divided by time in minutes)
        return if (elapsedTime > 0) totalDistance / (elapsedTime / 60.0) else 0.0
    }

    private fun calculateCaloriesBurned(stepCount: Int, elapsedTime: Int, age: Int): Double {
        val averageStepLength = 0.8 // meters
        val walkingSpeed = (stepCount * averageStepLength) / elapsedTime // meters per second
        val caloriesPerMinute = calculateCaloriesPerMinute(walkingSpeed, age)
        return caloriesPerMinute * (elapsedTime / 60.0)
    }

    private fun calculateCaloriesPerMinute(walkingSpeed: Double, age: Int): Double {
        val MET = calculateMET(walkingSpeed) // Metabolic Equivalent (MET)
        val basalMetabolicRate = calculateBasalMetabolicRate(age) // BMR
        return MET * basalMetabolicRate / 24 / 60 // calories per minute
    }

    private fun calculateMET(walkingSpeed: Double): Double {
        // Return MET based on walking speed (meters per second)
        return when {
            walkingSpeed < 0.9 -> 2.0
            walkingSpeed < 1.3 -> 2.5
            walkingSpeed < 1.8 -> 3.0
            walkingSpeed < 2.0 -> 4.0
            else -> 5.0
        }
    }

    private fun calculateBasalMetabolicRate(age: Int): Double {
        // Basal Metabolic Rate (BMR) estimated for males
        return when (age) {
            in 0..9 -> 1000.0
            in 10..17 -> 2000.0
            in 18..49 -> 2600.0
            else -> 2200.0
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371 * 1000 // Distance in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // returns distance in meters
    }

    // Request location provider updates after permission is granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1f, locationListener)
            }
        } else {
            showPermissionDeniedMessage()
        }
    }

    private fun showPermissionDeniedMessage() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Denied")
        builder.setMessage("Location permission is required to track your running.")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}