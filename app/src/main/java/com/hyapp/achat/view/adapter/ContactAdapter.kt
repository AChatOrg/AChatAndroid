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
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.hyapp.achat.R
import com.hyapp.achat.viewmodel.utils.TimeUtils
import com.hyapp.achat.databinding.ItemContactGroupBinding
import com.hyapp.achat.databinding.ItemContactSingleBinding
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.ContactList
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.Resource
import com.hyapp.achat.view.ChatActivity
import java.lang.RuntimeException

class ContactAdapter(private val context: Context)
    : RecyclerView.Adapter<ContactAdapter.Holder>() {

    private var contacts: List<Contact> = ContactList()

    override fun getItemViewType(position: Int): Int {
        return contacts[position].type.toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        if (viewType.toByte() == Contact.TYPE_SINGLE) {
            val binding: ItemContactSingleBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_contact_single, parent, false)
            binding.lifecycleOwner = context as LifecycleOwner
            return SingleHolder(binding)
        }
        if (viewType.toByte() == Contact.TYPE_GROUP) {
            val binding: ItemContactGroupBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_contact_group, parent, false)
            binding.lifecycleOwner = context as LifecycleOwner
            return GroupHolder(binding)
        }
        throw RuntimeException("ContactAdapter: incorrect viewType: " + javaClass.name)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetList(list: List<Contact>) {
        contacts = list
        notifyDataSetChanged()
    }

    fun putFirst(list: List<Contact>, oldIndex: Int) {
        contacts = list
        if (oldIndex != Resource.INDEX_NEW) {
            notifyItemRemoved(oldIndex)
        }
        notifyItemInserted(0)
    }

    @Suppress("LeakingThis")
    open inner class Holder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val media: SimpleDraweeView = view.findViewById(R.id.media)
        private val messageDelivery: ImageView = view.findViewById(R.id.messageDelivery)
        private val messageTime: TextView = view.findViewById(R.id.time)
        private val notif: TextView = view.findViewById(R.id.notif)

        init {
            itemView.setOnClickListener(this)
        }

        open fun bind(contact: Contact) {
            val mediaPath = contact.mediaPath
            if (mediaPath != "") {
                media.visibility = View.VISIBLE
                media.setImageURI(mediaPath)
            } else {
                media.visibility = View.GONE
            }

            messageTime.text = TimeUtils.millis2DayTime(contact.messageTime)

            if (contact.messageDelivery == Message.DELIVERY_HIDDEN) {
                messageDelivery.visibility = View.GONE
            } else {
                messageDelivery.visibility = View.VISIBLE
                when (contact.messageDelivery) {
                    Message.DELIVERY_READ -> messageDelivery.setImageResource(R.drawable.msg_read_contact)
                    Message.DELIVERY_UNREAD -> messageDelivery.setImageResource(R.drawable.msg_unread_contact)
                    Message.DELIVERY_WAITING -> messageDelivery.setImageResource(R.drawable.msg_waiting_contact)
                }
            }

            notif.visibility = if (contact.notifCount == "0") View.GONE else View.VISIBLE
        }

        override fun onClick(p0: View?) {
            ChatActivity.start(context, contacts[adapterPosition])
        }

    }

    inner class SingleHolder(private val binding: ItemContactSingleBinding)
        : Holder(binding.root) {

        override fun bind(contact: Contact) {
            super.bind(contact)

            binding.contact = contact
            binding.executePendingBindings()

            val avatars = contact.avatars
            binding.avatarDraweeView.setImageURI(if (avatars.isNotEmpty()) avatars[0] else null)


            if (contact.onlineTime == Contact.TIME_ONLINE) {
                binding.onlineTime.text = ""
                binding.onlineTime.setBackgroundResource(R.drawable.last_online_contact_bg_green)
            } else {
                binding.onlineTime.text = TimeUtils.timeAgoShort(System.currentTimeMillis() - contact.onlineTime)
                binding.onlineTime.setBackgroundResource(R.drawable.last_online_contact_bg_grey)
            }
        }
    }

    inner class GroupHolder(private val binding: ItemContactGroupBinding)
        : Holder(binding.root) {

        override fun bind(contact: Contact) {
            super.bind(contact)

            binding.contact = contact
            binding.executePendingBindings()

            binding.groupAvatarView.setAvatars(contact.avatars)
        }
    }
}