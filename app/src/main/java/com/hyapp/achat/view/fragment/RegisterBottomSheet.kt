package com.hyapp.achat.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.TextWatcherAdapter
import com.hyapp.achat.R
import com.hyapp.achat.databinding.BottomSheetTwoInputButtonBinding
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.EditProfileViewModel
import com.hyapp.achat.viewmodel.ProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class RegisterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: BottomSheetTwoInputButtonBinding
    private var primaryColor = Color.BLACK
    private var greenColor = Color.GREEN
    private var redColor = Color.RED
    private var isValidUsername = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.bottom_sheet_two_input_button,
                container,
                false
            )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val bottomSheetBehavior: BottomSheetBehavior<*> =
                    BottomSheetBehavior.from(sheet)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            }
        }
        primaryColor = UiUtils.getStyleColor(requireContext(), R.attr.colorPrimary)
        greenColor = ContextCompat.getColor(requireContext(), R.color.green_50)
        redColor = ContextCompat.getColor(requireContext(), R.color.red_nice)

        binding.title.setText(R.string.register)
        binding.inputOne.setHint(R.string.username)
        binding.inputTwo.setHint(R.string.password)
        binding.inputOne.inputType = InputType.TYPE_TEXT_VARIATION_NORMAL
        binding.inputOne.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.btnText.setText(R.string.register)
        binding.inputOne.requestFocus()
        setupUsername()
        setupButton()
    }

    private fun setupUsername() {
        binding.inputOne.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(
                username: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                lifecycleScope.launch {
                    viewModel.requestCheckUsername(username).collect { event ->
                        if (event.status == Event.Status.ERROR) {
                            isValidUsername = false
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.title.setTextColor(redColor)
                            when (event.msg) {
                                Event.MSG_MATCH -> binding.title.setText(R.string.invalid_username)
                                Event.MSG_EXIST -> binding.title.setText(R.string.username_exist)
                            }
                        } else {
                            binding.title.setTextColor(primaryColor)
                            binding.title.setText(R.string.register)
                            if (event.status == Event.Status.LOADING) {
                                binding.progressBar.visibility = View.VISIBLE
                            } else if (event.status == Event.Status.SUCCESS) {
                                isValidUsername = true
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.title.setTextColor(greenColor)
                                binding.title.setText(R.string.username_available)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun setupButton() {
        binding.btn.setOnClickListener {
            if (binding.inputOne.text?.isEmpty() == true || !isValidUsername) {
                UiUtils.vibrate(requireContext(), 200)
                binding.inputOne.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                )
            } else if (binding.inputTwo.text?.isEmpty() == true) {
                UiUtils.vibrate(requireContext(), 200)
                binding.inputTwo.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                )
            } else if (binding.inputTwo.text?.length ?: 0 < 4) {
                Toast.makeText(requireContext(), R.string.password_too_short, Toast.LENGTH_LONG)
                    .show()
                UiUtils.vibrate(requireContext(), 200)
                binding.inputTwo.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                )
            } else {
                lifecycleScope.launch {
                    viewModel.requestRegister(
                        binding.inputOne.text.toString(),
                        binding.inputTwo.text.toString(),
                    ).collect { res ->
                        when (res.status) {
                            Resource.Status.SUCCESS -> {
                                binding.progressBar.visibility = View.GONE
                                dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    R.string.successfully_registered,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Resource.Status.ERROR -> onError(res.message)
                            Resource.Status.LOADING -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.btn.isEnabled = false
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.btn.isEnabled = true
        when (msg) {
            Event.MSG_NET -> Toast.makeText(
                requireContext(),
                R.string.no_network_connection,
                Toast.LENGTH_LONG
            ).show()
            Event.MSG_EXIST -> Toast.makeText(
                requireContext(),
                R.string.wrong_password,
                Toast.LENGTH_LONG
            ).show()
            else -> Toast.makeText(
                requireContext(),
                R.string.sorry_an_error_occurred,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}