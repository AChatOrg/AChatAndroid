package com.hyapp.achat.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hyapp.achat.R;
import com.hyapp.achat.databinding.FragmentPeopleGroupsBinding;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.SortedList;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.ui.adapter.PeopleAdapter;
import com.hyapp.achat.bl.MainViewModel;

public class PeopleFragment extends Fragment {

    private MainViewModel viewModel;
    private FragmentPeopleGroupsBinding binding;

    private PeopleAdapter adapter;
    private boolean isFirstLoaded = false;

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
        binding.statusMessage.tryAgain.setOnClickListener(v -> viewModel.reloadPeople());
        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.reloadPeople());
        setupRecyclerView(requireContext(), view);
        observePeople();
//        observeNet();
    }

    private void setupRecyclerView(Context context, View view) {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new PeopleAdapter(context);
        binding.recyclerView.setAdapter(adapter);
    }

    private void observePeople() {
        viewModel.getPeopleLive().observe(getViewLifecycleOwner(), listResource -> {
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

//    private void observeNet() {
//        viewModel.getNetLive().observe(getViewLifecycleOwner(), aBoolean -> {
//            if (aBoolean) {
//                binding.setIsStatusVisible(false);
//                if (!isFirstLoaded) {
//                    viewModel.initPeople();
//                }
//            } else {
//                binding.setIsStatusVisible(true);
//                onError(MainViewModel.MSG_NET);
//            }
//        });
//    }

    private void onSuccess(Resource<SortedList<People>> resource) {
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

    public void addPeople(Resource<SortedList<People>> resource) {
        if (resource.index == Resource.INDEX_ALL) {
            binding.setIsStatusVisible(false);
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
            isFirstLoaded = true;
            adapter.resetList(resource.data);
        } else {
            adapter.addAt(resource.data, resource.index);
        }
    }

    private void onError(String message) {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.setIsStatusVisible(true);
        binding.progressBar.setVisibility(View.GONE);
        switch (message) {
            case Event.MSG_NET:
                binding.statusMessage.text.setText(R.string.no_network_connection);
                break;
            case Event.MSG_ERROR:
                binding.statusMessage.text.setText(R.string.sorry_an_error_occurred);
                break;
            default:
                binding.statusMessage.text.setText(message);
        }
    }

    private void onLoading() {
        binding.setIsStatusVisible(false);
        binding.progressBar.setVisibility(View.VISIBLE);
    }
}
