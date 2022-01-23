package com.hyapp.achat.view

import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ActivityMainBinding
import com.hyapp.achat.model.entity.ConnLive
import com.hyapp.achat.model.entity.ContactList
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.view.adapter.ContactAdapter
import com.hyapp.achat.view.component.AbstractTabSelectedListener
import com.hyapp.achat.view.fragment.GroupsFragment
import com.hyapp.achat.view.fragment.UsersFragment
import com.hyapp.achat.viewmodel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainActivity : EventActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var contactAdapter: ContactAdapter

    private var peopleSize = 0
    private val groupsSize = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        setupContacts()
        observeContacts()
        setupPeopleGroups()
        observePeopleGroupsSize()
        observeConnectivity()
        setupFab()
    }

    override fun onStart() {
        super.onStart()
        viewModel.activityStarted()
    }

    override fun onStop() {
        super.onStop()
        viewModel.activityStopped()
    }

    override fun onBackPressed() {
        when {
            binding.peopleGroups.peopleGroupsSearchEditText.isFocused -> {
                binding.peopleGroups.peopleGroupsSearchEditText.clearFocus()
            }
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            binding.searchEditText.isFocused -> {
                binding.searchEditText.clearFocus()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun init() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    private fun setupContacts() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        contactAdapter = ContactAdapter(this)
        binding.recyclerView.adapter = contactAdapter
    }

    private fun observeContacts() {
        viewModel.contactsLive.observe(
            this,
            { contactList: ContactList? -> contactAdapter.submitList(contactList) })
    }

    private fun setupPeopleGroups() {
        val usersFragment = UsersFragment()
        val groupsFragment = GroupsFragment()
        binding.peopleGroups.peopleGroupsTitle.text =
            String.format(getString(R.string.onile_s), peopleSize)
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        for (fragment in manager.fragments) {
            transaction.remove(fragment)
        }
        transaction
            .add(R.id.fragment, usersFragment)
            .add(R.id.fragment, groupsFragment)
            .show(usersFragment)
            .hide(groupsFragment)
            .commit()
        binding.peopleGroups.tabLayout.addOnTabSelectedListener(object :
            AbstractTabSelectedListener() {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val trans = manager.beginTransaction()
                when (tab.position) {
                    0 -> {
                        trans.show(usersFragment)
                        trans.hide(groupsFragment)
                    }
                    1 -> {
                        trans.hide(usersFragment)
                        trans.show(groupsFragment)
                    }
                }
                resetPeopleGroupsTitle(ConnLive.singleton().value)
                trans.commit()
            }
        })
    }

    private fun observePeopleGroupsSize() {
        viewModel.usersLive.observe(this) { res ->
            if (res.status == Resource.Status.SUCCESS) {
                peopleSize = res.data!!.size
                resetPeopleGroupsTitle()
            }
        }
    }

    private fun observeConnectivity() {
        ConnLive.singleton()
            .observe(this, { status: ConnLive.Status? -> this.resetPeopleGroupsTitle(status) })
    }

    private fun resetPeopleGroupsTitle(status: ConnLive.Status?) {
        if (status == null) return
        when (status) {
            ConnLive.Status.CONNECTING -> {
                binding.title.setText(R.string.connecting)
                binding.peopleGroups.peopleGroupsTitle.setText(R.string.connecting)
            }
            ConnLive.Status.CONNECTED -> {
                binding.title.setText(R.string.app_name)
                resetPeopleGroupsTitle()
            }
            ConnLive.Status.DISCONNECTED -> {
                binding.title.setText(R.string.disconnected)
                binding.peopleGroups.peopleGroupsTitle.setText(R.string.disconnected)
            }
            ConnLive.Status.NO_NET -> {
                binding.title.setText(R.string.no_network_connection)
                binding.peopleGroups.peopleGroupsTitle.setText(R.string.no_network_connection)
            }
        }
    }

    private fun resetPeopleGroupsTitle() {
        if (binding.peopleGroups.tabLayout.selectedTabPosition == 0) {
            binding.peopleGroups.peopleGroupsTitle.text =
                String.format(getString(R.string.onile_s), peopleSize)
        } else {
            binding.peopleGroups.peopleGroupsTitle.text =
                String.format(getString(R.string.groups_s), groupsSize)
        }
    }

    private fun setupFab() {
        binding.addFab.setOnClickListener {
            binding.drawerLayout.openDrawer(
                GravityCompat.START
            )
        }
    }
}