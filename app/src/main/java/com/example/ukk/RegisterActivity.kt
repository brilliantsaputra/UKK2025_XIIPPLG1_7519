package com.example.ukk

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukk.DatabaseHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)



        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etName = findViewById<EditText>(R.id.etName)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val name = etName.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val sharedPref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("username", username)
            editor.putString("email", email)
            editor.apply()

            try {
                dbHelper.addUser(username, password, email, name)
                Toast.makeText(this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }
}