package com.hyapp.achat.view.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hyapp.achat.R
import com.hyapp.achat.databinding.BottomSheetTwoInputButtonBinding
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.EditProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class ChangePassBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: EditProfileViewModel

    private lateinit var binding: BottomSheetTwoInputButtonBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[EditProfileViewModel::class.java]
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

        binding.title.setText(R.string.change_password)
        binding.inputOne.setHint(R.string.curr_password)
        binding.inputTwo.setHint(R.string.new_password)
        binding.inputOne.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.inputOne.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.btnText.setText(R.string.save)
        binding.inputOne.requestFocus()
        setupButton()
    }

    private fun setupButton() {
        binding.btn.setOnClickListener {
            when {
                binding.inputOne.text?.isEmpty() == true -> {
                    UiUtils.vibrate(requireContext(), 200)
                    binding.inputOne.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.shake
                        )
                    )
                }
                binding.inputTwo.text?.length ?: 0 < 4 -> {
                    Toast.makeText(requireContext(), R.string.password_too_short, Toast.LENGTH_LONG)
                        .show()
                    UiUtils.vibrate(requireContext(), 200)
                    binding.inputTwo.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.shake
                        )
                    )
                }
                else -> {
                    lifecycleScope.launch {
                        viewModel.requestChangePassword(
                            binding.inputOne.text.toString(),
                            binding.inputTwo.text.toString(),
                        ).collect { res ->
                            when (res.status) {
                                Resource.Status.SUCCESS -> {
                                    binding.progressBar.visibility = View.GONE
                                    dismiss()
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.password_changed,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                Resource.Status.ERROR -> onError(res)
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
    }

    private fun onError(res: Resource<Byte>) {
        binding.progressBar.visibility = View.GONE
        binding.btn.isEnabled = true
        when (res.message) {
            Event.MSG_NET -> Toast.makeText(
                requireContext(),
                R.string.no_network_connection,
                Toast.LENGTH_LONG
            ).show()
            Event.MSG_MATCH -> {
                UiUtils.vibrate(requireContext(), 200)
                binding.inputOne.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                )
                Toast.makeText(
                    requireContext(),
                    R.string.wrong_password,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> Toast.makeText(
                requireContext(),
                R.string.sorry_an_error_occurred,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}