package com.fatihsevban.instakotlinapp.Login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Home.HomeActivity.ActivityHome
import com.fatihsevban.instakotlinapp.Models.User
import kotlinx.android.synthetic.main.activity_login.*
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Register.ActivityRegister
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityLogin : AppCompatActivity() {

    // properties
    lateinit var mAuth: FirebaseAuth.AuthStateListener
    lateinit var textWatcher: TextWatcher

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupAuthStateListener()
        setupTextWatcher()
        addTextWatcherToViews()
    }

    /**
     * sets up auth state listener for this activity.
     */
    private fun setupAuthStateListener() {
        mAuth = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (FirebaseAuth.getInstance().currentUser != null) {
                val intent = Intent(this@ActivityLogin, ActivityHome::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                overridePendingTransition(0,0)
                startActivity(intent)
            }
        }
    }

    /**
     * sets up text watcher for some edit texts.
     */
    private fun setupTextWatcher() {

        textWatcher = object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnLogin.isEnabled = etPhoneEmailUsername.text.length >= 6 && etPassword.text.length >= 6
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }

    }

    /**
     * adds text watcher to the required edit texts.
     */
    private fun addTextWatcherToViews() {
        etPhoneEmailUsername.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
    }

    /**
     * directs the user to register activity.
     */
    fun goToActivityRegister(view: View) {
        val intent = Intent(this, ActivityRegister::class.java)
        startActivity(intent)
    }

    /**
     * enables the user to log in.
     * @param view is the login button.
     */
    fun logIn(view: View) {

        showProgressBar()

        // getting required informations.
        val phoneEmailUsername = etPhoneEmailUsername.text.toString()
        val password = etPassword.text.toString()

        // checking whether user exits in database.
        FirebaseDatabase.getInstance().reference.
                child("Users").
                addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        var isUserPresentInDatabase = false

                        for (data in dataSnapshot.children) {

                            val readedUser = data.getValue(User::class.java)!!

                            // user is registered.
                            if (readedUser.email.equals(phoneEmailUsername)
                                    || readedUser.phone_number.equals(phoneEmailUsername)
                                    || readedUser.user_name.equals(phoneEmailUsername)) {

                                // logging user in.
                                logInUser(readedUser, password)
                                isUserPresentInDatabase =true
                                break
                            }
                        }

                        // user is not registered.
                        if(!isUserPresentInDatabase) {
                            Toast.makeText(this@ActivityLogin, "You Are not Registered.", Toast.LENGTH_SHORT).show()
                            hideProgressBar()
                        }
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ActivityLogin, error.message, Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                    }
                })
    }

    /**
     * enables the user to log in.
     * @param user is the user that will log in.
     * @param password is users password.
     */
    private fun logInUser(user: User, password: String) {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(user.email.toString(), password).
                addOnCompleteListener { task ->

                    if (!task.isSuccessful) {
                        Toast.makeText(this@ActivityLogin, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    }

                    hideProgressBar()
                }
    }


    /**
     * Adds the Auth state listener.
     */
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuth)
    }

    /**
     * Removes the Auth state listener.
     */
    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuth)
    }

    fun showProgressBar() {
        progressBarLogin.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBarLogin.visibility = View.INVISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
    }

}