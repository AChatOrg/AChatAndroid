package com.hyapp.achat.view.fragment

import android.content.DialogInterface
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.hyapp.achat.R
import android.content.DialogInterface.OnShowListener
import android.content.Intent
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hyapp.achat.databinding.BottomSheetNewRoomBinding
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.view.MainActivity
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NewRoomBottomSheetFragment : BottomSheetDialogFragment() {

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
        binding.viewModel = viewModel
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
        subscribeCreated()
    }

    private fun subscribeCreated() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.roomCreatedFlow.collect { event ->
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
            Event.MSG_GENDER -> Toast.makeText(
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