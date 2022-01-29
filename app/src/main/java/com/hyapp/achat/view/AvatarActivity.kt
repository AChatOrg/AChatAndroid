package com.hyapp.achat.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityAvatarBinding
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.view.adapter.PhotoPagerAdapter
import com.hyapp.achat.view.component.DepthPageTransformer
import com.hyapp.achat.view.utils.FullscreenHelper
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.AvatarViewModel
import me.relex.photodraweeview.OnPhotoTapListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AvatarActivity : EventActivity() {

    companion object {
        private const val EXTRA_USER = "extraUser"

        fun start(context: Context, user: User) {
            val intent = Intent(context, AvatarActivity::class.java)
            intent.putExtra(EXTRA_USER, user)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityAvatarBinding
    private lateinit var viewModel: AvatarViewModel
    private lateinit var user: User

    private lateinit var fullScreenHelper: FullscreenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.media_black))
        init()
        setupViewPager()
        setupMore()
        setupFullscreenHelper()
        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.root.setOnClickListener { fullScreenHelper.toggleUiVisibility() }
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_avatar)
        binding.lifecycleOwner = this
        user = intent.getParcelableExtra(EXTRA_USER) ?: User()
        viewModel = ViewModelProvider(
            this,
            AvatarViewModel.Factory(user)
        )[AvatarViewModel::class.java]
        binding.viewModel = viewModel
        binding.backBtn.setColorFilter(Color.WHITE)
    }

    private fun setupViewPager() {
        val adapter = PhotoPagerAdapter(this as FragmentActivity?, user.avatars)
        binding.viewPager.adapter = adapter
        binding.pageIndicator.attachToPager(binding.viewPager)
        binding.viewPager.setPageTransformer(DepthPageTransformer())
    }

    @SuppressLint("RestrictedApi")
    private fun setupMore() {
        binding.more.setOnClickListener { v ->
            val popup = PopupMenu(this, v)
            popup.menuInflater.inflate(R.menu.avatar, popup.menu)
            UiUtils.setPopupMenuFont(this, popup, R.font.iran_sans_medium)
            popup.setOnMenuItemClickListener { item: MenuItem? -> true }
            val menuHelper = MenuPopupHelper(this, popup.menu as MenuBuilder, v)
            menuHelper.setForceShowIcon(true)
            menuHelper.show()
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