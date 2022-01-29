package com.hyapp.achat.view

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityProfileBinding
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.view.adapter.UserProfileAdapter
import com.hyapp.achat.view.component.like.LikeButton
import com.hyapp.achat.view.component.like.OnLikeListener
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.ProfileViewModel
import com.hyapp.achat.viewmodel.utils.TimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.facebook.binaryresource.FileBinaryResource

import com.facebook.imagepipeline.core.ImagePipelineFactory

import com.facebook.binaryresource.BinaryResource

import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory

import com.facebook.cache.common.CacheKey

import com.facebook.imagepipeline.request.ImageRequest
import java.io.File
import com.facebook.datasource.DataSources

import com.facebook.imagepipeline.image.CloseableImage

import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ResizeOptions

import com.facebook.imagepipeline.request.ImageRequestBuilder

import com.facebook.imagepipeline.core.ImagePipeline

import com.facebook.datasource.DataSubscriber
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.transition.TransitionInflater
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.common.executors.UiThreadImmediateExecutorService

import com.facebook.imagepipeline.image.CloseableBitmap

import com.facebook.datasource.BaseDataSubscriber
import com.hyapp.achat.view.component.AbstractTransitionListener


@ExperimentalCoroutinesApi
class ProfileActivity : EventActivity() {

