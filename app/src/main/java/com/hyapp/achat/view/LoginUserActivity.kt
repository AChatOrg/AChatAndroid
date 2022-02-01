package com.hyapp.achat.view

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityUserLoginBinding
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

@ExperimentalCoroutinesApi
class LoginUserActivity : EventActivity() {

    lateinit var viewModel: LoginViewModel
    lateinit var binding: ActivityUserLoginBinding
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setupHistory()
        setupKeyboardEvent()
        setupProgressDialog()
        subscribeLogged()
        binding.loginGuest.setOnClickListener {
            startActivity(Intent(this, LoginGuestActivity::class.java))
            finish()
        }
    }

    private fun setupKeyboardEvent() {
        KeyboardVisibilityEvent.setEventListener(this) { isOpen ->
            if (isOpen) {
                binding.haveNotAccount.visibility = View.GONE
                binding.loginGuest.visibility = View.GONE
            } else {
                binding.haveNotAccount.visibility = View.VISIBLE
                binding.loginGuest.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        progressDialog.dismiss()
        super.onDestroy()
    }

    private fun init() {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_login)
        binding.viewModel = viewModel
    }

    private fun setupHistory() {
        val usernameHistory = viewModel.getUsernameHistory()
        val usernameAdapter = ArrayAdapter(this, R.layout.item_suggestion, usernameHistory)
        binding.editTextUsername.threshold = 1
        binding.editTextUsername.setAdapter(usernameAdapter)
    }

    private fun setupProgressDialog() {
        progressDialog = ProgressDialog(this, R.style.RoundedCornersDialog)
        progressDialog.setTitle(R.string.login_members)
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
            Event.MSG_MATCH -> {
                UiUtils.vibrate(this, 200)
                binding.editTextPassword.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.shake
                    )
                )
                alert(R.string.login_members, R.string.incorrect_username_or_password) { _, _ ->
                    viewModel.cancelLogin()
                }
            }
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
            Event.MSG_NET -> alert(R.string.login_guest, R.string.no_network_connection)
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