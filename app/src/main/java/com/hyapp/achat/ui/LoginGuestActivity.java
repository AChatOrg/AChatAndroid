package com.hyapp.achat.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hyapp.achat.viewmodel.LoginGuestViewModel;

public class LoginGuestActivity extends BaseActivity {

    private LoginGuestViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginGuestViewModel.class);
    }
}
