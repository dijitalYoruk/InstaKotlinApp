package com.fatihsevban.instakotlinapp.Register

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_register_via_phone.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.concurrent.TimeUnit

class FragmentRegisterPhone: Fragment() {

    // properties
    lateinit var phoneNumber: String
    lateinit var authCode: String
    lateinit var verificationId: String
    lateinit var mainView: View

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_register_via_phone, container, false)

        mainView.tvPhoneNumber.text = phoneNumber
        sendVerificationMessage()
        setAuthenticationCodeEntryEditTextListener()
        setNextButtonListener()

        return mainView
    }

    /**
     * set next button.
     */
    private fun setNextButtonListener() {
        mainView.btnNext.setOnClickListener { view ->
            val receivedCode = mainView.etAuthenticationCodeEntry.text.toString()
            authenticate(receivedCode)
        }
    }

    /**
     * authenticode the sended code and the received code.
     */
    private fun authenticate(receivedCode: String) {

        if (receivedCode.equals(authCode)) {

            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.registerContainerLayout, FragmentRegisterUser())
            transaction?.addToBackStack("ADD FRAG REGISTER USER")
            transaction?.commit()

            EventBus.getDefault().postSticky(EventBusDataEvent.SendPhoneRecordData(
                    phoneNumber, verificationId, authCode))

        } else {
            Toast.makeText(context,"Entered Code is Wrong.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * adds text watchers to the required views.
     */
    private fun setAuthenticationCodeEntryEditTextListener() {

        mainView.etAuthenticationCodeEntry.addTextChangedListener( object : TextWatcher {

            // checks the edit text and sets functionality.
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mainView.btnNext.isEnabled = s!!.length == 6
            }

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }

    /**
     * sends verification message to the users phone number.
     */
    private fun sendVerificationMessage() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber( phoneNumber, 60, TimeUnit.SECONDS, activity as Activity,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
                        authCode = credential?.smsCode.toString()
                        hideProgressBar()
                    }

                    override fun onVerificationFailed(exception: FirebaseException?) {
                        Toast.makeText(context, exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                    }

                    override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                        super.onCodeSent(verificationId, token)
                        this@FragmentRegisterPhone.verificationId = verificationId.toString()
                        showProgressBar()
                    }
                })
    }

    /**
     * gets phone number data through event bus.
     */
    @Subscribe(sticky = true)
    fun getPhoneData(phoneData: EventBusDataEvent.SendPhoneData) {
        phoneNumber = phoneData.phoneNumber
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    fun showProgressBar() {
        mainView.myProgressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        mainView.myProgressBar.visibility = View.INVISIBLE
    }

}