package com.hyapp.achat.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hyapp.achat.view.fragment.PhotoFragment;

import java.util.List;

public class PhotoPagerAdapter extends FragmentStateAdapter {

    private final List<String> paths;
    private final int itemCount;

    public PhotoPagerAdapter(FragmentActivity fragmentActivity, List<String> paths) {
        super(fragmentActivity);
        this.paths = paths;
        itemCount = paths.size();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String path = paths.get(position);
        return PhotoFragment.newInstance(path);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }
}
