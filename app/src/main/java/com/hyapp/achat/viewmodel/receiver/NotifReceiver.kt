package com.hyapp.achat.viewmodel.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.core.app.RemoteInput
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserLive
import com.hyapp.achat.model.event.ActionEvent
import com.hyapp.achat.model.objectbox.UserDao.get
import com.hyapp.achat.viewmodel.Notifs
import com.hyapp.achat.viewmodel.service.SocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.coroutines.CoroutineContext

class NotifReceiver : BroadcastReceiver(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null) {
            when (action) {
                Notifs.ACTION_EXIT -> handleExitApp(context)
                Notifs.ACTION_REPLY_MESSAGE -> handleReplyMessage(intent)
                Notifs.ACTION_MARK_MESSAGE_AS_READ -> handleMarkMessageAsRead(intent)
            }
        }
    }

    private fun handleExitApp(context: Context) {
        context.stopService(Intent(context, SocketService::class.java))
        EventBus.getDefault().post(ActionEvent(ActionEvent.ACTION_EXIT_APP))
    }

    private fun handleReplyMessage(intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            val text: CharSequence =
                remoteInput.getCharSequence(Notifs.KEY_REPLY_MESSAGE).toString()
            if (!TextUtils.isEmpty(text)) {
                val message: Message? = intent.getParcelableExtra(EXTRA_MESSAGE)
                val contact: Contact? = intent.getParcelableExtra(EXTRA_CONTACT)
                if (message != null && contact != null) {
                    var currentUser = UserLive.value
                    if (currentUser == null) {
                        currentUser = get(User.CURRENT_USER_ID)
                    }
                    val newMessage = Message(
                        UUID.randomUUID().toString(), Message.TYPE_TEXT,
                        Message.TRANSFER_SEND, System.currentTimeMillis(), text.toString(), 0, "",
                        contact.uid, currentUser ?: User()
                    )
                    launch {
                        ChatRepo.sendPvMessage(newMessage, contact.getUser())
                    }
                    launch {
//                        ChatRepo.markMessageAsRead(message)
                    }
                }
            }
        }
    }

    private fun handleMarkMessageAsRead(intent: Intent) {
        val message: Message? = intent.getParcelableExtra(EXTRA_MESSAGE)
        if (message != null) {
            launch {
//                ChatRepo.markMessageAsRead(message)
            }
        }
    }

    companion object {
        const val EXTRA_CONTACT = "contact"
        const val EXTRA_MESSAGE = "message"
    }
}