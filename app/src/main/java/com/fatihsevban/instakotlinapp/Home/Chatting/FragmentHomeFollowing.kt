package com.fatihsevban.instakotlinapp.Home.Chatting

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home_following.view.*

class FragmentHomeFollowing: Fragment(), SearchView.OnQueryTextListener {

    // properties
    lateinit var mainView: View
    lateinit var adapterRecHomeFollowing: AdapterRecHomeFollowing
    private var usersData = ArrayList<User>()
    private var currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout and adding listeners.
        mainView = inflater.inflate(R.layout.fragment_home_following, container, false)

        setRecyclerView()
        getFollowingUsersIdData()
        setSearchView()

        return mainView
    }

    /**
     * initialises search view and adds
     * functionality depending on the recycler view.
     */
    private fun setSearchView() {
        mainView.searchView.setOnQueryTextListener( this )
    }

    /**
     * sets followed users recycler view.
     */
    private fun setRecyclerView() {

        // setting adapter
        adapterRecHomeFollowing = AdapterRecHomeFollowing(context!!, usersData)
        mainView.rcvFollowingUsers.adapter = adapterRecHomeFollowing

        // setting layout manager
        val linearLayoutManager = LinearLayoutManager(context!!)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mainView.rcvFollowingUsers.setLayoutManager(linearLayoutManager)
    }

    /**
     * gets following users id data.
     */
    private fun getFollowingUsersIdData() {

        FirebaseDatabase.getInstance()
                .reference
                .child("following")
                .child(currentUserId)
                .addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val followingUsersId = ArrayList<String>()

                        for (data in dataSnapshot.children) {
                            val temp = data.getValue().toString()
                            followingUsersId.add(temp)
                        }

                        getFollowingUsersData(followingUsersId)
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
                })
    }

    /**
     * gets following users id data.
     * @param followingUsersId is the users data.
     */
    private fun getFollowingUsersData(followingUsersId: ArrayList<String>) {

        usersData.clear()

        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (data in dataSnapshot.children) {
                            val temp = data.getValue(User::class.java)

                            if (followingUsersId.contains(temp!!.user_id.toString()))
                                usersData.add(temp)
                        }

                        adapterRecHomeFollowing.notifyDataSetChanged()
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                })

    }

    /**
     * shows error toast on the layout.
     * @param error is the error.
     */
    fun showErrorToast(error: DatabaseError) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * initialises search view and adds
     * functionality depending on the recycler view.
     */
    override fun onQueryTextChange(p0: String?): Boolean {
        adapterRecHomeFollowing.filter.filter( p0 )
        return true
    }

    /**
     * initialises search view and adds
     * functionality depending on the recycler view.
     */
    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

}