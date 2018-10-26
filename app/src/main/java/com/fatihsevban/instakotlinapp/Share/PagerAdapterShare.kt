package com.fatihsevban.instakotlinapp.Share

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup

class PagerAdapterShare(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {

    // properties
    val galleryFragment = FragmentShareGallery()
    val cameraFragment = FragmentShareCamera()
    val videoFragment = FragmentShareVideo()

    /**
     * places each item to view pager.
     * @param position is the position value.
     * @return the placed fragment.
     */
    override fun getItem(position: Int): Fragment? {

        when(position) {

            0 -> return galleryFragment

            1 -> return cameraFragment

            2 -> return videoFragment
        }

        return null
    }

    /**
     * sets each placed fragments title.
     * @param position is the placed position.
     * @return the setted title of the fragment.
     */
    override fun getPageTitle(position: Int): CharSequence? {

        when(position) {

            0 -> return "Gallery"

            1 -> return "Camera"

            2 -> return "Video"
        }

        return null
    }

    /**
     * indicates how many fragments the pager adapter contains.
     */
    override fun getCount(): Int { return 3 }

}