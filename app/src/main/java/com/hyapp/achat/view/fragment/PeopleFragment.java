package com.hyapp.achat.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hyapp.achat.R;
import com.hyapp.achat.databinding.FragmentPeopleGroupsBinding;
import com.hyapp.achat.model.entity.UserList;
import com.hyapp.achat.model.entity.Resource;
import com.hyapp.achat.model.entity.Event;
import com.hyapp.achat.view.adapter.UsersAdapter;
import com.hyapp.achat.viewmodel.MainViewModel;

public class PeopleFragment extends Fragment {

    private MainViewModel viewModel;
    private FragmentPeopleGroupsBinding binding;

    private UsersAdapter adapter;

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
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.reloadPeople());
        setupRecyclerView(requireContext(), view);
        observePeople();
    }

    private void setupRecyclerView(Context context, View view) {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new UsersAdapter(context);
        binding.recyclerView.setAdapter(adapter);
    }

    private void observePeople() {
        viewModel.getUsersLive().observe(getViewLifecycleOwner(), listResource -> {
            switch (listResource.status) {
                case SUCCESS:
                    onSuccess(listResource);
                    break;
                case ERROR:
                    onError(listResource.message);
                    break;
                case LOADING:
                    onLoading();
                    break;
            }
        });
    }

    private void onSuccess(Resource<UserList> resource) {
        switch (resource.action) {
            case ADD:
                addPeople(resource);
                break;
            case REMOVE:
                adapter.removeAt(resource.data, resource.index);
                break;
            case UPDATE:
                adapter.updateAt(resource.data, resource.index);
                break;
        }
    }

    public void addPeople(Resource<UserList> resource) {
        if (resource.index == Resource.INDEX_ALL) {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
            adapter.resetList(resource.data);
        } else {
            adapter.addAt(resource.data, resource.index);
        }
    }

    private void onError(String message) {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.progressBar.setVisibility(View.GONE);
        if (Event.MSG_ERROR.equals(message)) {
            Toast.makeText(requireContext(), R.string.sorry_an_error_occurred, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void onLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }
}
