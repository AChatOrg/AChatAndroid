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
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Message
import com.hyapp.achat.model.entity.UserConsts
import com.hyapp.achat.view.ProfileActivity
import com.hyapp.achat.view.component.GroupAvatarView
import com.hyapp.achat.view.component.emojiview.view.AXEmojiTextView
import com.hyapp.achat.view.utils.UiUtils
import com.hyapp.achat.viewmodel.utils.TimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi


class MessageAdapter(val context: Context, val recyclerView: RecyclerView) :
    ListAdapter<Message, MessageAdapter.Holder>(DIFF_CALLBACK) {

    val sp1: Int = UiUtils.sp2px(context, 1F)
    var isLoadingMore = false
    lateinit var onLoadMore: () -> Unit
    var onListChanged: (() -> Unit)? = null

    companion object {
        const val PAYLOAD_BUBBLE: Byte = 0
        const val PAYLOAD_DELIVERY: Byte = 1
        const val PAYLOAD_ONLINE_TIME: Byte = 2
        const val PAYLOAD_BIO: Byte = 3

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Message> =
            object : DiffUtil.ItemCallback<Message>() {
                override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                    return oldItem.uid == newItem.uid
                }

                override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                    return oldItem == newItem
                }

                override fun getChangePayload(oldItem: Message, newItem: Message): Any? {
                    return when {
                        oldItem.delivery != newItem.delivery -> {
                            PAYLOAD_DELIVERY
                        }
                        oldItem.time != newItem.time -> {
                            PAYLOAD_BUBBLE
                        }
                        oldItem.senderOnlineTime != newItem.senderOnlineTime -> {
                            PAYLOAD_ONLINE_TIME
                        }
                        oldItem.senderBio != newItem.senderBio -> {
                            PAYLOAD_BIO
                        }
                        else -> {
                            null
                        }
                    }
                }
            }
    }

    override fun submitList(list: MutableList<Message>?) {
        if (list != null) {
            super.submitList(ArrayList(list))
        }
    }

    fun getMessage(position: Int): Message {
        return getItem(position)
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Message>,
        currentList: MutableList<Message>
    ) {
        onListChanged?.invoke()
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return message.transfer + message.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        when (viewType) {
            Message.TRANSFER_RECEIVE + Message.TYPE_TYPING -> return TypingHolder(
                LayoutInflater.from(context).inflate(R.layout.item_message_typing, parent, false)
            )
            Message.TRANSFER_SEND + Message.TYPE_TEXT -> return TextHolder(
                LayoutInflater.from(
                    context
                ).inflate(R.layout.item_message_text_send, parent, false)
            )
            Message.TRANSFER_RECEIVE + Message.TYPE_TEXT -> return TextHolder(
                LayoutInflater.from(
                    context
                ).inflate(R.layout.item_message_text_receive, parent, false)
            )
            Message.TRANSFER_SEND + Message.TYPE_LOTTIE -> return LottieHolder(
                LayoutInflater.from(
                    context
                ).inflate(R.layout.item_message_lottie_send, parent, false)
            )
            Message.TRANSFER_RECEIVE + Message.TYPE_LOTTIE -> return LottieHolder(
                LayoutInflater.from(
                    context
                ).inflate(R.layout.item_message_lottie_receive, parent, false)
            )
            Message.TRANSFER_RECEIVE + Message.TYPE_DETAILS -> return DetailsHolder(
                LayoutInflater.from(
                    context
                ).inflate(R.layout.item_message_details, parent, false)
            )
            Message.TRANSFER_RECEIVE + Message.TYPE_PROFILE -> return ProfileHolder(
                LayoutInflater.from(context).inflate(R.layout.item_message_profile, parent, false)
            )
        }
        throw RuntimeException("incorrect view type :" + javaClass.name)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
        if (position <= 0 && !isLoadingMore && itemCount > 2) {
            isLoadingMore = true
            if (::onLoadMore.isInitialized) {
                recyclerView.post { onLoadMore() }
            }
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int, payloads: List<Any?>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.bind(getItem(position), payloads)
        }
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        if (holder is LottieHolder) {
            holder.recycle()
        }
    }

