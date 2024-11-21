package com.example.personalhealthtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.personalhealthtracker.databinding.ActivityDivingBinding

class DivingActivity : AppCompatActivity() {

    // Create a nullable variable for your activity binding
    private var _binding: ActivityDivingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the correct layout binding for DivingActivity
        _binding = ActivityDivingBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Set the root view using the binding

        // Here you can use 'binding' to refer to views in your activity
        // Example:
        // binding.someTextView.text = "Diving Activity Started"

    }

    override fun onDestroy() {
        super.onDestroy()
        // Avoid memory leaks by setting binding to null on destroy
        _binding = null
    }
}