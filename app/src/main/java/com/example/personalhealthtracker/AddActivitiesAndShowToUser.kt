package com.example.personalhealthtracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalhealthtracker.databinding.ActivityAddActivitiesAndShowToUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.personalhealthtracker.LoginActivity
import com.google.firebase.Timestamp  // This is the correct Firebase Timestamp.

private lateinit var sharedPreferences: SharedPreferences
private lateinit var sourceActivity: String

private lateinit var activityName: String
private lateinit var kmTravelled: String
private lateinit var energyConsump: String
private lateinit var userEmail: String
private lateinit var dateOfAct: Timestamp  // Correct Firebase Timestamp type.
private lateinit var elapsedTime: String

class AddActivitiesAndShowToUser : AppCompatActivity() {
    private var _binding: ActivityAddActivitiesAndShowToUserBinding? = null
    private val binding get() = _binding!!

    // Access a Cloud Firestore instance from your Activity
    val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAddActivitiesAndShowToUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        sourceActivity = intent.getStringExtra("sourceActivity").toString()

        if (sourceActivity == "Running Activity") {
            sharedPreferences = getSharedPreferences("Bilgiler", Context.MODE_PRIVATE)
            binding.activityType.text = sharedPreferences.getString("activityType", "0")
            binding.kcalView.text = sharedPreferences.getString("caloriesBurned", "0")
            binding.roadTravelledView.text = sharedPreferences.getString("roadTravelled", "0")
            binding.durationView.text = sharedPreferences.getString("timeElapsed", "0")

        } else {
            sharedPreferences = getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
            binding.activityType.text = sharedPreferences.getString("activityType", "0")
            binding.kcalView.text = sharedPreferences.getString("caloriesBurned", "0")
            binding.roadTravelledView.text = sharedPreferences.getString("totalDistance", "0")
            binding.durationView.text = sharedPreferences.getString("step", "0")
            binding.elapsedTimeText.text = "Step"
        }

        supportActionBar?.hide()

        binding.buttonSave.setOnClickListener {
            saveToHistory()
            startActivity(Intent(this@AddActivitiesAndShowToUser, LoginActivity::class.java))
        }

        binding.buttonCancel.setOnClickListener {
            startActivity(Intent(this@AddActivitiesAndShowToUser, LoginActivity::class.java))
        }
    }

    private fun saveToHistory() {
        sourceActivity = intent.getStringExtra("sourceActivity").toString()
        val healthyActMap = hashMapOf<String, Any>()

        if (sourceActivity == "Running Activity") {
            sharedPreferences = getSharedPreferences("Bilgiler", Context.MODE_PRIVATE)
            elapsedTime = sharedPreferences.getString("timeElapsed", "0")!!
            activityName = sharedPreferences.getString("activityType", "0")!!
            kmTravelled = sharedPreferences.getString("roadTravelled", "0")!!
            energyConsump = sharedPreferences.getString("caloriesBurned", "0")!!
        } else {
            sharedPreferences = getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
            elapsedTime = sharedPreferences.getString("step", "0")!!
            activityName = sharedPreferences.getString("activityType", "0")!!
            kmTravelled = sharedPreferences.getString("roadTravelled", "0")!!
            energyConsump = sharedPreferences.getString("caloriesBurned", "0")!!
        }

        userEmail = auth.currentUser!!.email!!.toString()
        dateOfAct = Timestamp.now()  // Use Firebase's Timestamp.

        healthyActMap["energyConsump"] = energyConsump
        healthyActMap["kmTravelled"] = kmTravelled
        healthyActMap["activityName"] = activityName
        healthyActMap["userEmail"] = userEmail
        healthyActMap["dateOfAct"] = dateOfAct
        healthyActMap["elapsedTime"] = elapsedTime

        // Getting the UID of the current user
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("HealthyActivities").add(healthyActMap)
            .addOnSuccessListener { documentReference ->
                val activityId = documentReference.id

                // Update the user's document in "user" collection
                if (userId != null) {
                    db.collection("user")
                        .document(userId)
                        .update("healthyActivities", FieldValue.arrayUnion(activityId))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}