package com.hyapp.achat.view

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hyapp.achat.R
import com.hyapp.achat.model.event.ActionEvent
import com.hyapp.achat.view.utils.UiUtils
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
        val dialog = AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setMessage(message)
            .setPositiveButton(R.string.ok, listener)
            .setCancelable(cancelable)
            .show()
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        dialog.window?.setBackgroundDrawableResource(R.drawable.rect_round_white_8dp)
        dialog.window?.setLayout(
            (rect.width() * 0.8F).toInt(),
            dialog.window?.attributes?.height ?: UiUtils.dp2px(this, 150F)
        )
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
        val dialog = AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setMessage(message)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes, listener)
            .setCancelable(cancelable)
            .show()
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        dialog.window?.setBackgroundDrawableResource(R.drawable.rect_round_white_8dp)
        dialog.window?.setLayout(
            (rect.width() * 0.8F).toInt(),
            dialog.window?.attributes?.height ?: UiUtils.dp2px(this, 150F)
        )
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