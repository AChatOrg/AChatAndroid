package com.hyapp.achat.view

import android.annotation.SuppressLint
import android.app.Activity
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
import android.app.ActivityOptions
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcelable
import android.transition.TransitionInflater
import android.transition.TransitionListenerAdapter
import android.util.Log
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.DraweeTransition
import com.facebook.drawee.view.SimpleDraweeView
import com.hyapp.achat.view.component.AbstractTransitionListener


class AvatarActivity : EventActivity() {

    companion object {
        private const val EXTRA_USER = "extraUser"

        fun start(activity: Activity, user: User, transitionImage: ImageView) {
            val intent = Intent(activity, AvatarActivity::class.java)
            intent.putExtra(EXTRA_USER, user)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    activity,
                    transitionImage,
                    activity.getString(R.string.transition_avatar)
                )
                activity.startActivity(intent, options.toBundle())
            } else {
                activity.startActivity(intent)
            }
        }
    }

    private lateinit var binding: ActivityAvatarBinding
    private lateinit var viewModel: AvatarViewModel
    private lateinit var user: User

    private lateinit var adapter: PhotoPagerAdapter
    private lateinit var fullScreenHelper: FullscreenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.media_black))
        init()
        setupTransition()
        setupViewPager()
        setupMore()
        setupFullscreenHelper()
        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.root.setOnClickListener { fullScreenHelper.toggleUiVisibility() }
    }

    private fun setupTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val inflater = TransitionInflater.from(this)
            val window = window
            window.enterTransition = inflater.inflateTransition(R.transition.fade)
            postponeEnterTransition()
            val enterTransition =
                inflater.inflateTransition(R.transition.circle_to_square_image_transition)
            val returnTransition =
                inflater.inflateTransition(R.transition.square_to_circle_image_transition)

            window.sharedElementEnterTransition = enterTransition
            window.sharedElementReturnTransition = returnTransition

            Glide.with(this)
                .asBitmap()
                .load(user.firstAvatar)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        binding.transitionImage.setImageDrawable(
                            RoundedBitmapDrawableFactory.create(
                                resources,
                                resource
                            )
                        )
                        startPostponedEnterTransition()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

            enterTransition.addListener(object : AbstractTransitionListener() {
                override fun onTransitionEnd(p0: android.transition.Transition?) {
                    binding.viewPager.visibility = View.VISIBLE
                }
            })

            returnTransition.addListener(object : AbstractTransitionListener() {
                override fun onTransitionStart(transition: android.transition.Transition?) {
                    super.onTransitionStart(transition)
                    binding.viewPager.visibility = View.INVISIBLE
                }
            })
        }
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
        adapter = PhotoPagerAdapter(this as FragmentActivity?, user.avatars)
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