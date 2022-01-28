package com.hyapp.achat.view

import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hyapp.achat.R
import com.hyapp.achat.model.event.ActionEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class EventActivity : AppCompatActivity() {
    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        startedActivities++
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        startedActivities--
    }

    fun alert(
        @StringRes titleRes: Int,
        message: String,
        cancelable: Boolean = true,
        listener: DialogInterface.OnClickListener? = null
    ) {
        AlertDialog.Builder(this, R.style.RoundedCornersDialog)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(R.string.ok, listener)
            .setCancelable(cancelable)
            .show()
    }

    fun alert(
        @StringRes titleRes: Int,
        @StringRes messageRes: Int,
        cancelable: Boolean = true,
        listener: DialogInterface.OnClickListener? = null
    ) {
        alert(titleRes, getString(messageRes), cancelable, listener)
    }

    fun yesNoAlert(
        @StringRes titleRes: Int,
        message: String,
        cancelable: Boolean = true,
        listener: DialogInterface.OnClickListener? = null
    ) {
        AlertDialog.Builder(this, R.style.RoundedCornersDialog)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(R.string.yes, listener)
            .setNegativeButton(R.string.no, null)
            .setCancelable(cancelable)
            .show()
    }

    fun yesNoAlert(
        @StringRes titleRes: Int,
        @StringRes messageRes: Int,
        cancelable: Boolean = true,
        listener: DialogInterface.OnClickListener? = null
    ) {
        yesNoAlert(titleRes, getString(messageRes), cancelable, listener)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onExitApp(event: ActionEvent) {
        if (event.action == ActionEvent.ACTION_EXIT_APP) {
            finishAffinity()
        }
    }

    companion object {
        var startedActivities: Byte = 0
    }
}