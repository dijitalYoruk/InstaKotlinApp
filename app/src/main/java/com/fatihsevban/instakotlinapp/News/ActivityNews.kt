package com.fatihsevban.instakotlinapp.News

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.Models.Notification
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.BottomNavigationViewHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_news.*

class ActivityNews : AppCompatActivity() {

    // constants
    private val ACTIVITY_NO = 3
    private val ACTIVITY_TAG = "ACTIVITY NEWS"

    // properties
    val notifications = ArrayList<Notification>()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
    lateinit var adapterRecNewsYou: AdapterRecNewsYou
    lateinit var mAuth: FirebaseAuth.AuthStateListener

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        setupAuthStateListener()
        setupBottomNavigationView()
        setRecyclerView()
        refreshNotifications()
        rcvNotificationsYouRefreshLayout.setOnRefreshListener { refreshNotifications() }
    }

    /**
     * sets news recycler view.
     */
    private fun setRecyclerView() {
        // setting adapter.
        adapterRecNewsYou = AdapterRecNewsYou(this, notifications)
        rcvNotificationsYou.adapter = adapterRecNewsYou

        // setting layout manager.
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rcvNotificationsYou.setLayoutManager(linearLayoutManager)
    }

    /**
     * refreshes notifications.
     */
    fun refreshNotifications() {

        notifications.clear()

        FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(currentUserId)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        // getting all notifications.
                        for (data in dataSnapshot.children)  {
                            val notification = data.getValue(Notification::class.java)!!
                            notifications.add(notification)
                        }

                        adapterRecNewsYou.notifyDataSetChanged()
                        rcvNotificationsYouRefreshLayout.setRefreshing(false)
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                })
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
                val intent = Intent(this@ActivityNews, ActivityLogin::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }

    /**
     * Adds the Auth state listener
     */
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuth)
    }

    /**
     * Removes the Auth state listener
     */
    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuth)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    /**
     * shows error toast on the layout.
     * @param error is the error.
     */
    private fun showErrorToast(error: DatabaseError) {
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
    }

}