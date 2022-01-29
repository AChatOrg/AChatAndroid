package com.hyapp.achat.view.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hyapp.achat.R;

import org.greenrobot.eventbus.EventBus;

import me.relex.photodraweeview.OnPhotoTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

public class PhotoFragment extends Fragment {

    public static final String EXTRA_URI = "uri";

    private String path;

    public static PhotoFragment newInstance(String path) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_URI, path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            path = args.getString(EXTRA_URI);
        }
        if (path == null) {
            path = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_drawee_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPhoto(view);
    }

    private void setupPhoto(View view) {
        final PhotoDraweeView photo = (PhotoDraweeView) view;
        photo.setPhotoUri(Uri.parse(path));
        photo.setOnPhotoTapListener((view1, v, v1) -> EventBus.getDefault().post((OnPhotoTapListener) (view2, v2, v11) -> {
        }));
    }
}
