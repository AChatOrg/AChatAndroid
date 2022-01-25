package com.hyapp.achat.view;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hyapp.achat.R;
import com.hyapp.achat.model.event.ActionEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EventActivity extends AppCompatActivity {

    public static byte startedActivities = 0;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        startedActivities++;
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        startedActivities--;
    }

    public void alert(@StringRes int titleRes, String message) {
        new AlertDialog.Builder(this, R.style.RoundedCornersDialog)
                .setTitle(titleRes)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    public void alert(@StringRes int titleRes, @StringRes int messageRes) {
        alert(titleRes, getString(messageRes));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExitApp(ActionEvent event) {
        if (event.getAction() == ActionEvent.ACTION_EXIT_APP) {
            finishAffinity();
        }
    }
}
