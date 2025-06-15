package com.syahna.storyapp.views.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.syahna.storyapp.R
import com.syahna.storyapp.data.pref.UserPreference
import com.syahna.storyapp.data.pref.dataStore
import com.syahna.storyapp.data.source.LoadingStateAdapter
import com.syahna.storyapp.databinding.ActivityMainBinding
import com.syahna.storyapp.views.ViewModelFactory
import com.syahna.storyapp.views.add.AddStoryActivity
import com.syahna.storyapp.views.maps.MapsActivity
import com.syahna.storyapp.views.welcome.WelcomeActivity
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapterPaging
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedLanguage = getSavedLanguage()
        if (savedLanguage != null) {
            changeAppLanguage(savedLanguage)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MainAdapterPaging()
        binding.recyclerView.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        userPreference = UserPreference.getInstance(this.dataStore)

        observeSession()

        setSupportActionBar(binding.toolbar)
        setupView()

        observeViewModel()

        binding.buttonAdd.setOnClickListener {
            addStory()
        }
    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun observeSession() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin || user.token.isEmpty()) {
                navigateToWelcomeActivity()
            } else {
                fetchStories()
            }
        }
    }

    private fun fetchStories() {
        lifecycleScope.launch {
            viewModel.getMapsStories()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchStories()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.pagingStories.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    private fun navigateToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
        return
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("AppBundleLocaleChanges")
    @Suppress("DEPRECATION")
    private fun changeAppLanguage(code: String) {
        val currentLanguage = Locale.getDefault().language

        if (currentLanguage != code) {
            saveLanguagePreference(code)

            val locale = Locale(code)
            Locale.setDefault(locale)

            val config = Configuration(resources.configuration)
            config.setLocale(locale)

            val context = createConfigurationContext(config)

            resources.updateConfiguration(config, resources.displayMetrics)

            recreate()
        }
    }

    private fun saveLanguagePreference(languageCode: String) {
        val sharedPreferences = getSharedPreferences("language_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("language", languageCode)
        editor.apply()
    }

    private fun getSavedLanguage(): String? {
        val sharedPreferences = getSharedPreferences("language_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("language", "en")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.show_map -> {
                navigateToLocationActivity()
                true
            }
            R.id.language_english -> {
                changeAppLanguage("en")
                true
            }
            R.id.language_indonesian -> {
                changeAppLanguage("id")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.logout_dialog_title))
            .setContentText(getString(R.string.logout_dialog_content))
            .setConfirmText(getString(R.string.logout_dialog_confirm))
            .setCancelText(getString(R.string.logout_dialog_cancel))
            .setConfirmClickListener { dialog ->
                performLogout()
                dialog.dismissWithAnimation()
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()
            }

        val customView = layoutInflater.inflate(R.layout.custom_dialog_layout, null)

        sweetAlertDialog.setCustomView(customView)

        sweetAlertDialog.show()
    }

    private fun performLogout() {
        viewModel.logout()
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        finish()
    }

    private fun navigateToLocationActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun addStory() {
        val intent = Intent(this, AddStoryActivity::class.java)
        startActivity(intent)
    }
}