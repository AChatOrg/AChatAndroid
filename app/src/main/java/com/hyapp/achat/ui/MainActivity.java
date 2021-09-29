package com.hyapp.achat.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.hyapp.achat.R;
import com.hyapp.achat.databinding.ActivityMainBinding;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.ui.fragment.GroupsFragment;
import com.hyapp.achat.ui.fragment.PeopleFragment;
import com.hyapp.achat.ui.model.AbstractTabSelectedListener;
import com.hyapp.achat.viewmodel.MainViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setupPeopleGroups();
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

        binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.onile_s), viewModel.getPeopleSize()));

        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
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
                        binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.onile_s), viewModel.getPeopleSize()));
                        transaction.show(peopleFragment);
                        transaction.hide(groupsFragment);
                        break;
                    case 1:
                        binding.peopleGroups.peopleGroupsTitle.setText(String.format(getString(R.string.groups_s), viewModel.getGroupsSize()));
                        transaction.hide(peopleFragment);
                        transaction.show(groupsFragment);
                        break;
                }
                transaction.commit();
            }
        });
    }

}