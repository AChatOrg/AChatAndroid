package com.hyapp.achat.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyapp.achat.R;
import com.hyapp.achat.databinding.FragmentPeopleGroupsBinding;
import com.hyapp.achat.model.People;
import com.hyapp.achat.ui.BaseActivity;
import com.hyapp.achat.ui.adapter.PeopleAdapter;
import com.hyapp.achat.ui.utils.UiUtils;
import com.hyapp.achat.viewmodel.LoginGuestViewModel;
import com.hyapp.achat.viewmodel.MainViewModel;

import java.util.List;

public class PeopleFragment extends Fragment {

    private MainViewModel viewModel;
    private FragmentPeopleGroupsBinding binding;

    private PeopleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_people_groups, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewModel(viewModel);
        binding.setIsStatusVisible(false);
        binding.statusMessage.tryAgain.setOnClickListener(v -> viewModel.initPeople());
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.initPeople());
        setupRecyclerView(requireContext(), view);
        observePeople();
        observeNet();
    }

    private void setupRecyclerView(Context context, View view) {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new PeopleAdapter(context);
        binding.recyclerView.setAdapter(adapter);
    }

    private void observePeople() {
        viewModel.getPeopleLive().observe(getViewLifecycleOwner(), listResource -> {
            switch (listResource.status) {
                case SUCCESS:
                    binding.setIsStatusVisible(false);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    onSuccess(listResource.data);
                    break;
                case ERROR:
                    binding.swipeRefreshLayout.setRefreshing(false);
                    binding.setIsStatusVisible(true);
                    binding.progressBar.setVisibility(View.GONE);
                    onError(listResource.message);
                    break;
                case LOADING:
                    binding.setIsStatusVisible(false);
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void observeNet() {
        viewModel.getNetLive().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                binding.setIsStatusVisible(false);
            } else {
                binding.setIsStatusVisible(true);
                onError(MainViewModel.MSG_NET);
            }
        });
    }

    private void onSuccess(List<People> people) {
        adapter.resetList(people);
    }

    private void onError(String message) {
        switch (message) {
            case MainViewModel.MSG_NET:
                binding.statusMessage.text.setText(R.string.no_network_connection);
                break;
            case MainViewModel.MSG_ERROR:
                binding.statusMessage.text.setText(R.string.sorry_an_error_occurred);
                break;
            default:
                binding.statusMessage.text.setText(message);
        }
    }
}
