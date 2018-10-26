package com.fatihsevban.instakotlinapp.Register

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_register.*
import org.greenrobot.eventbus.EventBus

class ActivityRegister : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setRegistrationEditTextListener()
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    /**
     * sets listener for registration edit text.
     */
    private fun setRegistrationEditTextListener() {

        etPhoneEmailRegister.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnProceed.isEnabled = s!!.length >= 10
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }

    /**
     * sets the layout for phone registration.
     */
    fun registerWithPhone(view: View) {
        phoneShadowView.visibility = View.VISIBLE
        emailShadowView.visibility = View.INVISIBLE
        etPhoneEmailRegister.inputType = InputType.TYPE_CLASS_PHONE
        etPhoneEmailRegister.hint = "Phone"
        etPhoneEmailRegister.setText("")
    }

    /**
     * sets the layout for email registration.
     */
    fun registerWithEmail(view: View) {
        phoneShadowView.visibility = View.INVISIBLE
        emailShadowView.visibility = View.VISIBLE
        etPhoneEmailRegister.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        etPhoneEmailRegister.hint = "Email"
        etPhoneEmailRegister.setText("")
    }

    fun proceed(view: View) {

        val text = etPhoneEmailRegister.text.toString()

        // registering user with email.
        if (etPhoneEmailRegister.hint.toString().equals("Email")) {

            if (isValidEmail(text)) {

                FirebaseDatabase.getInstance().reference.
                        child("Users").
                        addListenerForSingleValueEvent(object: ValueEventListener{

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                var emailExistsInDatabase = false
                                val email = etPhoneEmailRegister.text.toString()

                                // checking whether email exists in database.
                                for (data in dataSnapshot.children) {

                                    val user = data.getValue(User::class.java)

                                    if (user?.email.toString().equals(email)) {
                                        emailExistsInDatabase = true; break
                                    }

                                }

                                // email doesnt exist, directing
                                // user to register user fragment.
                                if (!emailExistsInDatabase) {

                                    registerRootLayout.visibility = View.INVISIBLE
                                    val transaction = supportFragmentManager.beginTransaction()
                                    transaction.replace(R.id.registerContainerLayout, FragmentRegisterUser())
                                    transaction.addToBackStack("ADD FRAG REGISTER USER")
                                    transaction.commit()

                                    EventBus.getDefault().postSticky(EventBusDataEvent.SendEmailData(email))
                                }

                                else { // email exists.
                                    Toast.makeText(this@ActivityRegister, "Email already exists in database.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            // error
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@ActivityRegister, error.message, Toast.LENGTH_SHORT).show()
                            }

                        })

            } else { // email not valid.
                Toast.makeText(this, "Email is not valid.", Toast.LENGTH_SHORT).show()
            }
        }

        // registering user with phone number.
        else if (etPhoneEmailRegister.hint.toString().equals("Phone")) {

            if (isValidPhoneNumber(text)) {

                FirebaseDatabase.getInstance().reference.
                        child("Users").
                        addListenerForSingleValueEvent(object: ValueEventListener{

                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                var phoneExistsInDatabase = false
                                val phoneNumber = etPhoneEmailRegister.text.toString()

                                // checking whether email exists in database.
                                for (data in dataSnapshot.children) {

                                    val user = data.getValue(User::class.java)

                                    if (user?.phone_number.toString().equals(phoneNumber)) {
                                        phoneExistsInDatabase = true; break
                                    }
                                }

                                // phone number doesnt exist, directing
                                // user to register user fragment.
                                if (!phoneExistsInDatabase) {

                                    registerRootLayout.visibility = View.INVISIBLE
                                    val transaction = supportFragmentManager.beginTransaction()
                                    transaction.replace(R.id.registerContainerLayout, FragmentRegisterPhone())
                                    transaction.addToBackStack("ADD FRAG REGISTER PHONE")
                                    transaction.commit()

                                    EventBus.getDefault().postSticky(EventBusDataEvent.SendPhoneData(phoneNumber))
                                }

                                else { // phone number exists.
                                    Toast.makeText(this@ActivityRegister, "Phone number already exists in database.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            // error
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@ActivityRegister, error.message, Toast.LENGTH_SHORT).show()
                            }

                        })

            } else { // phone number not valid.
                Toast.makeText(this, "Phone Number is not valid.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackStackChanged() {
        if (supportFragmentManager.backStackEntryCount == 0)
            registerRootLayout.visibility = View.VISIBLE
    }

    /**
     * checks whether email is a valid email.
     * @param email is the email to be checked.
     */
    fun isValidEmail(email: String):Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * checks whether number is a valid phone number.
     * @param phoneNumber is the number to be checked.
     */
    fun isValidPhoneNumber(phoneNumber: String):Boolean {

        if (phoneNumber.length > 13)
            return false

        return android.util.Patterns.PHONE.matcher(phoneNumber).matches()
    }

    /**
     * goes to ActivityLogin
     * @param view is the textview.
     */
    fun goToActivityLogin(view: View) {
        val intent = Intent(this, ActivityLogin::class.java)
        startActivity(intent)
    }

}