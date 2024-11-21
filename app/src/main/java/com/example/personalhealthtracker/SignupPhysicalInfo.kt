package com.example.personalhealthtracker

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.personalhealthtracker.databinding.FragmentSignupPhysicalInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupPhysicalInfo : Fragment() {

    private var _binding: FragmentSignupPhysicalInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth

    private val db = Firebase.firestore

    private lateinit var gender: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupPhysicalInfoBinding.inflate(inflater, container, false)
        val view: View = binding.root
        mAuth = FirebaseAuth.getInstance()

        progressOfAge()
        progressOfHeight()
        progressOfWeight()
        progressOfGender()

        binding.nextButton.setOnClickListener {
            // Handle Arguments passed from previous fragment
            arguments?.let {
                val email = SignupPhysicalInfoArgs.fromBundle(it).email
                val password = SignupPhysicalInfoArgs.fromBundle(it).password
                val username = SignupPhysicalInfoArgs.fromBundle(it).username
                val userHeight = binding.heightResult.text.toString()
                val userWeight = binding.weightResult.text.toString()
                val userAge = binding.ageResult.text.toString()
                val userGender = gender
                val healthyActivities = ArrayList<Any>()

                // Create user details Map to push to Firestore
                val userMap = hashMapOf<String, Any>()
                userMap["userEmail"] = email
                userMap["username"] = username
                userMap["userHeight"] = userHeight
                userMap["userWeight"] = userWeight
                userMap["userAge"] = userAge
                userMap["userGender"] = userGender
                userMap["healthyActivities"] = healthyActivities

                // Validate if all the fields are filled
                if (email == "" || password == "" || username == "" || userAge == "" || userGender == "" || userWeight == "" || userHeight == "") {
                    Toast.makeText(requireContext(), "Please fill in all the fields!", Toast.LENGTH_LONG).show()
                } else {
                    // Firebase createUserWithEmailAndPassword for signup process
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                userMap["userId"] = userId.toString() // Add userId to the map

                                // Store user data in Firestore under `user` collection
                                userId?.let {
                                    db.collection("user").document(it).set(userMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), "Signup Successful!", Toast.LENGTH_LONG).show()
                                            // Navigate to login screen on success
                                            try {
                                                Navigation.findNavController(view).navigate(R.id.navigateTo_signupPhysicalInfo_to_loginFragment)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Toast.makeText(requireContext(), "Navigation failure.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(requireContext(), "Signup Failed: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                val errorMessage = task.exception?.message
                                Toast.makeText(requireContext(), "Signup Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        // Handle prevButton to navigate back
        binding.prevButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.navigateTo_signupPhysicalInfo_to_signupUserInfo)
        }

        return view
    }

    /*** Gender Selection Handling ***/
    private fun progressOfGender() {
        val genderGroup = binding.gender
        genderGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.maleButton -> {
                    gender = "male"
                }
                R.id.femaleButton -> {
                    gender = "female"
                }
                R.id.notToSayButton -> {
                    gender = "not say"
                }
            }
        }
    }

    /*** Age Progress Handling ***/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun progressOfAge() {
        val ageSeekBar = binding.volumeSeekBar
        ageSeekBar.min = 18
        ageSeekBar.max = 100

        ageSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.ageResult.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    /*** Height Progress Handling ***/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun progressOfHeight() {
        val heightSeekBar = binding.heightSeekBar
        heightSeekBar.min = 150
        heightSeekBar.max = 210

        heightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.heightResult.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    /*** Weight Progress Handling ***/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun progressOfWeight() {
        val weightSeekBar = binding.weightSeekBar
        weightSeekBar.min = 50
        weightSeekBar.max = 170

        weightSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.weightResult.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    // Clean up binding object to avoid memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}