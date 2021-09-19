package com.hyapp.achat.ui;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hyapp.achat.R;

public class BaseActivity extends AppCompatActivity {

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
}
