package com.hyapp.achat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hyapp.achat.Config
import com.hyapp.achat.R
import com.hyapp.achat.databinding.ItemUserBinding
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserConsts
import com.hyapp.achat.view.ChatActivity.Companion.start

class UserAdapter(private val context: Context) :
    ListAdapter<User, UserAdapter.Holder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<User> = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(
                oldItem: User, newItem: User
            ): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(
                oldItem: User, newItem: User
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun submitList(list: MutableList<User>?) {
        if (list != null)
            super.submitList(ArrayList(list))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding: ItemUserBinding =
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_user, parent, false)
        binding.lifecycleOwner = context as LifecycleOwner
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        fun bind(user: User) {
            binding.user = user
            binding.executePendingBindings()
            val avatars: List<String> = user.avatars
            binding.avatar.setImageURI(if (avatars.isNotEmpty()) Config.SERVER_URL + avatars[0] else null)
            when (user.gender) {
                UserConsts.GENDER_MALE -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_user_male_bg)
                UserConsts.GENDER_FEMALE -> binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_user_female_bg)
            }
            val pair = UserConsts.rankInt2rankStrResAndColor(user.rank)
            binding.rank.setText(pair.first)
            binding.rank.setTextColor(pair.second)
        }

        override fun onClick(v: View) {
            val user = getItem(adapterPosition)
            start(context, Contact(user!!))
        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}