package com.hyapp.achat.ui.adapter

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
import com.hyapp.achat.bl.utils.TimeUtils
import com.hyapp.achat.databinding.ItemContactGroupBinding
import com.hyapp.achat.databinding.ItemContactSingleBinding
import com.hyapp.achat.model.*
import com.hyapp.achat.model.utils.MessageUtils
import com.hyapp.achat.model.utils.PersonUtils
import java.lang.RuntimeException
import java.util.*

class ContactAdapter(private val context: Context)
    : RecyclerView.Adapter<ContactAdapter.Holder>() {

    private var contacts: List<Contact> = LinkedList()

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

    open class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val media: SimpleDraweeView = view.findViewById(R.id.media)
        private val messageDelivery: ImageView = view.findViewById(R.id.messageDelivery)
        private val messageTime: TextView = view.findViewById(R.id.time)
        private val notif: TextView = view.findViewById(R.id.notif)

        open fun bind(contact: Contact) {
            val mediaPath = contact.mediaMessagePath
            if (mediaPath != null) {
                media.visibility = View.VISIBLE
                media.setImageURI(mediaPath)
            } else {
                media.visibility = View.GONE
            }

            messageTime.text = TimeUtils.millis2DayTime(contact.messageTime)

            if (contact.messageDelivery == ChatMessage.DELIVERY_HIDDEN) {
                messageDelivery.visibility = View.GONE
            } else {
                messageDelivery.visibility = View.VISIBLE
                when (contact.messageDelivery) {
                    ChatMessage.DELIVERY_READ -> messageDelivery.setImageResource(R.drawable.msg_read_contact)
                    ChatMessage.DELIVERY_UNREAD -> messageDelivery.setImageResource(R.drawable.msg_unread_contact)
                    ChatMessage.DELIVERY_WAITING -> messageDelivery.setImageResource(R.drawable.msg_waiting_contact)
                }
            }

            notif.visibility = if (contact.notifCount == null) View.GONE else View.VISIBLE
        }

    }

    class SingleHolder(private val binding: ItemContactSingleBinding)
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

    class GroupHolder(private val binding: ItemContactGroupBinding)
        : Holder(binding.root) {

        override fun bind(contact: Contact) {
            super.bind(contact)

            binding.contact = contact
            binding.executePendingBindings()

            binding.groupAvatarView.setAvatars(*contact.avatars)
        }
    }
}