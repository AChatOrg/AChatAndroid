package com.hyapp.achat.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
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
    private lateinit var notifOptionsMenuItem: MenuItem


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setupToolbar()
        observeUser()
        setupFriends()
        setupViewers()
        observeUserInfo()
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.requestUserInfo() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isCurrUser) {
            menuInflater.inflate(R.menu.profile_curr, menu)
        } else {
            menuInflater.inflate(R.menu.profile_others, menu)
            notifOptionsMenuItem = menu.findItem(R.id.notif)
        }
        UiUtils.setMenuFont(this, menu, R.font.iran_sans_medium)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.like) {
            isLiked = !isLiked
            setLike(isLiked)
        }
        return true
    }

    private fun setLike(isEnable: Boolean) {
        if (::notifOptionsMenuItem.isInitialized && !isCurrUser) {
            if (isEnable) {
                notifOptionsMenuItem.setIcon(R.drawable.action_like_fill)
            } else {
                notifOptionsMenuItem.setIcon(R.drawable.action_like_outline)
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
    }

    private fun setupToolbar() {
        binding.toolbar.title = user.name
        binding.toolbar.overflowIcon!!.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        val back = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_back, null)
        back!!.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(back)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
    }

    private fun setupFriends() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        friendsAdapter = UserProfileAdapter(this)
        binding.recyclerViewFriends.setHasFixedSize(true)
        binding.recyclerViewFriends.layoutManager = layoutManager
    }

    private fun setupViewers() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        viewersAdapter = UserProfileAdapter(this)
        binding.recyclerViewViewers.setHasFixedSize(true)
        binding.recyclerViewViewers.layoutManager = layoutManager
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

            friendsAdapter.submitList(userInfo.friendList)
            viewersAdapter.submitList(userInfo.viewerList)
        }
    }

    private fun onError(message: String) {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.progressBar.visibility = View.GONE
        when (message) {
            Event.MSG_NET -> alert(R.string.proflie, R.string.no_network_connection)
            Event.MSG_EMPTY -> alert(R.string.proflie, R.string.not_found)
            else -> alert(R.string.proflie, R.string.sorry_an_error_occurred)
        }
    }

    private fun onLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
}