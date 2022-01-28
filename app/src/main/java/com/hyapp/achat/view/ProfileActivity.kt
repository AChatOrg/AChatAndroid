package com.hyapp.achat.view

import android.content.Context
import android.content.DialogInterface
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityProfileBinding
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.view.adapter.UserProfileAdapter
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.ProfileViewModel
import com.hyapp.achat.viewmodel.utils.TimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
    private var isNotifed = true
    private lateinit var likeOptionsMenuItem: MenuItem
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
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.requestUserInfo() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isCurrUser) {
            menuInflater.inflate(R.menu.profile_curr, menu)
        } else {
            menuInflater.inflate(R.menu.profile_others, menu)
            likeOptionsMenuItem = menu.findItem(R.id.like)
            notifOptionsMenuItem = menu.findItem(R.id.notif)
        }
        UiUtils.setMenuFont(this, menu, R.font.iran_sans_medium)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.like) {
            isLiked = !isLiked
            setLike(isLiked)
        } else if (item.itemId == R.id.notif) {
            isNotifed = !isNotifed
            setNotif(isNotifed)
        }
        return true
    }

    private fun setLike(isEnable: Boolean) {
        if (::likeOptionsMenuItem.isInitialized && !isCurrUser) {
            if (isEnable) {
                likeOptionsMenuItem.setIcon(R.drawable.action_like_fill)
            } else {
                likeOptionsMenuItem.setIcon(R.drawable.action_like_outline)
            }
        }
    }

    private fun setNotif(isEnable: Boolean) {
        if (::notifOptionsMenuItem.isInitialized && !isCurrUser) {
            if (isEnable) {
                notifOptionsMenuItem.setIcon(R.drawable.ic_action_notif_fill)
            } else {
                notifOptionsMenuItem.setIcon(R.drawable.ic_action_notif_outline)
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
    }

    private fun setupToolbar() {
        binding.toolbar.title = user.name
        binding.toolbar.overflowIcon?.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        val back = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_back, null)
        back?.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
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
            binding.viewsCount.text = userInfo.viewsCount
            binding.likesCount.text = userInfo.likesCount
            binding.friendsCount.text = userInfo.friendsCount
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