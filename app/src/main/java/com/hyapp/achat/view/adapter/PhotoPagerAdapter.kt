package com.hyapp.achat.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hyapp.achat.view.fragment.PhotoFragment

class PhotoPagerAdapter(fragmentActivity: FragmentActivity?, private val paths: List<String>) :
    FragmentStateAdapter(
        fragmentActivity!!
    ) {
    private val itemCount: Int
    override fun createFragment(position: Int): Fragment {
        val path = paths[position]
        return PhotoFragment.newInstance(path)
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    init {
        itemCount = paths.size
    }
}