    companion object {
        private const val EXTRA_USER = "extraUser"

        fun start(context: Context, user: User, transitionImage: ImageView? = null) {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USER, user)
            if (transitionImage != null && context is Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    context,
                    transitionImage,
                    context.getString(R.string.transition_avatar)
                )
                context.startActivity(intent, options.toBundle())
            } else {
                context.startActivity(intent)
            }
        }
    }

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var user: User

    private lateinit var friendsAdapter: UserProfileAdapter
    private lateinit var viewersAdapter: UserProfileAdapter

    private var isCurrUser = true
    private var isLiked = false
    private var isUserNotifEnabled = true
    private lateinit var likeButton: LikeButton
    private lateinit var notifOptionsMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setupToolbar()
        setupFriends()
        setupViewers()
        setupChatButtons()
        observeUser()
        observeUserInfo()
        setupCurrUserNotif()
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.requestUserInfo() }
        binding.avatar.setOnClickListener {
            AvatarActivity.start(this, user, binding.avatar)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isCurrUser) {
            menuInflater.inflate(R.menu.profile_curr, menu)
            val logout = menu.findItem(R.id.logout)
            logout.actionView.setOnClickListener { onOptionsItemSelected(logout) }
            val edit = menu.findItem(R.id.edit)
            edit.actionView.setOnClickListener { onOptionsItemSelected(edit) }
        } else {
            menuInflater.inflate(R.menu.profile_others, menu)
            likeButton = menu.findItem(R.id.like).actionView.findViewById(R.id.likeButton)
            likeButton.setOnLikeListener(object : OnLikeListener {
                override fun liked(likeButton: LikeButton?) {
                    isLiked = true
                    setLike(isLiked)
                    likeUser()
                }

                override fun unLiked(likeButton: LikeButton?) {
                    isLiked = false
                    setLike(isLiked)
                    likeUser()
                }
            })
            setLike(isLiked)

            notifOptionsMenuItem = menu.findItem(R.id.notif)
            if (isUserNotifEnabled) {
                notifOptionsMenuItem.setIcon(R.drawable.ic_action_notif_fill)
            } else {
                notifOptionsMenuItem.setIcon(R.drawable.ic_action_notif_outline)
            }
        }
        UiUtils.setMenuFont(this, menu, R.font.iran_sans_medium)
        return true
    }

    private fun likeUser() {
        lifecycleScope.launch {
            viewModel.requestLikeUser().collect { pair ->
                when (pair.first) {
                    ProfileViewModel.LikeStatus.LIKED -> {
                        setLike(true)
                        binding.likesCount.text = UiUtils.formatNum(pair.second)
                    }
                    ProfileViewModel.LikeStatus.DISLIKED -> {
                        setLike(false)
                        binding.likesCount.text = UiUtils.formatNum(pair.second)
                    }
                    ProfileViewModel.LikeStatus.ERROR -> {
                        Toast.makeText(
                            this@ProfileActivity,
                            R.string.check_your_connection,
                            Toast.LENGTH_LONG
                        ).show()
                        setLike(!isLiked)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.notif) {
            isUserNotifEnabled = !isUserNotifEnabled
            setNotif(isUserNotifEnabled)
        } else if (item.itemId == R.id.logout) {
            if (user.isGuest && isCurrUser) {
                yesNoAlert(
                    R.string.logout,
                    R.string.are_you_sure_logout_guest
                ) { p, p1 -> logout() }
            }
        } else if (item.itemId == R.id.edit) {
            if (isCurrUser) {
                EditProfileActivity.start(this, user)
            }
        }
        return true
    }

    private fun logout() {
        lifecycleScope.launch {
            viewModel.requestLogout().collect { res ->
                when (res.status) {
                    Resource.Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        val intent = Intent(this@ProfileActivity, LoginGuestActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    Resource.Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        if (res.message == Event.MSG_NET) {
                            alert(R.string.logout, R.string.no_network_connection)
                        } else {
                            alert(R.string.logout, R.string.sorry_an_error_occurred)
                        }
                    }
                    Resource.Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setLike(isEnable: Boolean) {
        if (::likeButton.isInitialized && !isCurrUser) {
            likeButton.isLiked = isEnable
        }
    }

    private fun setNotif(isEnable: Boolean) {
        if (::notifOptionsMenuItem.isInitialized && !isCurrUser) {
            if (isEnable) {
                notifOptionsMenuItem.setIcon(R.drawable.ic_action_notif_fill)
                viewModel.setUserNotif(true)
            } else {
                notifOptionsMenuItem.setIcon(R.drawable.ic_action_notif_outline)
                viewModel.setUserNotif(false)
            }
        }
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        user = intent.getParcelableExtra(EXTRA_USER) ?: User()
        viewModel = ViewModelProvider(
            this,
            ProfileViewModel.Factory(user)
        )[ProfileViewModel::class.java]
        isCurrUser = UserLive.value?.uid == user.uid
        binding.currUserGroup.visibility = if (isCurrUser) View.VISIBLE else View.GONE
        isUserNotifEnabled = viewModel.isUserNotifEnabled()
    }

    private fun setupToolbar() {
        binding.toolbar.overflowIcon?.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        val back = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_back, null)
        back?.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (binding.scrollView.canScrollVertically(-1)) {
                binding.toolbarDivider.visibility = View.VISIBLE
            } else {
                binding.toolbarDivider.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupChatButtons() {
        if (isCurrUser) {
            binding.videoCall.visibility = View.GONE
            binding.voiceCall.visibility = View.GONE
            binding.chat.visibility = View.GONE
        } else {
            binding.videoCall.visibility = View.VISIBLE
            binding.voiceCall.visibility = View.VISIBLE
            binding.chat.visibility = View.VISIBLE
            binding.videoCall.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.comming_soon,
                    Toast.LENGTH_LONG
                ).show()
            }
            binding.voiceCall.setOnClickListener {
                Toast.makeText(
                    this,
                    R.string.comming_soon,
                    Toast.LENGTH_LONG
                ).show()
            }
            binding.chat.setOnClickListener { ChatActivity.start(this, Contact(user)) }
        }
    }

    private fun setupFriends() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        friendsAdapter = UserProfileAdapter(this)
        binding.recyclerViewFriends.layoutManager = layoutManager
        binding.recyclerViewFriends.setHasFixedSize(true)
        binding.recyclerViewFriends.adapter = friendsAdapter
    }

    private fun setupViewers() {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewersAdapter = UserProfileAdapter(this)
        binding.recyclerViewViewers.layoutManager = layoutManager
        binding.recyclerViewViewers.setHasFixedSize(true)
        binding.recyclerViewViewers.adapter = viewersAdapter
    }

    private fun setupUser(user: User) {
        binding.run {
            toolbar.title = user.username
            Glide.with(this@ProfileActivity)
                .load(user.firstAvatar)
                .circleCrop()
                .placeholder(R.drawable.avatar_profile)
                .into(avatar)
            name.text = user.name
            bio.text = user.bio
            val pair = UserConsts.rankInt2rankStrResAndColor(user.rank)
            rank.setText(pair.first)
            rank.setTextColor(pair.second)
            if (user.onlineTime == Contact.TIME_ONLINE) {
                onlineTime.text = ""
                onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_green)
            } else {
                onlineTime.text =
                    TimeUtils.timeAgoShort(System.currentTimeMillis() - user.onlineTime)
                onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_grey)
            }
        }
    }

    private fun setupCurrUserNotif() {
        val enabled = viewModel.isCurrUserNotifEnabled()
        binding.notifsSwitch.isChecked = enabled
        binding.notifsSwitch.setOnCheckedChangeListener { _, checked ->
            viewModel.setCurrUserNotif(
                checked
            )
        }
    }

    private fun observeUser() {
        if (isCurrUser) {
            UserLive.observe(this) {
                setupUser(it)
            }
        } else {
            setupUser(user)
        }
    }

    private fun observeUserInfo() {
        viewModel.userInfoLive.observe(this) { res ->
            when (res.status) {
                Resource.Status.LOADING -> onLoading()
                Resource.Status.ERROR -> onError(res.message)
                Resource.Status.SUCCESS -> onSuccess(res.data)
            }
        }
    }

    private fun onSuccess(userInfo: UserInfo?) {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.progressBar.visibility = View.GONE
        userInfo?.let {
            binding.viewsCount.text = UiUtils.formatNum(userInfo.viewsCount)
            binding.likesCount.text = UiUtils.formatNum(userInfo.likesCount)
            binding.friendsCount.text = UiUtils.formatNum(userInfo.friendsCount.toLong())
            if (userInfo.friendList.isNotEmpty()) {
                binding.friendsDivider.visibility = View.VISIBLE
                binding.friendsTitle.visibility = View.VISIBLE
                binding.recyclerViewFriends.visibility = View.VISIBLE
                friendsAdapter.submitList(userInfo.friendList)
            } else {
                binding.friendsDivider.visibility = View.GONE
                binding.recyclerViewFriends.visibility = View.GONE
                binding.friendsTitle.visibility = View.GONE
            }
            if (userInfo.viewerList.isNotEmpty()) {
                binding.viewersDivider.visibility = View.VISIBLE
                binding.viewersTitle.visibility = View.VISIBLE
                binding.recyclerViewViewers.visibility = View.VISIBLE
                viewersAdapter.submitList(userInfo.viewerList)
            } else {
                binding.viewersDivider.visibility = View.GONE
                binding.recyclerViewViewers.visibility = View.GONE
                binding.viewersTitle.visibility = View.GONE
            }
            isLiked = userInfo.likedByMe
            setLike(isLiked)
        }
    }

    private fun onError(message: String) {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.progressBar.visibility = View.GONE
        when (message) {
            Event.MSG_NET -> alert(
                R.string.proflie,
                R.string.no_network_connection,
                false
            ) { p0, p1 -> finish() }
            Event.MSG_EMPTY -> alert(
                R.string.proflie,
                R.string.not_found,
                false
            ) { p0, p1 -> finish() }
            else -> alert(
                R.string.proflie,
                R.string.sorry_an_error_occurred,
                false
            ) { p0, p1 -> finish() }
        }
    }

    private fun onLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
}