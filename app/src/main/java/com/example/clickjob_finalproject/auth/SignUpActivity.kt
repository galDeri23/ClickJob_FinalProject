package com.example.clickjob_finalproject.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.clickjob_finalproject.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var tvReqLength: TextView
    private lateinit var tvReqLetter: TextView
    private lateinit var tvReqDigit: TextView

    private val requirementMet = Color.parseColor("#2E7D32")
    private val requirementUnmet = Color.parseColor("#999999")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        bindViews()
        setupLiveValidation()
        setupClickListeners()
    }

    private fun bindViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        tvReqLength = findViewById(R.id.tvReqLength)
        tvReqLetter = findViewById(R.id.tvReqLetter)
        tvReqDigit = findViewById(R.id.tvReqDigit)
    }

    // Update the requirement list on every keystroke
    private fun setupLiveValidation() {
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateRequirements(s?.toString() ?: "")
                // Re-check the match if the user already typed a confirmation
                if (etConfirmPassword.text?.isNotEmpty() == true) checkPasswordsMatch()
            }
        })

        etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPasswordsMatch()
            }
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                tilEmail.error = null
            }
        })
    }

    private fun updateRequirements(password: String) {
        setRequirement(tvReqLength, PasswordValidator.hasMinLength(password), "לפחות 8 תווים")
        setRequirement(tvReqLetter, PasswordValidator.hasLetter(password), "לפחות אות אחת")
        setRequirement(tvReqDigit, PasswordValidator.hasDigit(password), "לפחות ספרה אחת")
    }

    private fun setRequirement(view: TextView, met: Boolean, label: String) {
        view.text = if (met) "✓ $label" else "✗ $label"
        view.setTextColor(if (met) requirementMet else requirementUnmet)
    }

    // Show the mismatch error only once the user has typed something
    private fun checkPasswordsMatch() {
        val password = etPassword.text.toString()
        val confirm = etConfirmPassword.text.toString()

        tilConfirmPassword.error = when {
            confirm.isEmpty() -> null
            password != confirm -> "הסיסמאות אינן תואמות"
            else -> null
        }
    }

    private fun setupClickListeners() {
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnNext).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (email.isEmpty()) {
                tilEmail.error = "יש להזין כתובת מייל"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "כתובת המייל אינה תקינה"
                return@setOnClickListener
            }

            val passwordError = PasswordValidator.validate(password)
            if (passwordError != null) {
                Toast.makeText(this, passwordError, Toast.LENGTH_SHORT).show()
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                tilConfirmPassword.error = "הסיסמאות אינן תואמות"
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