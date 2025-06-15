package com.syahna.storyapp.views.add

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import android.Manifest
import com.google.android.gms.location.LocationServices
import com.syahna.storyapp.R
import com.syahna.storyapp.databinding.ActivityAddBinding
import com.syahna.storyapp.util.getImageUri
import com.syahna.storyapp.util.reduceFileImage
import com.syahna.storyapp.util.showToast
import com.syahna.storyapp.util.uriToFile
import com.syahna.storyapp.views.ViewModelFactory
import com.syahna.storyapp.views.main.MainActivity
import kotlinx.coroutines.launch

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private var currentImageUri: Uri? = null
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var lat : Double? = null
    private var lon : Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addGallery.setOnClickListener {
            chooseGallery()
        }
        binding.addCamera.setOnClickListener {
            chooseCamera()
        }
        binding.buttonAdd.setOnClickListener{
            setupAction()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        observeViewModel()
    }

    private fun setupAction() {
        currentImageUri.let { uri ->
            val imageUpload = uri?.let { uriToFile(it, this).reduceFileImage() }
            val desc = binding.edAddDescription.text.toString()

            if (imageUpload != null) {
                lifecycleScope.launch {
                    viewModel.addNewstory(imageUpload, desc, lat, lon)
                }
            } else {
                showErrorDialog(getString(R.string.error_choose_image_first))
            }
        }
        binding.buttonEnableLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getCurrentLocation()
            } else {
                lat = null
                lon = null
            }
        }
    }

    private fun chooseGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", this.getString(R.string.no_media_selected))
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", getString(R.string.image_uri_log, it.toString()))
            binding.addImage.setImageURI(it)
        }
    }

    private fun chooseCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                showErrorDialog(errorMessage)
            }
        }

        viewModel.resultAddStory.observe(this) { response ->
            if (response != null) {
                showSuccessDialog(getString(R.string.success_add_story))
            }
        }
    }

    private fun getCurrentLocation() {
        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc ->
                    if(loc != null) {
                        lat = loc.latitude.toDouble()
                        lon = loc.longitude.toDouble()
                        showToast(this@AddStoryActivity, "Added Location for Story")
                    } else {
                        showToast(this@AddStoryActivity, "Location not found or disabled")
                    }
                }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {isGranted : Boolean ->
        if(isGranted) {
            getCurrentLocation()
        }
    }

    private fun showErrorDialog(message: String?) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(getString(R.string.failed_title))
            .setContentText(message ?: getString(R.string.failed_content))
            .setConfirmText(getString(R.string.ok_button))
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun showSuccessDialog(message: String?) {
        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(getString(R.string.success_title))
            .setContentText(message ?: getString(R.string.success_content))
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()

                val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .show()
    }

}