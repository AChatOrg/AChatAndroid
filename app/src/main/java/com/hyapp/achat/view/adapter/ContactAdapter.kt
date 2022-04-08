package com.hyapp.achat.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.hyapp.achat.Config
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ItemContactGroupBinding
import com.hyapp.achat.databinding.ItemContactSingleBinding
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.view.ChatActivity
import com.hyapp.achat.viewmodel.utils.TimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ContactAdapter(private val context: Context) :
    ListAdapter<Contact, ContactAdapter.Holder>(DIFF_CALLBACK) {

    val typingStr = context.getString(R.string.typing_three_dots)

    companion object {
        const val PAYLOAD_MESSAGE: Byte = 1
        const val PAYLOAD_ONLINE_TIME: Byte = 2
        const val PAYLOAD_AVATAR: Byte = 3

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Contact> =
            object : DiffUtil.ItemCallback<Contact>() {
                override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                    return oldItem.uid == newItem.uid
                }

                override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                    return oldItem.same(newItem)
                }

                override fun getChangePayload(oldItem: Contact, newItem: Contact): Any? {
                    return when {
                        oldItem.message != newItem.message
                                || oldItem.typingName != newItem.typingName
                                || oldItem.messageDelivery != newItem.messageDelivery
                                || oldItem.messageTime != newItem.messageTime
                                || oldItem.notifCount != newItem.notifCount
                                || oldItem.mediaPath != newItem.mediaPath -> {
                            PAYLOAD_MESSAGE
                        }
                        oldItem.onlineTime != newItem.onlineTime -> {
                            PAYLOAD_ONLINE_TIME
                        }
                        oldItem.avatars != newItem.avatars -> {
                            PAYLOAD_AVATAR
                        }
                        else -> {
                            null
                        }
                    }
                }
            }
    }

    override fun submitList(list: MutableList<Contact>?) {
        if (list != null) {
            super.submitList(ArrayList(list))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        if (viewType.toByte() == Contact.TYPE_USER) {
            val binding: ItemContactSingleBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_contact_single,
                parent,
                false
            )
            binding.lifecycleOwner = context as LifecycleOwner
            return SingleHolder(binding)
        }
        if (viewType.toByte() == Contact.TYPE_ROOM) {
            val binding: ItemContactGroupBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_contact_group,
                parent,
                false
            )
            binding.lifecycleOwner = context as LifecycleOwner
            return GroupHolder(binding)
        }
        throw RuntimeException("ContactAdapter: incorrect viewType: " + javaClass.name)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: Holder, position: Int, payloads: List<Any?>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.bind(getItem(position), payloads)
        }
    }

    @Suppress("LeakingThis")
    open inner class Holder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val message: TextView = view.findViewById(R.id.message)
        private val media: SimpleDraweeView = view.findViewById(R.id.media)
        private val messageDelivery: ImageView = view.findViewById(R.id.messageDelivery)
        private val messageTime: TextView = view.findViewById(R.id.time)
        private val notif: TextView = view.findViewById(R.id.notif)

        init {
            itemView.setOnClickListener(this)
        }

        open fun bind(contact: Contact) {
            setMessage(contact)
        }

        open fun bind(contact: Contact, payloads: List<Any?>) {
            for (payload in payloads) {
                when (payload as Byte) {
                    PAYLOAD_MESSAGE -> setMessage(contact)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setMessage(contact: Contact) {
            if (contact.typingName != null) {
                message.text = "${contact.typingName} $typingStr"
                media.visibility = View.GONE
                messageDelivery.visibility = View.GONE
            } else {
                message.text = contact.message
                val mediaPath = contact.mediaPath
                if (mediaPath != "") {
                    media.visibility = View.VISIBLE
                    media.setImageURI(mediaPath)
                } else {
                    media.visibility = View.GONE
                }
                if (contact.messageDelivery == Message.DELIVERY_HIDDEN) {
                    messageDelivery.visibility = View.GONE
                } else {
                    messageDelivery.visibility = View.VISIBLE
                    when (contact.messageDelivery) {
                        Message.DELIVERY_READ -> messageDelivery.setImageResource(R.drawable.msg_read_contact)
                        Message.DELIVERY_SENT -> messageDelivery.setImageResource(R.drawable.msg_unread_contact)
                        Message.DELIVERY_WAITING -> messageDelivery.setImageResource(R.drawable.msg_waiting_contact)
                    }
                }
            }
            messageTime.text = TimeUtils.millis2DayTime(contact.messageTime)
            notif.visibility = if (contact.notifCount == "0") {
                View.GONE
            } else {
                notif.text = contact.notifCount
                View.VISIBLE
            }
        }


        override fun onClick(p0: View?) {
            ChatActivity.start(context, getItem(adapterPosition))
        }

    }

    inner class SingleHolder(private val binding: ItemContactSingleBinding) : Holder(binding.root) {

        override fun bind(contact: Contact) {
            super.bind(contact)

            binding.contact = contact
            binding.executePendingBindings()

            setAvatar(contact)
            setOnlineTime(contact)
        }

        override fun bind(contact: Contact, payloads: List<Any?>) {
            super.bind(contact, payloads)
            for (payload in payloads) {
                when (payload as Byte) {
                    PAYLOAD_ONLINE_TIME -> setOnlineTime(contact)
                    PAYLOAD_AVATAR -> setAvatar(contact)
                }
            }
        }

        private fun setAvatar(contact: Contact) {
            val avatars = contact.avatars
            binding.avatarDraweeView.setImageURI(if (avatars.isNotEmpty()) Config.SERVER_URL + avatars[0] else null)
        }

        private fun setOnlineTime(contact: Contact) {
            if (contact.onlineTime == Contact.TIME_ONLINE) {
                binding.onlineTime.text = ""
                binding.onlineTime.setBackgroundResource(R.drawable.last_online_contact_bg_green)
            } else {
                binding.onlineTime.text =
                    TimeUtils.timeAgoShort(System.currentTimeMillis() - contact.onlineTime)
                binding.onlineTime.setBackgroundResource(R.drawable.last_online_contact_bg_grey)
            }
        }
    }

    inner class GroupHolder(private val binding: ItemContactGroupBinding) : Holder(binding.root) {

        override fun bind(contact: Contact) {
            super.bind(contact)

            binding.contact = contact
            binding.executePendingBindings()

            setAvatar(contact)
        }

        override fun bind(contact: Contact, payloads: List<Any?>) {
            super.bind(contact, payloads)
            for (payload in payloads) {
                when (payload as Byte) {
                    PAYLOAD_AVATAR -> setAvatar(contact)
                }
            }
        }

        private fun setAvatar(contact: Contact) {
            binding.groupAvatarView.setAvatars(contact.avatars, false)
        }
    }
}