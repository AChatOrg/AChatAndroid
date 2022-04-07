package com.hyapp.achat.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.internal.TextWatcherAdapter
import com.hyapp.achat.Config
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityEditProfileBinding
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.view.component.cropper.CropImage
import com.hyapp.achat.view.component.cropper.CropImageView
import com.hyapp.achat.view.fragment.ChangePassBottomSheet
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.EditProfileViewModel
import com.hyapp.achat.viewmodel.permissions.Permissions
import gun0912.tedbottompicker.TedRxBottomPicker
import id.zelory.compressor.Compressor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

@ExperimentalCoroutinesApi
class EditProfileActivity : EventActivity() {

    companion object {
        private const val EXTRA_USER = "extraUser"

        fun start(context: Context, user: User) {
            val intent = Intent(context, EditProfileActivity::class.java)
            intent.putExtra(EXTRA_USER, user)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel
    private lateinit var user: User

    private lateinit var avatarsResult: MutableList<String>

    private var isValidUsername = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setupScrollView()
        setupUser()
        setupGender()
        setupSaveButton()
        observeUser()
        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.avatar.setOnClickListener {
            if (user.avatars.isNotEmpty())
                AvatarActivity.start(this, user, true)
        }
        if (user.isGuest) {
            binding.usernameLayout.visibility = View.GONE
            binding.passwordLayout.visibility = View.GONE
            binding.addPicTextView.visibility = View.GONE
        } else {
            setupAddPic()
            setupUsername()
            setupPassword()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    @SuppressLint("CheckResult")
    private fun setupAddPic() {
        binding.addPicTextView.setOnClickListener {
            if (user.avatars.size >= 10) {
                alert(R.string.add_profile_picture, R.string.you_can_add_up_to_ten_avatars)
            } else {
                Permissions.with(this)
                    .request(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    )
                    .ifNecessary()
                    .withRationaleDialog(
                        R.string.storage_camera_permission_message,
                        R.drawable.permission_cam,
                        R.drawable.permission_storage
                    )
                    .withPermanentDenialDialog(R.string.storage_camera_permission_need)
                    .onAnyDenied {
                        Toast.makeText(
                            this,
                            R.string.storage_camera_permission_need,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .onAllGranted {
                        try {
                            TedRxBottomPicker.with(this)
                                .show()
                                .subscribe({ uri ->
                                    if (uri.path?.lastIndexOf(".")?.plus(1)
                                            ?.let { it1 ->
                                                uri.path?.substring(it1)?.isNotEmpty()
                                            } == true
                                    ) {
                                        lifecycleScope.launch {
                                            try {
                                                val compressed = Compressor.compress(
                                                    this@EditProfileActivity,
                                                    File(uri.path)
                                                )
                                                CropImage.activity(Uri.fromFile(compressed))
                                                    .setFixAspectRatio(true)
                                                    .setCropShape(CropImageView.CropShape.OVAL)
                                                    .setInitialCropWindowPaddingRatio(0.0f)
                                                    .setCropMenuCropButtonTitle(getString(R.string.save))
                                                    .start(this@EditProfileActivity)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                Toast.makeText(
                                                    this@EditProfileActivity,
                                                    R.string.this_photo_not_exists,
                                                    Toast.LENGTH_LONG
                                                )
                                                    .show()
                                            }
                                        }
                                    }
                                }, { t -> t.printStackTrace() })
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, R.string.this_photo_not_exists, Toast.LENGTH_LONG)
                                .show()
                        }
                    }.execute()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                val uri = CropImage.getActivityResult(data).uri
                lifecycleScope.launch {
                    viewModel.requestAddAvatar(uri).collect { res ->
                        when (res.status) {
                            Resource.Status.SUCCESS -> {
                                binding.progressBar.visibility = View.GONE
                            }
                            Resource.Status.LOADING -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            Resource.Status.ERROR -> {
                                binding.progressBar.visibility = View.GONE
                                when (res.message) {
                                    Event.MSG_NET -> Toast.makeText(
                                        this@EditProfileActivity,
                                        R.string.no_network_connection,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    else -> {
                                        Log.e("ssss", res.message)
                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            R.string.sorry_an_error_occurred,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupScrollView() {
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (binding.scrollView.canScrollVertically(-1)) {
                binding.toolbarDivider.visibility = View.VISIBLE
            } else {
                binding.toolbarDivider.visibility = View.INVISIBLE
            }
        }
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        user = intent.getParcelableExtra(EXTRA_USER) ?: User()
        viewModel = ViewModelProvider(
            this,
            EditProfileViewModel.Factory(user)
        )[EditProfileViewModel::class.java]
        avatarsResult = user.avatars as MutableList<String>
    }

    private fun setupUser() {
        val avatars: List<String> = user.avatars
        binding.run {
            avatar.setImageURI(if (avatars.isNotEmpty()) Config.SERVER_URL + avatars[0] else null)
            nameEditText.setText(user.name)
            bioEditText.setText(user.bio)
            genderEditText.setText(if (user.isMale) R.string.male else R.string.female)
            usernameEditText.setText(user.username.replace("-", ""))
            passwordEditText.setText(if (user.isGuest) "" else "0000000000000000")   
        }
    }

    private fun setupGender() {
        binding.genderEditText.setOnClickListener {
            UiUtils.showListDialog(
                this,
                R.string.gender,
                intArrayOf(R.string.male, R.string.female),
                if (binding.genderEditText.text.toString() == getString(R.string.male)) 0 else 1
            ) { value ->
                binding.genderEditText.setText(
                    value
                )
            }
        }
    }

    private fun setupPassword() {
        binding.passwordEditText.setOnClickListener {
            val bottomSheet = ChangePassBottomSheet()
            bottomSheet.show(supportFragmentManager, "changePass")
        }
    }

    private fun isChanged(
        name: String,
        bio: String,
        gender: Byte,
        username: String,
    ): Boolean {
        return (user.avatars != avatarsResult
                || user.name != name
                || user.bio != bio
                || user.gender != gender
                || user.username != username)
    }

    private fun setupUsername() {
        binding.usernameEditText.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(
                username: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                lifecycleScope.launch {
                    viewModel.requestCheckUsername(username.toString()).collect { event ->
                        if (event.status == Event.Status.ERROR) {
                            isValidUsername = false
                            binding.progressBar.visibility = View.INVISIBLE
                            when (event.msg) {
                                Event.MSG_MATCH -> {
                                    binding.usernameLayout.error =
                                        getString(R.string.invalid_username)
                                }
                                Event.MSG_EXIST -> {
                                    binding.usernameLayout.error =
                                        getString(R.string.username_exist)
                                }
                            }
                        } else {
                            binding.usernameLayout.error = null
                            binding.title.setText(R.string.register)
                            if (event.status == Event.Status.LOADING) {
                                binding.progressBar.visibility = View.VISIBLE
                            } else if (event.status == Event.Status.SUCCESS) {
                                isValidUsername = true
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.usernameLayout.error = null
                            }
                        }
                    }
                }
            }
        })
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val bio = binding.bioEditText.text.toString()
            val gender =
                if (binding.genderEditText.text.toString() == getString(R.string.male)) UserConsts.GENDER_MALE else UserConsts.GENDER_FEMALE
            val username = binding.usernameEditText.text.toString()
            if (isChanged(name, bio, gender, username)) {
                when {
                    name.trim().isEmpty() -> {
                        UiUtils.vibrate(this, 200)
                        binding.nameLayout.startAnimation(
                            AnimationUtils.loadAnimation(
                                this,
                                R.anim.shake
                            )
                        )
                    }
                    !isValidUsername || username.trim().isEmpty() -> {
                        UiUtils.vibrate(this, 200)
                        binding.usernameLayout.startAnimation(
                            AnimationUtils.loadAnimation(
                                this,
                                R.anim.shake
                            )
                        )
                    }
                    else -> {
                        lifecycleScope.launch {
                            viewModel.requestEditProfile(avatarsResult, name, bio, gender, username)
                                .collect { res ->
                                    when (res.status) {
                                        Resource.Status.SUCCESS -> onSuccess()
                                        Resource.Status.LOADING -> onLoading()
                                        Resource.Status.ERROR -> onError(res.message)
                                    }
                                }
                        }
                    }
                }
            } else {
                Toast.makeText(this, R.string.you_did_not_make_change, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun onSuccess() {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, R.string.profile_changed, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun onError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        when (msg) {
            Event.MSG_NET -> Toast.makeText(
                this,
                R.string.no_network_connection,
                Toast.LENGTH_LONG
            ).show()
            else -> Toast.makeText(
                this,
                R.string.sorry_an_error_occurred,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun onLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false
    }

    private fun observeUser() {
        UserLive.observe(this) {
            it?.let {
                user = it
                binding.avatar.setImageURI(Config.SERVER_URL + it.firstAvatar)
            }
        }
    }
}