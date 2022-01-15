package com.hyapp.achat.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.hyapp.achat.R;
import com.hyapp.achat.viewmodel.MainViewModel;
import com.hyapp.achat.databinding.ActivityMainBinding;
import com.hyapp.achat.model.entity.ConnLive;
import com.hyapp.achat.model.entity.Resource;
import com.hyapp.achat.view.adapter.ContactAdapter;
import com.hyapp.achat.view.fragment.GroupsFragment;
import com.hyapp.achat.view.fragment.PeopleFragment;
import com.hyapp.achat.view.component.AbstractTabSelectedListener;

public class MainActivity extends EventActivity {

    private MainViewModel viewModel;
    private ActivityMainBinding binding;

    private ContactAdapter contactAdapter;

    private int peopleSize = 0, groupsSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setupContacts();
        observeContacts();
        setupPeopleGroups();
        observePeopleGroupsSize();
        observeConnectivity();
        setupFab();
    }

    @Override
    public void onBackPressed() {
        if (binding.peopleGroups.peopleGroupsSearchEditText.isFocused()) {
            binding.peopleGroups.peopleGroupsSearchEditText.clearFocus();
        } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (binding.searchEditText.isFocused()) {
            binding.searchEditText.clearFocus();
        } else {
            super.onBackPressed();
        }
    }

    private void init() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    private void setupContacts() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(this);
        binding.recyclerView.setAdapter(contactAdapter);
    }

    private void observeContacts() {
        viewModel.getContactsLive().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                if (resource.action == Resource.Action.ADD) {
                    if (resource.index == Resource.INDEX_ALL) {
                        contactAdapter.resetList(resource.data);
                    } else {
                        contactAdapter.putFirst(resource.data, resource.index);
                    }
                }
            }
        });
    }

    private void setupPeopleGroups() {
        final PeopleFragment peopleFragment = new PeopleFragment();
        final GroupsFragment groupsFragment = new GroupsFragment();

        binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.onile_s), peopleSize));

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment fragment : manager.getFragments()) {
            transaction.remove(fragment);
        }
        transaction
                .add(R.id.fragment, peopleFragment)
                .add(R.id.fragment, groupsFragment)
                .show(peopleFragment)
                .hide(groupsFragment)
                .commit();

        binding.peopleGroups.tabLayout.addOnTabSelectedListener(new AbstractTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final FragmentTransaction transaction = manager.beginTransaction();
                switch (tab.getPosition()) {
                    case 0:
                        transaction.show(peopleFragment);
                        transaction.hide(groupsFragment);
                        break;
                    case 1:
                        transaction.hide(peopleFragment);
                        transaction.show(groupsFragment);
                        break;
                }
                resetPeopleGroupsTitle(ConnLive.singleton().getValue());
                transaction.commit();
            }
        });
    }

    private void observePeopleGroupsSize() {
        viewModel.getUsersLive().observe(this, listResource -> {
            if (listResource.status == Resource.Status.SUCCESS) {
                peopleSize = listResource.data.size();
                resetPeopleGroupsTitle();
            }
        });
    }

    private void observeConnectivity() {
        ConnLive.singleton().observe(this, this::resetPeopleGroupsTitle);
    }

    private void resetPeopleGroupsTitle(@Nullable ConnLive.Status status) {
        if (status == null) return;
        switch (status) {
            case CONNECTING:
                binding.title.setText(R.string.connecting);
                binding.peopleGroups.peopleGroupsTitle.setText(R.string.connecting);
                break;
            case CONNECTED:
                binding.title.setText(R.string.app_name);
                resetPeopleGroupsTitle();
                break;
            case DISCONNECTED:
                binding.title.setText(R.string.disconnected);
                binding.peopleGroups.peopleGroupsTitle.setText(R.string.disconnected);
                break;
            case NO_NET:
                binding.title.setText(R.string.no_network_connection);
                binding.peopleGroups.peopleGroupsTitle.setText(R.string.no_network_connection);
                break;
        }
    }

    private void resetPeopleGroupsTitle() {
        if (binding.peopleGroups.tabLayout.getSelectedTabPosition() == 0) {
            binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.onile_s), peopleSize));
        } else {
            binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.groups_s), groupsSize));
        }
    }

    private void setupFab() {
        binding.addFab.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });
    }
}