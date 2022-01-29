package com.hyapp.achat.view.fragment

import android.os.Bundle
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
import com.hyapp.achat.databinding.BottomSheetChangePassBinding
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

    private lateinit var binding: BottomSheetChangePassBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[EditProfileViewModel::class.java]
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.bottom_sheet_change_pass,
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
        setupButton()
    }

    private fun setupButton() {
        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                viewModel.requestChangePassword(
                    binding.currPassword.text.toString(),
                    binding.newPassword.text.toString(),
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
                            binding.btnSave.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun onError(res: Resource<Byte>) {
        binding.progressBar.visibility = View.GONE
        binding.btnSave.isEnabled = true
        when (res.message) {
            Event.MSG_EMPTY -> {
                if (res.data == EditProfileViewModel.EMPTY_CURR_PASS) {
                    UiUtils.vibrate(requireContext(), 200)
                    binding.currPassword.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.shake
                        )
                    )
                } else if (res.data == EditProfileViewModel.EMPTY_NEW_PASS) {
                    UiUtils.vibrate(requireContext(), 200)
                    binding.newPassword.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.shake
                        )
                    )
                }
            }
            Event.MSG_NET -> Toast.makeText(
                requireContext(),
                R.string.no_network_connection,
                Toast.LENGTH_LONG
            ).show()
            Event.MSG_MATCH -> Toast.makeText(
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