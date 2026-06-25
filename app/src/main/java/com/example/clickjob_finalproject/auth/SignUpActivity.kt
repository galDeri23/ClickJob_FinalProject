package com.example.clickjob_finalproject.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.clickjob_finalproject.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnNext).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString()
            val confirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword).text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "הסיסמא חייבת להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createAccount(email, password)
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Account created → go to registration details
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                val message = when {
                    exception.message?.contains("email address is already in use") == true ->
                        "כתובת המייל כבר קיימת במערכת"
                    exception.message?.contains("badly formatted") == true ->
                        "כתובת המייל אינה תקינה"
                    else -> "שגיאה ביצירת החשבון, נסי שוב"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
    }
}