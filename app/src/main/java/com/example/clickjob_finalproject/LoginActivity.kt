package com.example.clickjob_finalproject

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupRegisterSubtitle()
        setupClickListeners()
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
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupClickListeners() {
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            navigateToMain()
        }

        findViewById<MaterialButton>(R.id.btnGoogle).setOnClickListener {
            navigateToMain()
        }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            // TODO: forgot password screen
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}