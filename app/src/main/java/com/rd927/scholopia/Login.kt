package com.rd927.scholopia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the UserDao
        val db = AppDatabase.getDatabase(this)
        userDao = db.userDao()

        findViewById<TextView>(R.id.registerNav).setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val user = userDao.getUserByEmail(username)

                    if (user != null) {
                        if (user.password == password) {
                            // Login successful
                            // In Login activity after successful login
                            val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            with (sharedPref.edit()) {
                                putInt("user_id", user.id) // or other relevant user details
                                putString("user_email", user.email)
                                apply()
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@Login, Home::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            // Incorrect password
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(this@Login, "Invalid username or password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // User not found
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(this@Login, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
