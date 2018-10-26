package com.fatihsevban.instakotlinapp.Utils

import android.content.Context
import android.content.Intent
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.fatihsevban.instakotlinapp.Home.HomeActivity.ActivityHome
import com.fatihsevban.instakotlinapp.News.ActivityNews
import com.fatihsevban.instakotlinapp.Profile.ActivityProfile
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Search.ActivitySearch
import com.fatihsevban.instakotlinapp.Share.ActivityShare
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx

class BottomNavigationViewHelper {

    companion object {

        /**
         * sets the bottom navigation view.
         * @param context is the context where the bottom navigation view will be initialised
         * @param bottomNavigationViewEx is the bottom navigation view that will ne arranged.
         */
        fun setupBottomNavigationView(context: Context, bottomNavigationViewEx: BottomNavigationViewEx) {

            // setting some functionalities
            bottomNavigationViewEx.enableAnimation(false)
            bottomNavigationViewEx.enableItemShiftingMode(false)
            bottomNavigationViewEx.enableShiftingMode(false)
            bottomNavigationViewEx.setTextVisibility(false)

            // setting what to do when an item is selected
            bottomNavigationViewEx.onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
                menuItem: MenuItem ->

                when(menuItem.itemId) {

                    // Home
                    R.id.menu_item_home -> {
                        val intent = Intent(context, ActivityHome::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                        (context as AppCompatActivity).overridePendingTransition(0,0)
                    }

                    // News
                    R.id.menu_item_news -> {
                        val intent = Intent(context, ActivityNews::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                        (context as AppCompatActivity).overridePendingTransition(0,0)
                    }

                    // Profile
                    R.id.menu_item_profile -> {
                        val intent = Intent(context, ActivityProfile::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                        (context as AppCompatActivity).overridePendingTransition(0,0)
                    }

                    // Search
                    R.id.menu_item_search -> {
                        val intent = Intent(context, ActivitySearch::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                        (context as AppCompatActivity).overridePendingTransition(0,0)
                    }

                    // Share
                    R.id.menu_item_share -> {
                        val intent = Intent(context, ActivityShare::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                        (context as AppCompatActivity).overridePendingTransition(0,0)
                    }
                }

                return@OnNavigationItemSelectedListener true
            }
        }
    }
}