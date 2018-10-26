package com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatihsevban.instakotlinapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_profile_password.*
import kotlinx.android.synthetic.main.fragment_profile_password.view.*
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser




class FragmentUpdatePassword: Fragment() {

    // properties
    lateinit var mainView: View
    lateinit var textWatcher: TextWatcher

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_profile_password, container, false)

        mainView.imgUpdatePassword.setOnClickListener { updatePassword() }
        setupTextWatcher()
        addTextWatcherToViews()
        return mainView
    }

    /**
     * updates password of the current user.
     */
    private fun updatePassword() {

        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etPassword.text.toString()
        val newPasswordRepeat = etPasswordRepeat.text.toString()

        // cheking whether new passwords match.
        if (newPassword.equals(newPasswordRepeat)) {

            // reauthenticating current user.
            val credential = EmailAuthProvider
                    .getCredential(currentUser.email!!, currentPassword)

            currentUser.reauthenticate(credential).addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    // updating password.
                    currentUser.updatePassword(newPassword)
                            .addOnCompleteListener { task2 ->

                                if (task2.isSuccessful) {
                                    Toast.makeText(context, "Password Updated", Toast.LENGTH_SHORT).show()
                                    activity?.onBackPressed()
                                }
                            }

                } else { // reauthenticating error
                    Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        } else { // new passwords not matching.
            Toast.makeText(context, "Passwords doesn't match.", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * sets text watcher for the edit texts
     */
    private fun setupTextWatcher() {

        textWatcher = object: TextWatcher {

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etPassword.text.toString()
                val newPasswordRepeat = etPasswordRepeat.text.toString()

                if (currentPassword.length >= 6 && newPassword.length >= 6 && newPasswordRepeat.length >= 6)
                    mainView.imgUpdatePassword.isEnabled = true
            }

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        }
    }

    /**
     * adds textWatcher to the corresponding views.
     */
    private fun addTextWatcherToViews() {
        mainView.etCurrentPassword.addTextChangedListener(textWatcher)
        mainView.etPassword.addTextChangedListener(textWatcher)
        mainView.etPasswordRepeat.addTextChangedListener(textWatcher)
    }
}