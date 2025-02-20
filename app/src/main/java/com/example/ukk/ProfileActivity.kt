package com.example.ukk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)

        val sharedPref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "Username tidak ditemukan")
        val email = sharedPref.getString("email", "Email tidak ditemukan")


        tvUsername.text = username
        tvEmail.text = email


        imgProfile.setOnClickListener {
            openImageChooser()
        }

        btnLogout.setOnClickListener {

            sharedPref.edit().clear().apply()


            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            if (imageUri != null) {
                val imgProfile = findViewById<ImageView>(R.id.imgProfile)
                imgProfile.setImageURI(imageUri)
                Toast.makeText(this, "Foto profil telah diperbarui", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
