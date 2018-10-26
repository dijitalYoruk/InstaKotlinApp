package com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.BottomNavigationViewHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile_settings.*

class ActivityProfileSettings : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    // constants
    private val ACTIVITY_NO = 4
    private val ACTIVITY_TAG = "ACTIVITY PROFILE SETTINGS"

    // properties
    lateinit var mAuth: FirebaseAuth.AuthStateListener

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)
        setupAuthStateListener()
        setupBottomNavigationView()
        setupHideSwitch()

        supportFragmentManager.addOnBackStackChangedListener(this)

    }

    /**
     * sets up bottom navigation view.
     */
    private fun setupBottomNavigationView() {
        BottomNavigationViewHelper.setupBottomNavigationView(this,bottomNavigationViewEx)
        bottomNavigationViewEx.menu.getItem(ACTIVITY_NO).isChecked = true
    }

    /**
     * goes to profile settings.
     */
    fun goToProfileSettings(view: View) {
        val intent = Intent(this, ActivityProfileSettings::class.java)
        startActivity(intent)
    }

    /**
     * opens edit profile fragment.
     * @param view is the corresponding textView.
     */
    fun openEditProfileFragment(view: View) {
        profileSettingsRootLayout.visibility = View.INVISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.profileSettingsContainerLayout, FragmentEditProfile())
        transaction.addToBackStack("ADD FRAG EDIT PROFILE")
        transaction.commit()
    }

    /**
     * sets hide switch to hide account.
     */
    private fun setupHideSwitch() {

        val currentUId = FirebaseAuth.getInstance().currentUser?.uid!!

        // set switch state.
        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(currentUId)
                .child("_hidden")
                .addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@ActivityProfileSettings, databaseError.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val isHidden = dataSnapshot.value as Boolean
                        switchHide.isChecked = isHidden
                    }

                })

        // setting listener
        switchHide.setOnCheckedChangeListener{ compoundButton, boolean ->

            // hiding
            if (boolean) {

                FirebaseDatabase.getInstance()
                        .reference
                        .child("Users")
                        .child(currentUId)
                        .child("_hidden")
                        .setValue(true)

                // revealing account.
            } else {

                FirebaseDatabase.getInstance()
                        .reference
                        .child("Users")
                        .child(currentUId)
                        .child("_hidden")
                        .setValue(false)

            }
        }
    }

    /**
     * opens fragment liked posts.
     * @param view is the corresponding textView.
     */
    fun openFragmentLikedPosts(view: View) {
        profileSettingsRootLayout.visibility = View.INVISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.profileSettingsContainerLayout, FragmentLikedPosts())
        transaction.addToBackStack("ADD FRAG LIKED POSTS")
        transaction.commit()
    }

    /**
     * enables to go back.
     * @param view is the back image.
     */
    fun goBack(view: View) {
        onBackPressed()
    }

    /**
     * arranges the visibility of the root layout.
     */
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

    fun changePassword(view: View) {
        profileSettingsRootLayout.visibility = View.INVISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.profileSettingsContainerLayout, FragmentUpdatePassword())
        transaction.addToBackStack("ADD FRAG UPDATE PASSWORD")
        transaction.commit()
    }

    /**
     * enables to log Out.
     * asks whether the user is
     * sure or not in advance.
     */
    fun logOut(view: View) {

        AlertDialog.Builder(this)
                .setTitle("Logging Out From Instagram")
                .setMessage("Are You Sure ?")

                .setPositiveButton("Log Out") { dialog, which ->
                    FirebaseAuth.getInstance().signOut()
                }

                .setNegativeButton("Cancel") { dialog, which ->
                    // nothing to perform.
                }
                .show()
    }

    fun hideAccount (view: View) {
        switchHide.isChecked = !switchHide.isChecked
    }

    /**
     * sets up auth state listener for this activity.
     */
    private fun setupAuthStateListener() {
        mAuth = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (FirebaseAuth.getInstance().currentUser == null) {
                val intent = Intent(this@ActivityProfileSettings, ActivityLogin::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }

    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0)
            profileSettingsRootLayout.visibility = View.VISIBLE
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
}