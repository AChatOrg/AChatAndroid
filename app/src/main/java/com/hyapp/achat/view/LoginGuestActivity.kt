package com.hyapp.achat.view

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityGuestLoginBinding
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

@ExperimentalCoroutinesApi
class LoginGuestActivity : EventActivity() {

    lateinit var viewModel: LoginViewModel
    lateinit var binding: ActivityGuestLoginBinding
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
//        setupHistory()
        setupKeyboardEvent()
        setupProgressDialog()
        subscribeLogged()
        binding.login.setOnClickListener {
            startActivity(Intent(this, LoginUserActivity::class.java))
            finish()
        }
    }

    private fun setupKeyboardEvent() {
        KeyboardVisibilityEvent.setEventListener(this) { isOpen ->
            if (isOpen) {
                binding.alreadyMember.visibility = View.GONE
                binding.login.visibility = View.GONE
            } else {
                binding.alreadyMember.visibility = View.VISIBLE
                binding.login.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        progressDialog.dismiss()
        super.onDestroy()
    }

    private fun init() {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guest_login)
        binding.viewModel = viewModel
    }

//    private fun setupHistory() {
//        val nameHistory = viewModel.nameHistory
//        val bioHistory = viewModel.bioHistory
//        val nameAdapter = ArrayAdapter(this, R.layout.item_suggestion, nameHistory)
//        val bioAdapter = ArrayAdapter(this, R.layout.item_suggestion, bioHistory)
//        binding.editTextUsername.threshold = 1
//        binding.editTextBio.threshold = 1
//        binding.editTextUsername.setAdapter(nameAdapter)
//        binding.editTextBio.setAdapter(bioAdapter)
//    }

    private fun setupProgressDialog() {
        progressDialog = ProgressDialog(this, R.style.RoundedCornersDialog)
        progressDialog.setTitle(R.string.login_guest)
        progressDialog.setMessage(getString(R.string.loading))
        progressDialog.setCancelable(false)
        progressDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.cancel)
        ) { dialog: DialogInterface, which: Int ->
            viewModel.cancelLogin()
            dialog.dismiss()
        }
    }

    private fun onSuccess() {
        progressDialog.dismiss()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun onError(message: String) {
        progressDialog.dismiss()
        when (message) {
            Event.MSG_EMPTY -> {
                UiUtils.vibrate(this, 200)
                binding.editTextUsername.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.shake
                    )
                )
            }
            Event.MSG_EXIST -> alert(R.string.login_guest, R.string.this_user_is_online)
            Event.MSG_NET -> alert(R.string.login_guest, R.string.no_network)
            Event.MSG_ERROR -> alert(R.string.login_guest, R.string.sorry_an_error_occurred)
            else -> alert(R.string.login_guest, message)
        }
    }

    private fun onLoading() {
        progressDialog.show()
        progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(UiUtils.getStyleColor(this, R.attr.colorPrimary))
    }

    private fun subscribeLogged() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loggedFlow.collect { event ->
                    when (event.status) {
                        Event.Status.SUCCESS -> onSuccess()
                        Event.Status.ERROR -> onError(event.msg)
                        Event.Status.LOADING -> onLoading()
                    }
                }
            }
        }
    }
}