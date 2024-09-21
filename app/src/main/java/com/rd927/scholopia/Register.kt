package com.rd927.scholopia

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.loginNav).setOnClickListener {
            // Start the LoginActivity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        val emailEditText = findViewById<EditText>(R.id.email)
        val usernameEditText = findViewById<EditText>(R.id.username)
        val nameEditText = findViewById<EditText>(R.id.name)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirm_password)
        val dobTextView = findViewById<TextView>(R.id.dob)
        val registerButton = findViewById<Button>(R.id.register_button)

        dobTextView.setOnClickListener {
            showDatePicker()
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val dob = dobTextView.text.toString().trim()

            if (email.isNotEmpty() && username.isNotEmpty() && name.isNotEmpty() && password.isNotEmpty() && dob.isNotEmpty()) {
                if (password == confirmPassword) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        registerUser(email, password, username, name, dob)
                    }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            findViewById<TextView>(R.id.dob).text = date
        }, year, month, day)

        datePickerDialog.show()
    }

    private suspend fun registerUser(email: String, password: String, username: String, name: String, dob: String) {
        // Create a User object
        val user = User(
            email = email,
            username = username,
            name = name,
            password = password,
            dob = dob
        )
        // Access the database and DAO
        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        try {
            // Insert the User object into the database
            userDao.insertUser(user)
            withContext(Dispatchers.Main) {
                // Registration successful
                Toast.makeText(this@Register, "Registration successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Register, Home::class.java))
                finish()
            }
        } catch (e: Exception) {
            // Handle any errors that occurred during insertion
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Register, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
