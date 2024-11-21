package com.example.personalhealthtracker

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.example.personalhealthtracker.databinding.ActivityBreathTakingExerciseBinding

class BreathTakingExerciseActivity : AppCompatActivity() {

    // Declare binding as a nullable variable and within the class scope
    private var _binding: ActivityBreathTakingExerciseBinding? = null
    private val binding get() = _binding!!

    private val actType = "Breath Taking Activity"
    private var actDuration = "30"
    private var breathTimer: CountDownTimer? = null  // Optional: To cancel or manage your timer properly

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding
        _binding = ActivityBreathTakingExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start breathing sequence
        binding.buttonStartInBreathTaking.setOnClickListener {
            startBreathing()
            binding.buttonStartInBreathTaking.visibility = View.GONE
            binding.buttonStopInBreathTaking.visibility = View.VISIBLE
        }

        // Handle the stop button behavior
        binding.buttonStopInBreathTaking.setOnClickListener {
            breathTimer?.cancel()  // Cancel the ongoing timer
            binding.buttonStartInBreathTaking.visibility = View.VISIBLE
            binding.buttonStopInBreathTaking.visibility = View.GONE
            resetBreathingSession() // Reset UI state
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startBreathing() {
        var count = 30
        val time = 30000  // Total time: 30 seconds

        // Initialize a new CountDownTimer for the breathing sequence
        breathTimer = object : CountDownTimer(time.toLong(), 1000) {
            override fun onTick(millisTillFinished: Long) {
                when {
                    count > 20 -> {
                        // First 10 seconds -> Take
                        binding.headerOfBreathTaking.text = "TAKE: ${(millisTillFinished - 20000) / 1000}"
                        count--
                    }
                    count > 10 -> {
                        // Next 10 seconds -> Hold
                        binding.headerOfBreathTaking.text = "HOLD: ${(millisTillFinished - 10000) / 1000}"
                        count--
                    }
                    count > 0 -> {
                        // Final 10 seconds -> Release
                        binding.headerOfBreathTaking.text = "RELEASE: ${millisTillFinished / 1000}"
                        count--
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                // Once the timer finishes
                binding.headerOfBreathTaking.text = "YOU DID A GREAT JOB!"
                binding.buttonStartInBreathTaking.visibility = View.GONE
                binding.buttonStopInBreathTaking.visibility = View.GONE
                binding.buttonFinish.visibility = View.VISIBLE

                // Handle the finish button click
                binding.buttonFinish.setOnClickListener {
                    startActivity(Intent(this@BreathTakingExerciseActivity, LoginActivity::class.java))
                }
            }
        }.start()
    }

    // Reset the breathing session to its initial state
    private fun resetBreathingSession() {
        binding.headerOfBreathTaking.text = "Ready to start breathing exercise?"
    }

    // Properly clear resources when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        breathTimer?.cancel()  // Cancel the timer if the activity is destroyed
        _binding = null         // Avoid memory leaks by clearing the binding reference
    }
}