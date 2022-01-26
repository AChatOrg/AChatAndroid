package com.hyapp.achat.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ItemRoomBinding
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.Room
import com.hyapp.achat.model.entity.UserConsts
import com.hyapp.achat.model.entity.UserLive
import com.hyapp.achat.view.ChatActivity.Companion.start
import com.hyapp.achat.view.EventActivity
import com.hyapp.achat.view.utils.UiUtils

class RoomAdapter(private val context: Context) :
    ListAdapter<Room, RoomAdapter.Holder>(DIFF_CALLBACK) {

    val membersStr = context.getString(R.string.members)
    val onlineStr = context.getString(R.string.online)

    companion object {
        const val PAYLOAD_BIO: Byte = 1

        val DIFF_CALLBACK: DiffUtil.ItemCallback<Room> = object : DiffUtil.ItemCallback<Room>() {
            override fun areItemsTheSame(
                oldItem: Room, newItem: Room
            ): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(
                oldItem: Room, newItem: Room
            ): Boolean {
                return oldItem == newItem
            }

            override fun getChangePayload(oldItem: Room, newItem: Room): Any? {
                return when {
                    oldItem.memberCount != newItem.memberCount -> {
                        PAYLOAD_BIO
                    }
                    else -> {
                        null
                    }
                }
            }
        }
    }

    override fun submitList(list: MutableList<Room>?) {
        if (list != null)
            super.submitList(ArrayList(list))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding: ItemRoomBinding =
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_room, parent, false)
        binding.lifecycleOwner = context as LifecycleOwner
        return Holder(binding)
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

    inner class Holder(private val binding: ItemRoomBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(room: Room) {
            binding.room = room
            binding.executePendingBindings()

            setBio(room)

            binding.avatar.setAvatars(room.avatars)

            when (room.gender) {
                UserConsts.GENDER_MALE -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_user_male_bg)
                UserConsts.GENDER_FEMALE -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_user_female_bg)
                UserConsts.GENDER_MIXED -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_user_mixed_bg)
            }
        }

        open fun bind(room: Room, payloads: List<Any?>) {
            for (payload in payloads) {
                when (payload as Byte) {
                    PAYLOAD_BIO -> setBio(room)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setBio(room: Room) {
            binding.bio.text =
                "${UiUtils.formatNum(room.memberCount.toLong())} " + membersStr + ", ${
                    UiUtils.formatNum(
                        room.onlineMemberCount.toLong()
                    )
                } " + onlineStr
        }

        override fun onClick(v: View) {
            val room = getItem(adapterPosition)
            UserLive.value?.let { user ->
                if (room.gender == UserConsts.GENDER_MIXED || room.gender == user.gender)
                    start(context, Contact(room!!, membersStr, onlineStr))
                else if (context is EventActivity) {
                    val genderStr = when (room.gender) {
                        UserConsts.GENDER_MALE -> context.getString(R.string.male)
                        UserConsts.GENDER_FEMALE -> context.getString(R.string.female)
                        else -> context.getString(R.string.mixed)
                    }
                    context.alert(
                        R.string.rooms,
                        String.format(
                            context.getString(R.string.only_s_can_join_this_room),
                            genderStr
                        )
                    )
                }
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}