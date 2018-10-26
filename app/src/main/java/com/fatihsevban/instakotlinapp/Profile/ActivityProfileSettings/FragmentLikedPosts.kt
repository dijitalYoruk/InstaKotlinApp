package com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Profile.AdapterRecProfilePostsGrid
import com.fatihsevban.instakotlinapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_profile_liked_posts.view.*

class FragmentLikedPosts: Fragment() {

    // constants
    val CURRENT_USER_ID = FirebaseAuth.getInstance().currentUser?.uid!!

    // properties
    lateinit var mainView: View
    lateinit var adapterRecProfilePostsGrid: AdapterRecProfilePostsGrid
    val likedPostIDs = ArrayList<String>()
    val likedPosts = ArrayList<Post>()

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_profile_liked_posts, container, false)

        // clearing posts.
        likedPosts.clear()
        likedPostIDs.clear()

        getPostIds()
        return mainView
    }

    /**
     * gets post ids from database.
     */
    private fun getPostIds() {

        FirebaseDatabase.getInstance()
                .reference
                .child("PostLikers")
                .orderByChild(CURRENT_USER_ID)
                .equalTo(CURRENT_USER_ID)
                .addListenerForSingleValueEvent(object: ValueEventListener{


                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (data in dataSnapshot.children)
                            likedPostIDs.add(data.key.toString())

                        getPosts()
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
                })

    }

    /**
     * gets all liked posts from database.
     */
    private fun getPosts() {

        for (likedPostId in  likedPostIDs) {

            FirebaseDatabase.getInstance()
                    .reference
                    .child("Posts")
                    .orderByChild(likedPostId)
                    .addListenerForSingleValueEvent(object: ValueEventListener{

                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            var postMakerUserId = ""

                            for (data in dataSnapshot.children)
                                postMakerUserId = data.key.toString()

                            val post = dataSnapshot
                                    .child(postMakerUserId)
                                    .child(likedPostId)
                                    .getValue(Post::class.java)!!

                            likedPosts.add(post)

                            // after getting all posts, setting recycler view.
                            if (likedPostIDs.size == likedPosts.size)
                                setRecyclerView()
                        }

                        // error
                        override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
                    })
        }


    }

    /**
     * sets liked posts recycler view in grid mode.
     */
    private fun setRecyclerView() {
        // setting adapter
        adapterRecProfilePostsGrid = AdapterRecProfilePostsGrid(context!!, likedPosts)
        mainView.rcvLikedPosts.adapter = adapterRecProfilePostsGrid

        // setting layout manager.
        val layoutManager = GridLayoutManager(context,3)
        mainView.rcvLikedPosts.layoutManager = layoutManager
    }


    /**
     * shows Error Message through Toast.
     * @param error is the error to be displayed.
     */
    fun showErrorToast(error: DatabaseError) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }
}