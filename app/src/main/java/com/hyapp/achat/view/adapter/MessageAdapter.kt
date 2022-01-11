package com.hyapp.achat.view.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aghajari.rlottie.AXrLottieImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.hyapp.achat.R
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.utils.PersonUtils
import com.hyapp.achat.view.component.GroupAvatarView
import com.hyapp.achat.view.component.emojiview.view.AXEmojiTextView
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.utils.TimeUtils
import java.lang.RuntimeException

class MessageAdapter(val context: Context) : ListAdapter<Message, MessageAdapter.Holder>(DiffCallback) {

    val sp1: Int = UiUtils.sp2px(context, 1F)

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return message.transfer + message.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        when (viewType) {
            Message.TRANSFER_SEND + Message.TYPE_TEXT -> return TextHolder(LayoutInflater.from(context).inflate(R.layout.item_message_text_send, parent, false))
            Message.TRANSFER_RECEIVE + Message.TYPE_TEXT -> return TextHolder(LayoutInflater.from(context).inflate(R.layout.item_message_text_receive, parent, false))
            Message.TRANSFER_SEND + Message.TYPE_LOTTIE -> return LottieHolder(LayoutInflater.from(context).inflate(R.layout.item_message_lottie_send, parent, false))
            Message.TRANSFER_RECEIVE + Message.TYPE_LOTTIE -> return LottieHolder(LayoutInflater.from(context).inflate(R.layout.item_message_lottie_receive, parent, false))
            Message.TRANSFER_RECEIVE + Message.TYPE_DETAILS -> return DetailsHolder(LayoutInflater.from(context).inflate(R.layout.item_message_details, parent, false))
            Message.TRANSFER_RECEIVE + Message.TYPE_PROFILE -> return SingleProfileHolder(LayoutInflater.from(context).inflate(R.layout.item_message_profile, parent, false))
        }
        throw RuntimeException("incorrect view type :" + javaClass.name)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        if (holder is LottieHolder) {
            holder.recycle()
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.same(newItem)
        }
    }

    abstract inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)
    }

    @Suppress("LeakingThis")
    abstract inner class ProfileHolder(itemView: View) : Holder(itemView), View.OnClickListener {
        private val name: TextView = itemView.findViewById(R.id.name)
        private val description: TextView = itemView.findViewById(R.id.description)

        override fun bind(message: Message) {
            val profileMessage = message as ProfileMessage
            name.text = profileMessage.contact.name
            description.text = profileMessage.contact.bio
        }

        override fun onClick(v: View) {
            //todo go to profile page
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    inner class SingleProfileHolder(itemView: View) : ProfileHolder(itemView) {
        private val avatar: GroupAvatarView = itemView.findViewById(R.id.avatar)
        private val rank: TextView = itemView.findViewById(R.id.rank)
        private val onlineTime: TextView = itemView.findViewById(R.id.lastOnline)

        override fun bind(message: Message) {
            super.bind(message)
            val profileMessage = message as ProfileMessage
            val contact = profileMessage.contact
            if (profileMessage.contact.type == Contact.TYPE_SINGLE) {
                val avatars = contact.avatars
                avatar.setAvatars(if (avatars.isNotEmpty()) avatars[0] else null)
                if (contact.onlineTime == Contact.TIME_ONLINE) {
                    onlineTime.text = ""
                    onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_green)
                } else {
                    onlineTime.text = TimeUtils.timeAgoShort(System.currentTimeMillis() - contact.onlineTime)
                    onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_grey)
                }
                onlineTime.visibility = View.VISIBLE
            } else {
                avatar.setAvatars(*contact.avatars)
                onlineTime.visibility = View.GONE
            }
            val pair = PersonUtils.rankInt2rankStrResAndColor(contact.rank)
            rank.setText(pair.first)
            rank.setTextColor(pair.second)
        }
    }

    inner class DetailsHolder(itemView: View) : Holder(itemView) {
        override fun bind(message: Message) {
            (itemView as TextView).text = (message as DetailsMessage).details
        }
    }

    @Suppress("LeakingThis")
    abstract inner class ChatHolder(itemView: View) : Holder(itemView), View.OnClickListener {
        protected val avatar: SimpleDraweeView? = itemView.findViewById(R.id.avatar)
        protected val time: TextView = itemView.findViewById(R.id.time)
        protected val delivery: AppCompatImageView = itemView.findViewById(R.id.read)
        protected val online: View? = itemView.findViewById(R.id.lastOnline)

        override fun bind(message: Message) {
            val chatMessage = message as ChatMessage
            time.text = TimeUtils.millis2DayTime(chatMessage.time)

            val bubble: Byte = chatMessage.bubble
            val bubbleView = bubbleView
            bubbleView?.let {
                if (message.transfer == Message.TRANSFER_SEND) {
                    when (bubble) {
                        ChatMessage.BUBBLE_END -> it.setBackgroundResource(R.drawable.chat_bubble_send_end)
                        ChatMessage.BUBBLE_MIDDLE -> it.setBackgroundResource(R.drawable.chat_bubble_send_middle)
                        ChatMessage.BUBBLE_START -> it.setBackgroundResource(R.drawable.chat_bubble_send_start)
                        ChatMessage.BUBBLE_SINGLE -> it.setBackgroundResource(R.drawable.chat_bubble_send_single)
                    }
                    if (bubble == ChatMessage.BUBBLE_SINGLE || bubble == ChatMessage.BUBBLE_END) {
                        time.visibility = View.VISIBLE
                    } else {
                        time.visibility = View.GONE
                    }
                    when (chatMessage.delivery) {
                        ChatMessage.DELIVERY_READ -> delivery.setImageResource(R.drawable.msg_read_contact)
                        ChatMessage.DELIVERY_UNREAD -> delivery.setImageResource(R.drawable.msg_unread_contact)
                        ChatMessage.DELIVERY_WAITING -> delivery.setImageResource(R.drawable.msg_waiting_contact)
                    }
                } else {
                    when (bubble) {
                        ChatMessage.BUBBLE_END -> it.setBackgroundResource(R.drawable.chat_bubble_receive_end)
                        ChatMessage.BUBBLE_MIDDLE -> it.setBackgroundResource(R.drawable.chat_bubble_receive_middle)
                        ChatMessage.BUBBLE_START -> it.setBackgroundResource(R.drawable.chat_bubble_receive_start)
                        ChatMessage.BUBBLE_SINGLE -> it.setBackgroundResource(R.drawable.chat_bubble_receive_single)
                    }
                    if (bubble == ChatMessage.BUBBLE_SINGLE || bubble == ChatMessage.BUBBLE_END) {
                        avatar?.visibility = View.VISIBLE
                        time.visibility = View.VISIBLE
                        val avatars = chatMessage.sender.avatars
                        avatar?.setImageURI(if (avatars.isNotEmpty()) avatars[0] else null)
                        online?.visibility = if (chatMessage.sender.onlineTime == Contact.TIME_ONLINE) View.VISIBLE else View.GONE
                    } else {
                        avatar?.visibility = View.GONE
                        time.visibility = View.GONE
                    }
                }
            }
        }

        protected abstract val bubbleView: View?

        override fun onClick(v: View) {
            val bubble: Byte = (getItem(adapterPosition) as ChatMessage).bubble
            if (bubble != ChatMessage.BUBBLE_SINGLE && bubble != ChatMessage.BUBBLE_END && bubble != ChatMessage.BUBBLE_SINGLE && bubble != ChatMessage.BUBBLE_END) {
                time.visibility = if (time.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    inner class TextHolder(itemView: View) : ChatHolder(itemView) {
        private val textView: AXEmojiTextView = itemView.findViewById(R.id.text)
        override fun bind(message: Message) {
            super.bind(message)
            val textMessage = message as TextMessage
            val text = textMessage.text
            textView.text = text
            val sizes = textMessage.setAndGetTextSizes(sp1)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizes.first)
            textView.setEmojiSize(sizes.second)
        }

        override val bubbleView: View?
            get() = textView

    }

    inner class LottieHolder(itemView: View) : ChatHolder(itemView) {
        private val lottieImageView: AXrLottieImageView = itemView.findViewById(R.id.lottieImageView)
        override fun bind(message: Message) {
            super.bind(message)
            lottieImageView.lottieDrawable = (message as LottieMessage).drawable
            lottieImageView.playAnimation()
        }

        fun recycle() {
            lottieImageView.stopAnimation()
        }

        override val bubbleView: View?
            get() = null

    }
}