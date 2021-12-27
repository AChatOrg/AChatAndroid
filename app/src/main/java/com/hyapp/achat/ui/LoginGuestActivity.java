package com.hyapp.achat.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.hyapp.achat.R;
import com.hyapp.achat.bl.LoginGuestViewModel;
import com.hyapp.achat.databinding.ActivityGuestLoginBinding;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.event.Event;
import com.hyapp.achat.model.event.LoggedEvent;
import com.hyapp.achat.ui.utils.UiUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LoginGuestActivity extends EventActivity {

    private LoginGuestViewModel viewModel;
    private ActivityGuestLoginBinding binding;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setupHistory();
        setupProgressDialog();
        //////////////////
//        ContactDao.put(new Contact(
//                "ali"
//                , "ahmadi"
//                , Person.GENDER_MALE
//                , new Key("aliUid", (byte) 10, 1000, 10000)
//                , new String[]{"avatar1", "avatar2"}
//                , 16000
//                , 17000
//                , Contact.DELIVERY_HIDDEN
//                , "19"
//                , "mediaPath"));
//
//        for (Contact c : ContactDao.getAll()) {
//            Log.e("ssss", c.getId()+" "+c.getName()+" "+c.getBio()+" "+c.getGender()+" "+c.getUid()+" "+c.getLoginTime()+" "+c.getOnlineTime()+" "+c.getMediaMessagePath());
//        }
    }

    @Override
    protected void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }

    private void init() {
        viewModel = new ViewModelProvider(this).get(LoginGuestViewModel.class);
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
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> {
            viewModel.cancelLogin();
            dialog.dismiss();
        });
    }

    private void onSuccess(People people) {
        progressDialog.dismiss();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void onError(String message) {
        progressDialog.dismiss();
        switch (message) {
            case Event.MSG_EMPTY:
                UiUtils.vibrate(this, 200);
                binding.editTextUsername.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
                break;
            case Event.MSG_EXIST:
                alert(R.string.login_guest, R.string.this_user_is_online);
                break;
            case Event.MSG_NET:
                alert(R.string.login_guest, R.string.no_network_connection);
                break;
            case Event.MSG_ERROR:
                alert(R.string.login_guest, R.string.sorry_an_error_occurred);
                break;
            default:
                alert(R.string.login_guest, message);
        }
    }


    private void onLoading() {
        progressDialog.show();
        progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(UiUtils.getStyleColor(this, R.attr.colorPrimary));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onlogged(LoggedEvent event) {
        if (event.action == LoggedEvent.ACTION_ME) {
            switch (event.status) {
                case SUCCESS:
                    onSuccess(event.getPeople());
                    break;
                case ERROR:
                    onError(event.message);
                    break;
                case LOADING:
                    onLoading();
            }
        }
    }
}
