package com.fatihsevban.instakotlinapp.Search

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.Profile.AdapterRecProfilePostsGrid
import com.fatihsevban.instakotlinapp.Profile.AdapterRecProfilePostsList
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.Notifications
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import kotlinx.android.synthetic.main.fragment_search_profile.view.*

class FragmentSearchProfile: Fragment() {

    // properties
    lateinit var profileData: User
    lateinit var mainView: View
    lateinit var valueEventListener: ValueEventListener
    lateinit var adapterRecProfilePostsGrid: AdapterRecProfilePostsGrid
    lateinit var adapterRecProfilePostsList: AdapterRecProfilePostsList

    private var currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
    private var profilePosts = ArrayList<Post>()

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mainView = inflater.inflate(R.layout.fragment_search_profile, container, false)

        setValueEventListener()
        addValueEventListener()
        setUIAccordingToProfileData()
        setOnClickListenersToViews()
        getUserPosts()


        return mainView
    }

    /**
     * sets recycler view posts.
     */
    private fun setRecyclerView() {
        // setting adapter.
        adapterRecProfilePostsGrid = AdapterRecProfilePostsGrid(context!!, profilePosts)
        mainView.rcvProfilePosts.adapter = adapterRecProfilePostsGrid

        // setting layout manager.
        val layoutManager = GridLayoutManager(context!!,3)
        mainView.rcvProfilePosts.layoutManager = layoutManager
    }

    /**
     * sets User interface according to
     * the current users information.
     */
    private fun setUIAccordingToProfileData() {

        // setting Followers Count
        FirebaseDatabase.getInstance()
                .reference
                .child("followers")
                .child(profileData.user_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mainView.tvFollowers.text = dataSnapshot.childrenCount.toString()
                    }

                })

        // setting Following Count
        FirebaseDatabase.getInstance()
                .reference
                .child("following")
                .child(profileData.user_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mainView.tvFollowing.text = dataSnapshot.childrenCount.toString()
                    }
                })

        // setting Posts Count
        FirebaseDatabase.getInstance()
                .reference
                .child("Posts")
                .child(profileData.user_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        mainView.tvPosts.text = dataSnapshot.childrenCount.toString()
                    }
                })

        // setting follow button
        FirebaseDatabase.getInstance()
                .reference
                .child("following")
                .child(currentUserId)
                .addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }


                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.hasChild(profileData.user_id!!))
                            mainView.btnFollow.text = "Following"

                        else {

                            if (profileData._hidden!!) {

                                FirebaseDatabase.getInstance()
                                        .reference
                                        .child("Follow Requests")
                                        .child(profileData.user_id!!)
                                        .addListenerForSingleValueEvent(object: ValueEventListener{

                                            override fun onCancelled(error: DatabaseError) {
                                                showErrorToast(error)
                                            }

                                            override fun onDataChange(p0: DataSnapshot) {

                                                if (p0.hasChild(currentUserId)) {
                                                    mainView.btnFollow.text = "Follow Requested"
                                                } else {
                                                    mainView.btnFollow.text = "Follow"
                                                }
                                            }

                                        })

                            } else
                                mainView.btnFollow.text = "Follow"

                        }
                    }
                })


        mainView.tvUsername.text = profileData.user_name
        mainView.tvNameAndSurname.text = profileData.name_and_surname

        // setting biography
        if (profileData.biography.isNullOrEmpty() ) {
            mainView.tvBiography.text = "No Biography available."
        } else {
            mainView.tvBiography.text = profileData.biography
        }

        // setting profile image
        if (!profileData.profile_picture!!.equals("not determined"))
            UniversalImageLoader.setImage(profileData.profile_picture!!,
                    mainView.imgProfile, mainView.progressBar3, "")

    }

    /**
     * sets on click listeners to the
     * required views on the layout.
     */
    private fun setOnClickListenersToViews() {

        // making rcvProfilePosts grid
        mainView.imgMakeRecGrid.setOnClickListener { view ->
            // setting adapter.
            adapterRecProfilePostsGrid = AdapterRecProfilePostsGrid(context!!, profilePosts)
            mainView.rcvProfilePosts.adapter = adapterRecProfilePostsGrid

            // setting layout manager.
            val layoutManager = GridLayoutManager(context!!,3)
            mainView.rcvProfilePosts.layoutManager = layoutManager
        }

        // making rcvProfilePosts list
        mainView.imgMakeRecList.setOnClickListener { view ->
            // setting adapter.
            adapterRecProfilePostsList = AdapterRecProfilePostsList(context!!, profilePosts, profileData)
            mainView.rcvProfilePosts.adapter = adapterRecProfilePostsList

            // setting layout manager.
            val linearLayoutManager = LinearLayoutManager(context!!)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            mainView.rcvProfilePosts.layoutManager = linearLayoutManager
        }

        mainView.imgGetBack.setOnClickListener { view ->
            activity?.onBackPressed()
        }

        // enabling a user to follow.
        mainView.btnFollow.setOnClickListener { view ->

            FirebaseDatabase.getInstance()
                    .reference
                    .child("following")
                    .child(currentUserId)
                    .addListenerForSingleValueEvent(object: ValueEventListener{

                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            // unfollowing
                            if (dataSnapshot.hasChild(profileData.user_id!!)) {
                                unfollowProfile()

                                if (profileData._hidden!!)
                                    hidePosts()
                            }

                            else { // following

                                // profile is hidden
                                if (profileData._hidden!!) {

                                    FirebaseDatabase.getInstance()
                                            .reference
                                            .child("Follow Requests")
                                            .child(profileData.user_id!!)
                                            .addListenerForSingleValueEvent(object: ValueEventListener{

                                                override fun onDataChange(dataSnapshot: DataSnapshot) {

                                                    // removing follow request
                                                    if (dataSnapshot.hasChild(currentUserId)) {
                                                        removeFollowRequest()
                                                        mainView.btnFollow.text = "Follow"

                                                    } else { // making follow request.
                                                        makeFollowRequest()
                                                        mainView.btnFollow.text = "Follow Requested"
                                                    }
                                                }

                                                // error
                                                override fun onCancelled(error: DatabaseError) {
                                                    showErrorToast(error)
                                                }

                                            })

                                } else { // profile is not hidden.
                                    followProfile()
                                }
                            }
                        }

                        // error
                        override fun onCancelled(error: DatabaseError) {
                            showErrorToast(error)
                        }

                    })
        }
    }

    /**
     * sets valueEventListener.
     */
    private fun setValueEventListener() {

        valueEventListener = object: ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                profileData = dataSnapshot.getValue(User::class.java)!!

                FirebaseDatabase.getInstance()
                        .reference
                        .child("following")
                        .child(currentUserId)
                        .addListenerForSingleValueEvent(object: ValueEventListener{

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                val isFollowing = dataSnapshot.hasChild(profileData.user_id!!)

                                if (!profileData._hidden!! || (profileData._hidden!! && isFollowing)) {

                                    FirebaseDatabase.getInstance()
                                            .reference
                                            .child("Follow Requests")
                                            .child(profileData.user_id!!)
                                            .addListenerForSingleValueEvent(object: ValueEventListener{

                                                override fun onCancelled(error: DatabaseError) {
                                                    showErrorToast(error)
                                                }

                                                override fun onDataChange(dataSnapshot: DataSnapshot) {

                                                    if ( dataSnapshot.hasChild(currentUserId) ) {
                                                        removeFollowRequest()
                                                        followProfile()
                                                        showPosts()
                                                    }
                                                }
                                            })
                                }

                                else { // hiding posts
                                    hidePosts()
                                }
                            }

                            // error
                            override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
                        })
            }

            // error
            override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
        }

    }

    /**
     * removes valueEventListener.
     */
    private fun removeValueEventListener() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(profileData.user_id!!)
                .removeEventListener(valueEventListener)
    }

    /**
     * adds valueEventListener.
     */
    private fun addValueEventListener() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(profileData.user_id!!)
                .addValueEventListener(valueEventListener)
    }

    /**
     * shows error toast on the layout.
     * @param error is the error.
     */
    private fun showErrorToast(error: DatabaseError) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * makes follow request and
     * saves the request to database.
     */
    private fun makeFollowRequest() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Follow Requests")
                .child(profileData.user_id!!)
                .child(currentUserId)
                .setValue(currentUserId)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Notifications.newFollowRequestNotification(profileData.user_id!!)
                    }
                }
    }

    /**
     * removes follow request and removes
     * the request from database as well.
     */
    private fun removeFollowRequest() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Follow Requests")
                .child(profileData.user_id!!)
                .child(currentUserId)
                .removeValue()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Notifications.removeFollowRequestNotification(profileData.user_id!!)
                    }
                }
    }

    /**
     * follows profile and saves to database.
     */
    private fun followProfile() {

        FirebaseDatabase.getInstance()
                .reference
                .child("following")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(profileData.user_id!!)
                .setValue(profileData.user_id!!)

        FirebaseDatabase.getInstance()
                .reference
                .child("followers")
                .child(profileData.user_id!!)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(FirebaseAuth.getInstance().currentUser!!.uid)

        Notifications.startedToFollowNotification(profileData.user_id!!)
        mainView.btnFollow.text = "Following"
    }

    /**
     * unfollows profile and removes from database.
     */
    private fun unfollowProfile() {

        FirebaseDatabase.getInstance()
                .reference
                .child("following")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(profileData.user_id!!)
                .removeValue()

        FirebaseDatabase.getInstance()
                .reference
                .child("followers")
                .child(profileData.user_id!!)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .removeValue()

        Notifications.abandonedToFollowNotification(profileData.user_id!!)
        mainView.btnFollow.text = "Follow"

    }

    /**
     * shows posts by making corresponding views visible.
     */
    private fun showPosts() {
        mainView.containerHidden.visibility = View.GONE
        mainView.rcvProfilePosts.visibility = View.VISIBLE
        mainView.imgMakeRecGrid.visibility = View.VISIBLE
        mainView.imgMakeRecList.visibility = View.VISIBLE
        mainView.imgContacts.visibility = View.VISIBLE
        mainView.imgLocation.visibility = View.VISIBLE
        mainView.dividerBottom.visibility = View.VISIBLE
    }

    /**
     * hides posts by making corresponding views invisible.
     */
    private fun hidePosts() {
        mainView.containerHidden.visibility = View.VISIBLE
        mainView.rcvProfilePosts.visibility = View.GONE
        mainView.imgMakeRecGrid.visibility = View.GONE
        mainView.imgMakeRecList.visibility = View.GONE
        mainView.imgContacts.visibility = View.GONE
        mainView.imgLocation.visibility = View.GONE
        mainView.dividerBottom.visibility = View.GONE
    }

    /**
     * gets user posts and diplays them in recyclerview.
     */
    private fun getUserPosts() {

        profilePosts = ArrayList<Post>()

        FirebaseDatabase.getInstance()
                .reference
                .child("Posts")
                .child(profileData.user_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }


                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (data in dataSnapshot.children) {
                            val post = data.getValue(Post::class.java)
                            profilePosts.add(post!!)
                        }


                        setRecyclerView()
                    }

                })
    }

    /**
     * gets profile data through EventBus.
     */
    @Subscribe(sticky = true)
    fun getProfileData(event: EventBusDataEvent.SendProfileData){
        profileData = event.profileData
    }

    /**
     * registers EventBus from the fragment.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    /**
     * unregisters EventBus from the fragment.
     */
    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
        removeValueEventListener()
    }

}