//    fun add(list: MessageList, prevChanged: Boolean, addedCount: Int) {
//        messages = list
//        val size = messages.size
//        if (prevChanged)
//            notifyItemChanged(size - 1 - addedCount, PAYLOAD_BUBBLE)
//        notifyItemRangeInserted(size - addedCount, addedCount)
//    }
//
//    fun addPaging(list: MessageList, addedCount: Int, firstChanged: Boolean) {
//        messages = list
//        if (firstChanged)
//            notifyItemChanged(0, PAYLOAD_BUBBLE)
//        notifyItemRangeInserted(0, addedCount)
//    }

    abstract inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)

        open fun bind(message: Message, payloads: List<Any?>) {}
    }

    @ExperimentalCoroutinesApi
    @Suppress("LeakingThis")
    inner class ProfileHolder(itemView: View) : Holder(itemView), View.OnClickListener {
        private val name: TextView = itemView.findViewById(R.id.name)
        private val bio: TextView = itemView.findViewById(R.id.description)
        private val avatar: GroupAvatarView = itemView.findViewById(R.id.avatar)
        private val rank: TextView = itemView.findViewById(R.id.rank)
        private val onlineTime: TextView = itemView.findViewById(R.id.lastOnline)

        override fun bind(message: Message) {
            name.text = message.senderName
            bio.text = message.senderBio
            val contact = message.getContact()
            if (message.isPv) {
                avatar.setAvatars(contact.avatars)
                setOnlineTime(message.senderOnlineTime)
                onlineTime.visibility = View.VISIBLE
            } else {
                avatar.setAvatars(contact.avatars)
                onlineTime.visibility = View.GONE
            }
            val pair = UserConsts.rankInt2rankStrResAndColor(contact.rank)
            rank.setText(pair.first)
            rank.setTextColor(pair.second)
        }

        override fun bind(message: Message, payloads: List<Any?>) {
            super.bind(message, payloads)
            for (payload in payloads) {
                when (payload as Byte) {
                    PAYLOAD_ONLINE_TIME -> setOnlineTime(message.senderOnlineTime)
                    PAYLOAD_BIO -> bio.text = message.senderBio

                }
            }
        }

        private fun setOnlineTime(time: Long) {
            if (time == Contact.TIME_ONLINE) {
                onlineTime.text = ""
                onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_green)
            } else {
                onlineTime.text =
                    TimeUtils.timeAgoShort(System.currentTimeMillis() - time)
                onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_grey)
            }
        }

        override fun onClick(v: View) {
            val message = getItem(adapterPosition)
            ProfileActivity.start(context, message.getContact().getUser())
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    inner class DetailsHolder(itemView: View) : Holder(itemView) {
        override fun bind(message: Message) {
            (itemView as TextView).text = message.text
        }
    }

    @Suppress("LeakingThis")
    abstract inner class ChatHolder(itemView: View) : Holder(itemView), View.OnClickListener {
        protected val avatar: SimpleDraweeView? = itemView.findViewById(R.id.avatar)
        protected val time: TextView = itemView.findViewById(R.id.time)
        protected val delivery: AppCompatImageView? = itemView.findViewById(R.id.read)

        override fun bind(message: Message) {
            setBubble(message)
            setDelivery(message)
            time.text = TimeUtils.millis2DayTime(message.time)
        }

        override fun bind(message: Message, payloads: List<Any?>) {
            super.bind(message, payloads)
            for (payload in payloads) {
                when (payload as Byte) {
                    PAYLOAD_BUBBLE -> setBubble(message)
                    PAYLOAD_DELIVERY -> setDelivery(message)
                }
            }
        }

        private fun setDelivery(message: Message) {
            if (message.transfer == Message.TRANSFER_SEND) {
                when (message.delivery) {
                    Message.DELIVERY_READ -> delivery?.setImageResource(R.drawable.msg_read_contact)
                    Message.DELIVERY_SENT -> delivery?.setImageResource(R.drawable.msg_unread_contact)
                    Message.DELIVERY_WAITING -> delivery?.setImageResource(R.drawable.msg_waiting_contact)
                }
            }
        }

        private fun setBubble(message: Message) {
            val bubble: Byte = message.bubble
            val bubbleView = bubbleView
            bubbleView?.let {
                if (message.transfer == Message.TRANSFER_SEND) {
                    when (bubble) {
                        Message.BUBBLE_END -> it.setBackgroundResource(R.drawable.chat_bubble_send_end)
                        Message.BUBBLE_MIDDLE -> it.setBackgroundResource(R.drawable.chat_bubble_send_middle)
                        Message.BUBBLE_START -> it.setBackgroundResource(R.drawable.chat_bubble_send_start)
                        Message.BUBBLE_SINGLE -> it.setBackgroundResource(R.drawable.chat_bubble_send_single)
                    }
                    if (bubble == Message.BUBBLE_SINGLE || bubble == Message.BUBBLE_END) {
                        time.visibility = View.VISIBLE
                    } else {
                        time.visibility = View.GONE
                    }
                } else {
                    when (bubble) {
                        Message.BUBBLE_END -> it.setBackgroundResource(R.drawable.chat_bubble_receive_end)
                        Message.BUBBLE_MIDDLE -> it.setBackgroundResource(R.drawable.chat_bubble_receive_middle)
                        Message.BUBBLE_START -> it.setBackgroundResource(R.drawable.chat_bubble_receive_start)
                        Message.BUBBLE_SINGLE -> it.setBackgroundResource(R.drawable.chat_bubble_receive_single)
                    }
                    if (bubble == Message.BUBBLE_SINGLE || bubble == Message.BUBBLE_END) {
                        avatar?.visibility = View.VISIBLE
                        time.visibility = View.VISIBLE
                        avatar?.setImageURI(message.firstSenderAvatar)
                    } else {
                        avatar?.visibility = View.GONE
                        time.visibility = View.GONE
                    }
                }
            }
        }

        protected abstract val bubbleView: View?

        override fun onClick(v: View) {
            val bubble: Byte = getItem(adapterPosition).bubble
            if (bubble != Message.BUBBLE_SINGLE && bubble != Message.BUBBLE_END && bubble != Message.BUBBLE_SINGLE && bubble != Message.BUBBLE_END) {
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
            val text = message.text
            textView.text = text
            val sizes = message.setAndGetTextSizes(sp1)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizes.first)
            textView.setEmojiSize(sizes.second)
        }

        override val bubbleView: View
            get() = textView

    }

    inner class LottieHolder(itemView: View) : ChatHolder(itemView) {
        private val lottieImageView: AXrLottieImageView =
            itemView.findViewById(R.id.lottieImageView)

        override fun bind(message: Message) {
            super.bind(message)
//            lottieImageView.lottieDrawable = (message as LottieMessage).drawable
//            lottieImageView.playAnimation()
        }

        fun recycle() {
//            lottieImageView.stopAnimation()
        }

        override val bubbleView: View?
            get() = null

    }

    inner class TypingHolder(itemView: View) : Holder(itemView) {
        val avatar = itemView.findViewById<SimpleDraweeView>(R.id.avatar)

        override fun bind(message: Message) {
            val ava = message.senderAvatars
            avatar.setImageURI(if (ava.isNotEmpty()) ava[0] else null)
        }
    }
}