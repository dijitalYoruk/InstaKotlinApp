package com.fatihsevban.instakotlinapp.Register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_register_user.*
import kotlinx.android.synthetic.main.fragment_register_user.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class FragmentRegisterUser: Fragment() {

    // properties
    lateinit var mainView : View
    private var phoneNumber: String = "Not Determined"
    lateinit var textWatcher: TextWatcher

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_register_user, container, false)

        setTextWatcher()
        addTextWatcherToViews()
        EventBus.getDefault().register(this)
        setRegisterButton()

        return mainView
    }

    /**
     * sets text watcher.
     */
    private fun setTextWatcher() {

        textWatcher = object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                mainView.btnRegister.isEnabled = (mainView.etNameAndSurname.text.length >= 6)
                        && (mainView.etPassword.text.length >= 6)
                        && (mainView.etUserName.text.length >= 6)
                        && (mainView.etEmail.text.length >= 10)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    /**
     * adds textwatcher to the required views.
     */
    private fun addTextWatcherToViews() {

        mainView.etNameAndSurname.addTextChangedListener(textWatcher)
        mainView.etPassword.addTextChangedListener(textWatcher)
        mainView.etUserName.addTextChangedListener(textWatcher)
        mainView.etEmail.addTextChangedListener(textWatcher)
    }

    @Subscribe(sticky = true)
    fun getPhoneRecordData(phoneRecordData: EventBusDataEvent.SendPhoneRecordData) {
        phoneNumber = phoneRecordData.phoneNumber
    }

    @Subscribe(sticky = true)
    fun getEmailData(emailData: EventBusDataEvent.SendEmailData) {
        val receivedEmail = emailData.email.toString()
        mainView.etEmail.setText(receivedEmail)
        mainView.etEmail.isEnabled = false

    }

    /**
     * registers user and directs to login activity.
     */
    private fun setRegisterButton() {

        mainView.btnRegister.setOnClickListener{ view ->

            showProgressBar()
            val user_name = mainView.etUserName.text.toString()
            val email = mainView.etEmail.text.toString()
            val password = mainView.etPassword.text.toString()

            FirebaseDatabase.getInstance().reference.
                    child("Users").
                    addListenerForSingleValueEvent(object: ValueEventListener{

                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            var userNameExistsInDatabase = false

                            // checking whether username exists in database.
                            for (data in dataSnapshot.children) {
                                val temp = data.getValue(User::class.java)

                                if (temp?.user_name.toString().equals(user_name)) {
                                    userNameExistsInDatabase = true; break
                                }
                            }

                            // username doesn't exist
                            if (!userNameExistsInDatabase) {
                                recordUserToAuth(email, password)
                            }

                            else { // username exists.
                                Toast.makeText(context, "User name already exists in database.", Toast.LENGTH_SHORT).show()
                            }

                        }

                        // error
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }

                    })
        }
    }

    /**
     * records user to FirebaseAuth.
     * @param email is the email to be recorded.
     * @param password is the password to be recorded.
     */
    private fun recordUserToAuth(email: String, password: String) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).
                addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        recordUserToDatabase(getUserToBeRecorded())
                        FirebaseAuth.getInstance().signOut()
                    } else {
                        Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                    }
                }
    }

    /**
     * gets the users required informations.
     */
    private fun getUserToBeRecorded(): User {
        val email = mainView.etEmail.text.toString()
        val user_name = mainView.etUserName.text.toString()
        val name_and_surname = mainView.etNameAndSurname.text.toString()
        val phone_number = phoneNumber
        val is_hidden = false
        val user_id: String? = FirebaseAuth.getInstance().currentUser?.uid
        val profile_picture: String? = "not determined"
        val biography: String? = ""
        val web_site: String? = ""

        return User(email, user_name, name_and_surname, phone_number, user_id,
                is_hidden, profile_picture, biography, web_site)
    }

    /**
     * records user to database.
     * @param user is the user to be recorded.
     */
    private fun recordUserToDatabase(user: User) {

        FirebaseDatabase.getInstance().reference.
                child("Users").
                child(user.user_id.toString()).
                setValue(user).
                addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        Toast.makeText(context, "User created", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, ActivityLogin::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)

                    } else {
                        Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    }

                    hideProgressBar()
                }
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    fun showProgressBar() {
        mainView.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        mainView.progressBar.visibility = View.INVISIBLE
    }

}