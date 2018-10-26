package com.fatihsevban.instakotlinapp.Search

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.BottomNavigationViewHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_search.*

class ActivitySearch : AppCompatActivity(), SearchView.OnQueryTextListener, FragmentManager.OnBackStackChangedListener {

    // constants
    private val ACTIVITY_NO = 1
    private val ACTIVITY_TAG = "ACTIVITY SEARCH"

    // properties
    private var usersData = ArrayList<User>()
    private lateinit var adapterRecSearchProfiles: AdapterRecSearchProfiles
    lateinit var mAuth: FirebaseAuth.AuthStateListener

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setupAuthStateListener()
        setupBottomNavigationView()
        setRecyclerView()
        getUsersData()
        setSearchView()

        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    /**
     * initialises search view and adds
     * functionality depending on the recycler view.
     */
    private fun setSearchView() {
        searchView.setOnQueryTextListener( this )
    }

    /**
     * get users data and displays it recycler view.
     */
    private fun getUsersData() {

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ActivitySearch, error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (data in dataSnapshot.children) {
                            val temp = data.getValue(User::class.java)

                            if (!temp?.user_id!!.equals(currentUserId)) {
                                usersData.add(temp)
                            }
                        }

                        adapterRecSearchProfiles.users = usersData
                        adapterRecSearchProfiles.notifyDataSetChanged()
                    }
                })
    }

    /**
     * sets users recycler view.
     */
    private fun setRecyclerView() {

        // setting adapter
        adapterRecSearchProfiles = AdapterRecSearchProfiles(this, usersData)
        rcvProfiles.adapter = adapterRecSearchProfiles

        // setting layout manager
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rcvProfiles.setLayoutManager(linearLayoutManager)
    }

    /**
     * sets up bottom navigation view.
     */
    private fun setupBottomNavigationView() {
        BottomNavigationViewHelper.setupBottomNavigationView(this, bottomNavigationViewEx)
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
     * sets up auth state listener for this activity.
     */
    private fun setupAuthStateListener() {
        mAuth = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (FirebaseAuth.getInstance().currentUser == null) {
                val intent = Intent(this@ActivitySearch, ActivityLogin::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }

    /**
     * registers the auth state listener to the activity.
     */
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuth)
    }

    /**
     * unregisters the auth state listener from the activity.
     */
    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuth)
    }

    /**
     * initialises search view and adds
     * functionality depending on the recycler view.
     */
    override fun onQueryTextChange(p0: String?): Boolean {
        adapterRecSearchProfiles.filter.filter( p0 )
        return true
    }

    /**
     * initialises search view and adds
     * functionality depending on the recycler view.
     */
    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0)
            searchRootLayout.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

}