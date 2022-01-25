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
import com.hyapp.achat.view.ChatActivity.Companion.start
import com.hyapp.achat.view.utils.UiUtils

class RoomAdapter(private val context: Context) :
    ListAdapter<Room, RoomAdapter.Holder>(DIFF_CALLBACK) {

    val membersStr = context.getString(R.string.members)
    val onlineStr = context.getString(R.string.online)

    companion object {
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

    inner class Holder(private val binding: ItemRoomBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        @SuppressLint("SetTextI18n")
        fun bind(room: Room) {
            binding.room = room
            binding.executePendingBindings()

            binding.bio.text =
                "${UiUtils.formatNum(room.memberCount.toLong())} " + membersStr + ", ${UiUtils.formatNum(room.onlineMemberCount.toLong())} " + onlineStr

            binding.avatar.setAvatars(room.avatars)

            when (room.gender) {
                UserConsts.GENDER_MALE -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_user_male_bg)
                UserConsts.GENDER_FEMALE -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_user_female_bg)
                UserConsts.GENDER_MIXED -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_user_mixed_bg)
            }
        }

        override fun onClick(v: View) {
            val room = getItem(adapterPosition)
            start(context, Contact(room!!, membersStr, onlineStr))
        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}