package com.hyapp.achat.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

@ExperimentalCoroutinesApi
class ProfileActivity : EventActivity() {

    companion object {
        private const val EXTRA_USER = "extraUser"

        fun start(context: Context, user: User) {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USER, user)
            context.startActivity(intent)
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
        observeUser()
        setupFriends()
        setupViewers()
        setupChatButtons()
        observeUserInfo()
        subscribeLikeEvent()
        setupCurrUserNotif()
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.requestUserInfo() }
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
                    viewModel.requestLikeUser()
                }

                override fun unLiked(likeButton: LikeButton?) {
                    isLiked = false
                    setLike(isLiked)
                    viewModel.requestLikeUser()
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
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

    private fun observeUser() {
        viewModel.userLive.observe(this) {
            binding.run {
                toolbar.title = it.username
                val avatars: List<String> = it.avatars
                avatar.setImageURI(if (avatars.isNotEmpty()) avatars[0] else null)
                name.text = it.name
                bio.text = it.bio
                val pair = UserConsts.rankInt2rankStrResAndColor(it.rank)
                rank.setText(pair.first)
                rank.setTextColor(pair.second)
                if (it.onlineTime == Contact.TIME_ONLINE) {
                    onlineTime.text = ""
                    onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_green)
                } else {
                    onlineTime.text =
                        TimeUtils.timeAgoShort(System.currentTimeMillis() - user.onlineTime)
                    onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_grey)
                }
            }
        }
    }

    private fun subscribeLikeEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.likeFlow.collect { pair ->
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