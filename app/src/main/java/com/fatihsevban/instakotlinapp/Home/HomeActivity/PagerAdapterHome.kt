package com.fatihsevban.instakotlinapp.Home.HomeActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.fatihsevban.instakotlinapp.Home.Camera.FragmentHomeCamera
import com.fatihsevban.instakotlinapp.Home.Chatting.FragmentHomeChats
import com.fatihsevban.instakotlinapp.Home.Main.FragmentHomeMain

class PagerAdapterHome(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {

    // constants
    val currentItemPosition = 1

    // properties
    val fragmentHomeCamera = FragmentHomeCamera()
    val fragmentHomeMain = FragmentHomeMain()
    val fragmentHomeContacts = FragmentHomeChats()


    override fun getItem(position: Int): Fragment {

        when(position) {

            0 -> return fragmentHomeCamera
            1 -> return fragmentHomeMain
            2 -> return fragmentHomeContacts
        }

        return Fragment()
    }

    override fun getCount(): Int {
        return 3
    }
}