package com.hyapp.achat.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityEditProfileBinding
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.view.fragment.ChangePassBottomSheet
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.EditProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setupScrollView()
        setupUser()
        setupGender()
        setupPassword()
        setupSaveButton()
        binding.backBtn.setOnClickListener { onBackPressed() }
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
        binding.avatar.setImageURI(user.firstAvatar)
        binding.nameEditText.setText(user.name)
        binding.bioEditText.setText(user.bio)
        binding.genderEditText.setText(if (user.isMale) R.string.male else R.string.female)
        binding.usernameEditText.setText(user.username)
        binding.passwordEditText.setText(if (user.isGuest) "" else "0000000000000000")
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
                    username.trim().isEmpty() -> {
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
}