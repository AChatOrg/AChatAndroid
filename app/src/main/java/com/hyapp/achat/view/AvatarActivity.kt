package com.hyapp.achat.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityAvatarBinding
import com.hyapp.achat.model.entity.Event
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.view.adapter.PhotoPagerAdapter
import com.hyapp.achat.view.component.DepthPageTransformer
import com.hyapp.achat.view.utils.FullscreenHelper
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.AvatarViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.relex.photodraweeview.OnPhotoTapListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.IndexOutOfBoundsException

@ExperimentalCoroutinesApi
class AvatarActivity : EventActivity() {

    companion object {
        private const val EXTRA_USER = "extraUser"
        private const val EXTRA_HAS_DELETE_AVATAR = "extraHasDeleteAvatar"

        fun start(context: Context, user: User, hasDeleteAvatar: Boolean) {
            val intent = Intent(context, AvatarActivity::class.java)
            intent.putExtra(EXTRA_USER, user)
            intent.putExtra(EXTRA_HAS_DELETE_AVATAR, hasDeleteAvatar)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityAvatarBinding
    private lateinit var viewModel: AvatarViewModel
    private lateinit var user: User
    private var hasDeleteAvatar = false

    private lateinit var fullScreenHelper: FullscreenHelper
    private lateinit var adapter: PhotoPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.media_black))
        init()
        setupViewPager()
        setupFullscreenHelper()
        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.root.setOnClickListener { fullScreenHelper.toggleUiVisibility() }
        if (hasDeleteAvatar) {
            setupDelete()
        } else {
            binding.delete.visibility = View.GONE
        }
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_avatar)
        binding.lifecycleOwner = this
        user = intent.getParcelableExtra(EXTRA_USER) ?: User()
        hasDeleteAvatar = intent.getBooleanExtra(EXTRA_HAS_DELETE_AVATAR, false)
        viewModel = ViewModelProvider(
            this,
            AvatarViewModel.Factory(user)
        )[AvatarViewModel::class.java]
        binding.viewModel = viewModel
        binding.backBtn.setColorFilter(Color.WHITE)
    }

    private fun setupViewPager() {
        adapter = PhotoPagerAdapter(
            this as FragmentActivity?,
            user.avatars as MutableList<String>
        )
        binding.viewPager.adapter = adapter
        binding.pageIndicator.attachToPager(binding.viewPager)
        binding.viewPager.setPageTransformer(DepthPageTransformer())
    }

    @SuppressLint("RestrictedApi")
    private fun setupDelete() {
        binding.delete.setOnClickListener { v ->
            yesNoAlert(R.string.delete_avatar, R.string.are_you_sure_delete_avatar) { _, _ ->
                try {
                    val avatar = user.avatars[binding.viewPager.currentItem]
                    lifecycleScope.launch {
                        viewModel.requestDeleteAvatar(avatar).collect { res ->
                            when (res.status) {
                                Resource.Status.SUCCESS -> {
                                    binding.progressBar.visibility = View.GONE
                                    adapter.remove(avatar)
                                    user.avatars = res.data ?: listOf()
                                    binding.pageIndicator.attachToPager(binding.viewPager)
                                    binding.delete.visibility =
                                        if (user.avatars.isNotEmpty()) View.VISIBLE else View.GONE
                                }
                                Resource.Status.ERROR -> {
                                    binding.progressBar.visibility = View.GONE
                                    when (res.message) {
                                        Event.MSG_NET -> Toast.makeText(
                                            this@AvatarActivity,
                                            R.string.no_network_connection,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        else -> Toast.makeText(
                                            this@AvatarActivity,
                                            R.string.sorry_an_error_occurred,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                }
                                Resource.Status.LOADING -> {
                                    binding.progressBar.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupFullscreenHelper() {
        fullScreenHelper = FullscreenHelper(this)
        fullScreenHelper.configureToolbarSpacer(binding.toolbarCutoutSpacer)
        fullScreenHelper.showAndHideWithSystemUI(window, binding.group)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.header.visibility = View.GONE
            FullscreenHelper.disallowChangeVisibility(binding.group)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.header.visibility = View.VISIBLE
            FullscreenHelper.allowChangeVisibility(binding.group)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPhotoTap(event: OnPhotoTapListener?) {
        fullScreenHelper.toggleUiVisibility()
    }
}