package com.hyapp.achat.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import com.hyapp.achat.model.Preferences;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }

        if (Preferences.instance() != null) {
            Pair<Boolean, Boolean> pair = Preferences.instance().getLogged();
            Boolean logged = pair.first;
            if (logged) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Boolean hasRegistered = pair.second;
                if (hasRegistered) {
                    startActivity(new Intent(this, LoginUserActivity.class));
                } else {
                    startActivity(new Intent(this, LoginGuestActivity.class));
                }
            }
        } else {
            startActivity(new Intent(this, LoginGuestActivity.class));
        }
        finish();
    }
}