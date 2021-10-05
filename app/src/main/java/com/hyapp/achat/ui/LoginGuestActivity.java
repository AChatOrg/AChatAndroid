package com.hyapp.achat.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.hyapp.achat.R;
import com.hyapp.achat.databinding.ActivityGuestLoginBinding;
import com.hyapp.achat.model.People;
import com.hyapp.achat.ui.utils.UiUtils;
import com.hyapp.achat.viewmodel.LoginGuestViewModel;

public class LoginGuestActivity extends BaseActivity {

    private LoginGuestViewModel viewModel;
    private ActivityGuestLoginBinding binding;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setupHistory();
        setupProgressDialog();
        observeUser();
    }

    @Override
    protected void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }

    private void init() {
        viewModel = new ViewModelProvider(this).get(LoginGuestViewModel.class);
        viewModel.init();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guest_login);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
    }

    private void setupHistory() {
        String[] nameHistory = viewModel.getNameHistory();
        String[] bioHistory = viewModel.getBioHistory();
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this, R.layout.item_suggestion, nameHistory);
        ArrayAdapter<String> bioAdapter = new ArrayAdapter<>(this, R.layout.item_suggestion, bioHistory);
        binding.editTextUsername.setThreshold(1);
        binding.editTextBio.setThreshold(1);
        binding.editTextUsername.setAdapter(nameAdapter);
        binding.editTextBio.setAdapter(bioAdapter);
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this, R.style.RoundedCornersDialog);
        progressDialog.setTitle(R.string.login_guest);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    private void observeUser() {
        viewModel.getUserLive().observe(this, userResource -> {
            switch (userResource.status) {
                case SUCCESS:
                    onSuccess(userResource.data);
                    break;
                case ERROR:
                    onError(userResource.message);
                    break;
                case LOADING:
                    onLoading();
            }
        });
    }

    private void onSuccess(People people) {
        progressDialog.dismiss();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void onError(String message) {
        progressDialog.dismiss();
        switch (message) {
            case LoginGuestViewModel.MSG_EMPTY:
                UiUtils.vibrate(this, 200);
                binding.editTextUsername.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
                break;
            case LoginGuestViewModel.MSG_EXIST:
                alert(R.string.login_guest, R.string.this_user_is_online);
                break;
            case LoginGuestViewModel.MSG_NET:
                alert(R.string.login_guest, R.string.no_network_connection);
                break;
            case LoginGuestViewModel.MSG_ERROR:
                alert(R.string.login_guest, R.string.sorry_an_error_occurred);
                break;
            default:
                alert(R.string.login_guest, message);
        }
    }


    private void onLoading() {
        progressDialog.show();
    }
}
