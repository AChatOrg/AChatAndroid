package com.hyapp.achat.ui;

import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.hyapp.achat.R;
import com.hyapp.achat.bl.MainViewModel;
import com.hyapp.achat.databinding.ActivityMainBinding;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.ui.fragment.GroupsFragment;
import com.hyapp.achat.ui.fragment.PeopleFragment;
import com.hyapp.achat.ui.model.AbstractTabSelectedListener;

public class MainActivity extends EventActivity {

    private MainViewModel viewModel;
    private ActivityMainBinding binding;

    private int peopleSize = 0, groupsSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setupPeopleGroups();
        observePeopleGroupsSize();
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
        viewModel.init();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
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
                        binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.onile_s), peopleSize));
                        transaction.show(peopleFragment);
                        transaction.hide(groupsFragment);
                        break;
                    case 1:
                        binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.groups_s), groupsSize));
                        transaction.hide(peopleFragment);
                        transaction.show(groupsFragment);
                        break;
                }
                transaction.commit();
            }
        });
    }

    private void observePeopleGroupsSize() {
        viewModel.getPeopleLive().observe(this, listResource -> {
            if (listResource.status == Resource.Status.SUCCESS) {
                peopleSize = listResource.data.size();
                if (binding.peopleGroups.tabLayout.getSelectedTabPosition() == 0) {
                    binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.onile_s), peopleSize));
                }
            }
        });
    }

    private void setupFab() {
        binding.addFab.setOnClickListener(v -> {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });
    }

}