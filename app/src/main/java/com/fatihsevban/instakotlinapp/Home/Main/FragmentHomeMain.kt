package com.fatihsevban.instakotlinapp.Home.Main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Home.HomeActivity.ActivityHome
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Models.PostWithUser
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.BottomNavigationViewHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home_main.view.*

class FragmentHomeMain: Fragment() {

    // constants
    val POST_COUNT_PER_PAGE = 30

    // properties
    lateinit var mainView: View
    lateinit var adapterRecHomeMain: AdapterRecHomeMain
    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    var followingUsersPosts = ArrayList<PostWithUser>()
    var displayedFollowingUsersPosts = ArrayList<PostWithUser>()
    var followingUsersIDs = ArrayList<String>()

    var pageNumber = 0
    var allPostsDisplayed = false

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_home_main, container, false)

        setupRecyclerView()
        setupBottomNavigationView()
        getFollowingUsersIdData()

        return mainView
    }

    /**
     * gets folllowing users Id data
     * to get the corresponding posts.
     */
    private fun getFollowingUsersIdData() {

        followingUsersIDs.add(currentUserId)

        FirebaseDatabase.getInstance()
                .reference
                .child("following")
                .child(currentUserId)
                .addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        // getting id data
                        for (data in dataSnapshot.children)
                            followingUsersIDs.add(data.value.toString())

                        // getting corresponding posts.
                        getFollowingUsersPostsData()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context!!, error.message, Toast.LENGTH_SHORT).show()
                    }

                })
    }

    /**
     * gets following users data and passes to the extension function.
     */
    private fun getFollowingUsersPostsData() {

        for (position in 0 until followingUsersIDs.size) {

            FirebaseDatabase.getInstance()
                    .reference
                    .child("Users")
                    .child(followingUsersIDs[position])
                    .addListenerForSingleValueEvent(object: ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            val followingUser = dataSnapshot.getValue(User::class.java)
                            getFollowingUsersPostsData(followingUser)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }

                    })
        }
    }

    /**
     * gets the folowing users posts and constructs
     * the recycler view with the obtained posts.
     * @param followingUser the user whose posts will be obtained.
     */
    private fun getFollowingUsersPostsData(followingUser: User?) {

        FirebaseDatabase.getInstance()
                .reference
                .child("Posts")
                .child(followingUser?.user_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot2: DataSnapshot) {

                        // getting posts
                        for (data in dataSnapshot2.children) {
                            val post = data.getValue(Post::class.java)
                            val postWithUser = PostWithUser(followingUser, post)
                            followingUsersPosts.add(postWithUser)
                        }

                        followingUsersPosts.sortWith(Comparator { file1, file2 ->

                            if(file1.post!!.upload_date!! > file2.post!!.upload_date!!){
                                -1
                            }else {
                                1
                            }
                        })

                        displayedFollowingUsersPosts.clear()
                        updateList()

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }

                })
    }

    /**
     * sets the recyclerViews functionality, adapter and and layout.
     */
    private fun setupRecyclerView() {

        // setting functionality to increase performance
        mainView.recHome.setHasFixedSize(true)
        mainView.recHome.setItemViewCacheSize(20)
        mainView.recHome.setDrawingCacheEnabled(true)
        mainView.recHome.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW)

        // setting adapter.
        adapterRecHomeMain = AdapterRecHomeMain(displayedFollowingUsersPosts, this)
        mainView.recHome.adapter = adapterRecHomeMain

        // setting layout manager.
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mainView.recHome.setLayoutManager(linearLayoutManager)


        mainView.recHome!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val pos = (mainView.recHome.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                if ( pos == displayedFollowingUsersPosts.size - 2)
                    addNewPosts()
            }

        })

    }

    /**
     * updates list while posts are uploaded by database.
     */
    fun updateList() {

        if (followingUsersPosts.size >= POST_COUNT_PER_PAGE) {

            val startingIndex = pageNumber * POST_COUNT_PER_PAGE
            val endingIndex = (pageNumber + 1) * POST_COUNT_PER_PAGE

            for (index in startingIndex until endingIndex)
                if (displayedFollowingUsersPosts.size < followingUsersPosts.size)
                    displayedFollowingUsersPosts.add(followingUsersPosts[index])

            adapterRecHomeMain.notifyDataSetChanged()

        } else {

            for (i in 0 until followingUsersPosts.size)
                displayedFollowingUsersPosts.add(followingUsersPosts.get(i))

            adapterRecHomeMain.notifyDataSetChanged()
            allPostsDisplayed = true
        }

    }

    /**
     * adds new posts while scrolling
     */
    private fun addNewPosts() {

        if (!allPostsDisplayed) {

            if (followingUsersPosts.size >= POST_COUNT_PER_PAGE) {

                val startingIndex = pageNumber * POST_COUNT_PER_PAGE
                val endingIndex = (++pageNumber) * POST_COUNT_PER_PAGE

                for (index in startingIndex until endingIndex) {

                    if (displayedFollowingUsersPosts.size < followingUsersPosts.size)
                        displayedFollowingUsersPosts.add(followingUsersPosts[index])

                    else {
                        allPostsDisplayed = true
                        break
                    }
                }

                adapterRecHomeMain.notifyDataSetChanged()

            } else {

                for (i in 0 until followingUsersPosts.size)
                    displayedFollowingUsersPosts.add(followingUsersPosts.get(i))

                adapterRecHomeMain.notifyDataSetChanged()
                allPostsDisplayed = true
            }
        }
    }


    /**
     * initialises bottom navigation view.
     */
    private fun setupBottomNavigationView() {
        val ACTIVITY_NO = (activity as ActivityHome).ACTIVITY_NO
        BottomNavigationViewHelper.setupBottomNavigationView(context!!, mainView.bottomNavigationViewEx)
        mainView.bottomNavigationViewEx.menu.getItem(ACTIVITY_NO).isChecked = true
    }

    /**
     * sets the current item of bottom navigation view.
     */
    override fun onResume() {
        super.onResume()
        val ACTIVITY_NO = (activity as ActivityHome).ACTIVITY_NO
        mainView.bottomNavigationViewEx.menu.getItem(ACTIVITY_NO).isChecked = true
    }

}