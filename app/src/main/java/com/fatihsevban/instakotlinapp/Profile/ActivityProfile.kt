package com.fatihsevban.instakotlinapp.Profile

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings.ActivityProfileSettings
import com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings.FragmentEditProfile
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.BottomNavigationViewHelper
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile.*
import org.greenrobot.eventbus.EventBus

class ActivityProfile : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    // constants
    private val ACTIVITY_NO = 4
    private val ACTIVITY_TAG = "ACTIVITY PROFILE"

    // properties
    lateinit var currentUserData : User
    lateinit var userPosts: ArrayList<Post>
    lateinit var valueEventListener: ValueEventListener
    lateinit var mAuth: FirebaseAuth.AuthStateListener
    lateinit var adapterRecProfilePostsGrid: AdapterRecProfilePostsGrid
    lateinit var adapterRecProfilePostsList: AdapterRecProfilePostsList

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupAuthStateListener()
        setupValueEventListener()
        setupBottomNavigationView()

        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    private fun getUserPosts() {

        userPosts = ArrayList<Post>()

        FirebaseDatabase.getInstance().reference
                .child("Posts")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ActivityProfile, error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (data in dataSnapshot.children) {
                            val post = data.getValue(Post::class.java)
                            userPosts.add(post!!)
                        }

                        setRecyclerView()
                    }

                })
    }

    /**
     * sets the recyclerViews functionality, adapter and  .
     */
    private fun setRecyclerView() {

        // setting functionality to increase performance
        rcvProfilePosts.setHasFixedSize(true)
        rcvProfilePosts.setItemViewCacheSize(20)
        rcvProfilePosts.setDrawingCacheEnabled(true)
        rcvProfilePosts.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW)

        // setting adapter.
        adapterRecProfilePostsGrid = AdapterRecProfilePostsGrid(this, userPosts)
        rcvProfilePosts.adapter = adapterRecProfilePostsGrid

        // setting layout manager.
        val layoutManager = GridLayoutManager(this,3)
        rcvProfilePosts.layoutManager = layoutManager
    }

    fun makeRecGrid(view: View) {
        adapterRecProfilePostsGrid = AdapterRecProfilePostsGrid(this, userPosts)
        rcvProfilePosts.adapter = adapterRecProfilePostsGrid

        val layoutManager = GridLayoutManager(this,3)
        rcvProfilePosts.layoutManager = layoutManager
    }

    fun makeRecList(view: View) {
        adapterRecProfilePostsList = AdapterRecProfilePostsList(this, userPosts, currentUserData)
        rcvProfilePosts.adapter = adapterRecProfilePostsList

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rcvProfilePosts.setLayoutManager(linearLayoutManager)
    }

    /**
     * sets up value event listener.
     */
    private fun setupValueEventListener() {

        // setting Value event listener.
        valueEventListener = object: ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    currentUserData = data.getValue(User::class.java)!!
                    setUIAccordingToCurrentUser()
                    getUserPosts()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ActivityProfile, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * initialises(adds) value event listener.
     */
    private fun initialiseValueEventListener() {

        FirebaseDatabase.getInstance().reference
                .child("Users")
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().currentUser!!.uid)
                .addValueEventListener(valueEventListener)
    }

    /**
     * removes value event listener from this activity.
     */
    private fun removeValueEventListener() {

        FirebaseDatabase.getInstance().reference
                .child("Users")
                .removeEventListener(valueEventListener)
    }


    /**
     * sets User interface according to
     * the current users information.
     */
    private fun setUIAccordingToCurrentUser() {

        // setting followers count
        FirebaseDatabase.getInstance()
                .reference
                .child("followers")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ActivityProfile, error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        tvFollowers.text = dataSnapshot.childrenCount.toString()
                    }

                })

        // setting following count
        FirebaseDatabase.getInstance()
                .reference
                .child("following")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ActivityProfile, error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        tvFollowing.text = dataSnapshot.childrenCount.toString()
                    }
                })

        // setting Posts count
        FirebaseDatabase.getInstance()
                .reference
                .child("Posts")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ActivityProfile, error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        tvPosts.text = dataSnapshot.childrenCount.toString()
                    }
                })

        tvUsername.text = currentUserData.user_name
        tvNameAndSurname.text = currentUserData.name_and_surname

        // setting biography
        if (currentUserData.biography.isNullOrEmpty()) {
            tvBiography.text = "No Biography available."
        } else {
            tvBiography.text = currentUserData.biography
        }

        // setting profile image
        if (!currentUserData.profile_picture!!.equals("not determined"))
            UniversalImageLoader.setImage(currentUserData.profile_picture!!,
                    imgProfile, progressBar3, "")


        EventBus.getDefault().postSticky(EventBusDataEvent.SendCurrentUserData(currentUserData))
        makeUIVisible()
    }

    /**
     * makes user interface visible.
     */
    private fun makeUIVisible() {
        tvPosts.visibility = View.VISIBLE
        tvFollowers.visibility = View.VISIBLE
        tvFollowing.visibility = View.VISIBLE
        tvUsername.visibility = View.VISIBLE
        tvNameAndSurname.visibility = View.VISIBLE
        tvBiography.visibility = View.VISIBLE
        tvEditProfile.isEnabled = true
    }

    /**
     * sets up bottom navigation view.
     */
    private fun setupBottomNavigationView() {
        BottomNavigationViewHelper.setupBottomNavigationView(this,bottomNavigationViewEx)
        bottomNavigationViewEx.menu.getItem(ACTIVITY_NO).isChecked = true
    }

    /**
     * updates bottom navigation view when triggered.
     */
    override fun onResume() {
        super.onResume()
        bottomNavigationViewEx.menu.getItem(ACTIVITY_NO).isChecked = true
    }

    /**
     * goes to profile settings.
     * @param view is the menu icon.
     */
    fun goToProfileSettings(view: View) {
        val intent = Intent(this, ActivityProfileSettings::class.java)
        startActivity(intent)
        overridePendingTransition(0,0)
    }

    /**
     * opens edit profile fragment.
     * @param is the edit profile button.
     */
    fun openEditProfileFragment(view: View) {
        profileRootLayout.visibility = View.INVISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.profileContainerLayout, FragmentEditProfile())
        transaction.addToBackStack("ADD FRAG EDIT PROFILE")
        transaction.commit()
    }

    /**
     * arranges the visibility of the root layout.
     */
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            profileRootLayout.visibility = View.VISIBLE
            EventBus.getDefault().postSticky(EventBusDataEvent.SendCurrentUserData(currentUserData))
        }
    }

    /**
     * sets up auth state listener for this activity.
     */
    private fun setupAuthStateListener() {
        mAuth = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (FirebaseAuth.getInstance().currentUser == null) {
                val intent = Intent(this@ActivityProfile, ActivityLogin::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }

    /**
     * Adds the Auth state listener and
     * inialises the Event Value Listener.
     */
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuth)
        initialiseValueEventListener()
    }

    /**
     * Removes the Auth state listener
     * and the Event Value Listener.
     */
    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuth)
        removeValueEventListener()
    }

}