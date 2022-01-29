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
import com.hyapp.achat.databinding.BottomSheetNewRoomBinding
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.UserConsts
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NewRoomBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var binding: BottomSheetNewRoomBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding =
            DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_new_room, container, false)
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
        binding.btnCreate.setOnClickListener {
            lifecycleScope.launch {
                viewModel.createRoom(
                    binding.editTextName.text.toString(),
                    when {
                        binding.radioBtnMixed.isChecked -> UserConsts.GENDER_MIXED
                        binding.radioBtnMale.isChecked -> UserConsts.GENDER_MALE
                        else -> UserConsts.GENDER_FEMALE
                    }
                ).collect { event ->
                    when (event.status) {
                        Event.Status.SUCCESS -> {
                            binding.progressBar.visibility = View.GONE
                            dismiss()
                            Toast.makeText(
                                requireContext(),
                                binding.editTextName.text.toString() + " " + getString(R.string.created),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Event.Status.ERROR -> onError(event.msg)
                        Event.Status.LOADING -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnCreate.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun onError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.btnCreate.isEnabled = true
        when (message) {
            Event.MSG_EMPTY -> {
                UiUtils.vibrate(requireContext(), 200)
                binding.editTextName.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                )
            }
            Event.MSG_NET -> Toast.makeText(
                requireContext(),
                R.string.no_network_connection,
                Toast.LENGTH_LONG
            ).show()
            Event.MSG_MATCH -> Toast.makeText(
                requireContext(),
                R.string.room_gender_doest_match_your_gender,
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