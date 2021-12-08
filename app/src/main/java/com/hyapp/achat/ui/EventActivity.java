package com.hyapp.achat.ui;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hyapp.achat.R;
import com.hyapp.achat.model.event.Event;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EventActivity extends AppCompatActivity {

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

    protected void alert(@StringRes int titleRes, String message) {
        new AlertDialog.Builder(this, R.style.RoundedCornersDialog)
                .setTitle(titleRes)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    protected void alert(@StringRes int titleRes, @StringRes int messageRes) {
        alert(titleRes, getString(messageRes));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExitApp(Event event) {
        if (event.action == Event.ACTION_EXIT_APP) {
            finishAffinity();
        }
    }
}
