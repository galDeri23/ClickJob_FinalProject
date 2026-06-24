package com.example.clickjob_finalproject.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.clickjob_finalproject.MainActivity
import com.example.clickjob_finalproject.R
import com.example.clickjob_finalproject.data.repository.UserRepository
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var tvSkip: TextView
    private lateinit var tvStepTitle: TextView
    private lateinit var tvStepIndicator: TextView
    private lateinit var progressBar: ProgressBar

    val registerViewModel: RegisterViewModel by viewModels()

    private val stepTitles = listOf(
        "מי אני?",
        "מתי אני פנוי?",
        "מיומנויות",
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        tvStepTitle = findViewById(R.id.tvStepTitle)
        tvStepIndicator = findViewById(R.id.tvStepIndicator)
        progressBar = findViewById(R.id.progressBar)

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
            when (current) {
                0 -> saveStep1AndContinue()
                1 -> saveStep2AndContinue()
                2 -> saveStep3AndContinue()
                3 -> saveStep4AndFinish()
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

    private fun saveStep1AndContinue() {
        if (registerViewModel.name.isEmpty() || registerViewModel.phone.isEmpty() ||
            registerViewModel.email.isEmpty() || registerViewModel.address.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות החובה", Toast.LENGTH_SHORT).show()
            return
        }

        UserRepository.saveUserProfile(
            profile = registerViewModel.buildUserProfile(),
            onSuccess = { goToNextStep() },
            onFailure = {
                Toast.makeText(this, "שגיאה בשמירה, נסי שוב", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveStep2AndContinue() {
        val fragment = getCurrentFragment() as? RegisterStep2Fragment ?: return
        registerViewModel.availableDays = fragment.getSelectedDays()

        UserRepository.saveUserProfile(
            profile = registerViewModel.buildUserProfile(),
            onSuccess = { goToNextStep() },
            onFailure = {
                Toast.makeText(this, "שגיאה בשמירה, נסי שוב", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveStep3AndContinue() {
        val fragment = getCurrentFragment() as? RegisterStep3Fragment ?: return
        val data = fragment.getSelectedSkills()

        registerViewModel.languages = data.languages
        registerViewModel.licenses = data.licenses
        registerViewModel.certificates = data.certificates
        registerViewModel.software = data.software
        registerViewModel.jobCategories = data.jobCategories
        registerViewModel.softSkills = data.softSkills
        registerViewModel.other = data.other

        UserRepository.saveUserProfile(
            profile = registerViewModel.buildUserProfile(),
            onSuccess = { goToNextStep() },
            onFailure = {
                Toast.makeText(this, "שגיאה בשמירה, נסי שוב", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveStep4AndFinish() {
        val fragment = getCurrentFragment() as? RegisterStep4Fragment ?: return
        registerViewModel.cvUrl = fragment.getCvUrl()

        UserRepository.saveUserProfile(
            profile = registerViewModel.buildUserProfile(),
            onSuccess = { navigateToMain() },
            onFailure = {
                Toast.makeText(this, "שגיאה בשמירה, נסי שוב", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun goToNextStep() {
        val current = viewPager.currentItem
        viewPager.setCurrentItem(current + 1, true)
        updateStep(current + 1)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
            ?: supportFragmentManager.fragments.firstOrNull { fragment ->
                when (viewPager.currentItem) {
                    0 -> fragment is RegisterStep1Fragment
                    1 -> fragment is RegisterStep2Fragment
                    2 -> fragment is RegisterStep3Fragment
                    3 -> fragment is RegisterStep4Fragment
                    else -> false
                }
            }
    }

    private fun updateStep(step: Int) {
        progressBar.progress = step + 1
        tvStepTitle.text = stepTitles[step]
        tvStepIndicator.text = stepIndicators[step]
        btnNext.text = if (step == 3) "הצג לי משרות מתאימות" else "הבא"
        if (step == 3) {
            btnNext.setBackgroundResource(R.drawable.bg_login_gradient)
        }
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