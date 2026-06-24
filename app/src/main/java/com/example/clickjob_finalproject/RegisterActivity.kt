package com.example.clickjob_finalproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var tvSkip: TextView
    private lateinit var tvStepTitle: TextView
    private lateinit var tvStepIndicator: TextView
    private lateinit var progressBar: ProgressBar

    private val stepTitles = listOf(
        "מי אני ואיפה אני?",
        "מתי אני פנוי?",
        "מיומנות",
        "רקע אישי"
    )

    private val stepIndicators = listOf(
        "יכולת התאמה (15%)",
        "יכולת התאמה (30%)",
        "יכולת התאמה (50%)",
        "יכולת התאמה (80%)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        tvStepTitle = findViewById(R.id.tvStepTitle)
        tvStepIndicator = findViewById(R.id.tvStepIndicator)
        progressBar = findViewById(R.id.progressBar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        setupViewPager()
        setupClickListeners()
    }

    private fun setupViewPager() {
        viewPager.adapter = RegisterPagerAdapter(this)
        viewPager.isUserInputEnabled = false
        updateStep(0)
    }

    private fun setupClickListeners() {
        btnNext.setOnClickListener {
            val current = viewPager.currentItem
            if (current < 3) {
                viewPager.setCurrentItem(current + 1, true)
                updateStep(current + 1)
            } else {
                navigateToMain()
            }
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            val current = viewPager.currentItem
            if (current > 0) {
                viewPager.setCurrentItem(current - 1, true)
                updateStep(current - 1)
            } else {
                finish()
            }
        }

        tvSkip.setOnClickListener {
            navigateToMain()
        }
    }

    private fun updateStep(step: Int) {
        progressBar.progress = step + 1
        tvStepTitle.text = stepTitles[step]
        tvStepIndicator.text = stepIndicators[step]
        btnNext.text = if (step == 3) "הצג לי משרות מתאימות" else "הבא"

        // Show skip button on all steps except first
        tvSkip.visibility = if (step == 0) View.GONE else View.VISIBLE
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

class RegisterPagerAdapter(activity: RegisterActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 4
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RegisterStep1Fragment()
            1 -> RegisterStep2Fragment()
            2 -> RegisterStep3Fragment()
            3 -> RegisterStep4Fragment()
            else -> RegisterStep1Fragment()
        }
    }
}