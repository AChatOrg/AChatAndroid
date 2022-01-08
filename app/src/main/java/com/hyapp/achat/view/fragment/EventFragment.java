package com.hyapp.achat.view.fragment;

import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;

public abstract class EventFragment extends Fragment {

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
