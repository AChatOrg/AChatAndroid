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
import com.hyapp.achat.databinding.ItemUserProfileBinding
import com.hyapp.achat.model.entity.Contact
import com.hyapp.achat.model.entity.User
import com.hyapp.achat.model.entity.UserConsts
import com.hyapp.achat.view.ProfileActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class UserProfileAdapter(private val context: Context) :
    ListAdapter<User, UserProfileAdapter.Holder>(DIFF_CALLBACK) {

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

    override fun submitList(list: List<User>?) {
        if (list != null)
            super.submitList(ArrayList(list))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding: ItemUserProfileBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.item_user_profile,
            parent,
            false
        )
        binding.lifecycleOwner = context as LifecycleOwner
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(private val binding: ItemUserProfileBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        fun bind(user: User) {

            binding.user = user
            binding.executePendingBindings()

            val avatars: List<String> = user.avatars
            binding.avatar.setImageURI(if (avatars.isNotEmpty()) /*Config.SERVER_URL +*/ avatars[0] else null)

            val pair = UserConsts.rankInt2rankStrResAndColor(user.rank)
            binding.rank.setText(pair.first)
            binding.rank.setTextColor(pair.second)
        }

        override fun onClick(v: View) {
            val user = getItem(adapterPosition)
            ProfileActivity.start(context, user)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}