package com.example.clickjob_finalproject.auth

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.clickjob_finalproject.MainActivity
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "שגיאה בהתחברות עם גוגל", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // If user already logged in → skip to main
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        setupGoogleSignIn()
        setupRegisterSubtitle()
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupRegisterSubtitle() {
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)
        val fullText = "עדיין אין משתמש? רישום"
        val spannable = SpannableString(fullText)
        val pinkColor = ContextCompat.getColor(this, R.color.brand_pink)
        val start = fullText.indexOf("רישום")
        spannable.setSpan(
            ForegroundColorSpan(pinkColor),
            start,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvRegisterLink.text = spannable
        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun setupClickListeners() {
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginWithEmail(email, password)
        }

        findViewById<MaterialButton>(R.id.btnGoogle).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            // TODO: forgot password screen
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Check if user already has profile in Firestore
                UserRepository.checkUserExists(
                    onExists = { navigateToMain() },
                    onNotExists = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    }
                )
            }
            .addOnFailureListener { exception ->
                val message = when (exception.message) {
                    "The password is invalid or the user does not have a password." ->
                        "סיסמא שגויה"
                    "There is no user record corresponding to this identifier. The user may have been deleted." ->
                        "משתמש לא קיים"
                    else -> "שגיאה בהתחברות, נסי שוב"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                // Check if new user or existing user
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    // New user → go to registration
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                } else {
                    // Existing user → go to main
                    navigateToMain()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "שגיאה בהתחברות עם גוגל", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}