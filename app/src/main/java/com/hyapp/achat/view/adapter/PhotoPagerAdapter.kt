package com.hyapp.achat.view.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hyapp.achat.view.fragment.PhotoFragment

class PhotoPagerAdapter(
    fragmentActivity: FragmentActivity?,
    private val paths: MutableList<String>
) :
    FragmentStateAdapter(
        fragmentActivity!!
    ) {

    private val pageIds = paths.map { it.hashCode().toLong() }

    override fun createFragment(position: Int): Fragment {
        val path = paths[position]
        return PhotoFragment.newInstance(path)
    }

    override fun getItemCount(): Int {
        return paths.size
    }

    override fun getItemId(position: Int): Long {
        return paths[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return pageIds.contains(itemId)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun remove(path: String) {
        val index = paths.indexOf(path)
        paths.removeAt(index)
        notifyItemRangeChanged(index, paths.size)
        notifyDataSetChanged()
    }
}