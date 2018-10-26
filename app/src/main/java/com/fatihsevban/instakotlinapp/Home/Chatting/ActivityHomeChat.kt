package com.fatihsevban.instakotlinapp.Home.Chatting

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.Models.Chat
import com.fatihsevban.instakotlinapp.Models.ChatMessage
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home_chat.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ActivityHomeChat : AppCompatActivity() {

    // constants
    private val MESSAGES_PER_PAGE = 5

    // properties
    lateinit var contactUser: User
    lateinit var currentUser: User

    lateinit var childEventListener: ChildEventListener
    lateinit var valueEventListener: ValueEventListener
    lateinit var childEventListenerMore: ChildEventListener
    lateinit var adapterRecHomeMessaging: AdapterRecHomeMessaging
    lateinit var mAuth: FirebaseAuth.AuthStateListener

    lateinit var lastRefreshedMessageId : String
    lateinit var presentRefreshedMessageId: String

    var allMessages = ArrayList<ChatMessage>()
    var messagePosition = 0

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_chat)

        setupAuthStateListener()
        setupValueEventListener()
        getCurrentUserData()
        setRecyclerView()
        setMessageTypingListener()
        setRefreshLayoutListener()
    }

    /**
     * sets up auth state listener for this activity.
     */
    private fun setupAuthStateListener() {
        mAuth = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (FirebaseAuth.getInstance().currentUser == null) {
                val intent = Intent(this@ActivityHomeChat, ActivityLogin::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }

    /**
     * setups ValueEventListener and checks
     * whether contact user is typing or not.
     */
    private fun setupValueEventListener() {

        valueEventListener = object: ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {

                    val isContactTyping = dataSnapshot.value as Boolean

                    // contact typing
                    if (isContactTyping) {
                        tvTyping.visibility = View.VISIBLE
                        tvTyping.startAnimation(AnimationUtils.loadAnimation(this@ActivityHomeChat,android.R.anim.fade_in))

                    } else { // contact not typing

                        tvTyping.visibility = View.GONE
                        tvTyping.startAnimation(AnimationUtils.loadAnimation(this@ActivityHomeChat,android.R.anim.fade_out))
                    }
                }
            }

            // error
            override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
        }
    }

    /**
     * adds Value Event Listener.
     */
    private fun addValueEventListener() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Chats")
                .child("Typing")
                .child(contactUser.user_id!!)
                .child("is_typing")
                .addValueEventListener(valueEventListener)
    }

    /**
     * removes Value Event Listener.
     */
    private fun removeValueEventListener() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Chats")
                .child("Typing")
                .child(contactUser.user_id!!)
                .child("is_typing")
                .removeEventListener(valueEventListener)
    }

    /**
     * gets current users data.
     */
    private fun getCurrentUserData() {

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(currentUserId)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        currentUser = dataSnapshot.getValue(User::class.java)!!
                        setChildEventListener()
                        setUserInterface()
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                })
    }

    /**
     * sets child event listener for the messages.
     */
    private fun setChildEventListener() {

        childEventListener = object: ChildEventListener {

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {

                val message = dataSnapshot.getValue(ChatMessage::class.java)

                if (allMessages.size == 0) {
                    lastRefreshedMessageId = dataSnapshot.key!!
                    presentRefreshedMessageId = dataSnapshot.key!!
                }

                // adding message
                allMessages.add(message!!)

                // positioning recycler view.
                adapterRecHomeMessaging.notifyItemInserted(allMessages.size -1)
                adapterRecHomeMessaging.notifyItemRangeChanged(allMessages.size -1, allMessages.size)
                rcvChatMessages.smoothScrollToPosition(allMessages.size - 1)
            }
        }

        addChildEventListener()
    }

    /**
     * adds child event listener
     */
    private fun addChildEventListener() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Messages")
                .child(currentUser.user_id!!)
                .child(contactUser.user_id!!)
                .limitToLast(MESSAGES_PER_PAGE)
                .addChildEventListener(childEventListener)
    }

    /**
     * removes child event listener.
     */
    private fun removeChildEventListener(){

        FirebaseDatabase.getInstance()
                .reference
                .child("Messages")
                .child(currentUser.user_id!!)
                .child(contactUser.user_id!!)
                .limitToLast(MESSAGES_PER_PAGE)
                .removeEventListener(childEventListener)
    }


    /**
     * sets user interface according to current user.
     */
    private fun setUserInterface() {
        tvUsernameContact.text = contactUser.user_name

        UniversalImageLoader.setImage(currentUser.profile_picture!!,
                imgProfile, null, "")
    }

    /**
     * sets messages recycler view.
     */
    private fun setRecyclerView() {

        // setting adapter.
        adapterRecHomeMessaging = AdapterRecHomeMessaging(this, allMessages)
        rcvChatMessages.adapter = adapterRecHomeMessaging

        // setting layout.
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rcvChatMessages.setLayoutManager(linearLayoutManager)
    }

    /**
     * sets the state of typing message to database.
     */
    private fun setMessageTypingListener() {

        etMessage.addTextChangedListener(object: TextWatcher{

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                // typing
                if (etMessage.text.isNullOrEmpty()) {

                    FirebaseDatabase.getInstance()
                            .reference
                            .child("Chats")
                            .child("Typing")
                            .child(currentUser.user_id!!)
                            .child("is_typing")
                            .setValue(false)

                } else { // not typing

                    FirebaseDatabase.getInstance()
                            .reference
                            .child("Chats")
                            .child("Typing")
                            .child(currentUser.user_id!!)
                            .child("is_typing")
                            .setValue(true)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    /**
     * sets refresh listener for the recycler view.
     */
    private fun setRefreshLayoutListener() {

        rcvRefreshLayout.setOnRefreshListener {

            FirebaseDatabase.getInstance()
                    .reference
                    .child("Messages")
                    .child(currentUser.user_id!!)
                    .child(contactUser.user_id!!)
                    .addListenerForSingleValueEvent(object: ValueEventListener{

                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            if (dataSnapshot.childrenCount > allMessages.size) {

                                try {
                                    removeChildEventListenerMore()
                                } catch (e : Exception) {
                                    Toast.makeText(this@ActivityHomeChat, e.localizedMessage.toString(), Toast.LENGTH_SHORT).show()
                                }

                                messagePosition = 0
                                setChildEventListenerMore()
                            }

                            else {
                                rcvRefreshLayout.setRefreshing(false)

                            }
                        }

                        // error
                        override fun onCancelled(databaseError: DatabaseError) { showErrorToast(databaseError) }
                    })
        }
    }

    /**
     * removes childEventListenerMore.
     */
    private fun removeChildEventListenerMore() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Messages")
                .child(currentUser.user_id!!)
                .child(contactUser.user_id!!)
                .removeEventListener(childEventListenerMore)
    }

    /**
     * sets child event listener for more messages.
     */
    private fun setChildEventListenerMore() {

        childEventListenerMore = object: ChildEventListener{

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {

                val tempMessage= dataSnapshot.getValue(ChatMessage::class.java)

                if(!presentRefreshedMessageId.equals(dataSnapshot.key)){
                    allMessages.add(messagePosition, tempMessage!!)
                    adapterRecHomeMessaging.notifyItemInserted(messagePosition)
                }

                else {
                    presentRefreshedMessageId = lastRefreshedMessageId
                }

                if(messagePosition==0){
                    lastRefreshedMessageId = dataSnapshot.key!!
                }

                messagePosition++
                rcvChatMessages.smoothScrollToPosition(0)
                rcvRefreshLayout.setRefreshing(false)
            }

        }

        addChildEventListenerMore()
    }

    /**
     * adds child event listener more.
     */
    private fun addChildEventListenerMore() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Messages")
                .child(currentUser.user_id!!)
                .child(contactUser.user_id!!)
                .orderByKey()
                .endAt(lastRefreshedMessageId)
                .limitToLast(MESSAGES_PER_PAGE)
                .addChildEventListener(childEventListenerMore)
    }

    /**
     * send new message and the new
     * message will be saved to database.
     */
    fun sendMessage(view: View) {

        if (etMessage.text.isNotEmpty()) {

            // getting attributes
            val message = etMessage.text.toString()
            val time = System.currentTimeMillis().toString()

            // saving the message to Chat
            val chatCurrentUser = Chat(true, time, contactUser.user_id, message)
            val chatContactUser = Chat(false, time, currentUser.user_id, message)

            FirebaseDatabase.getInstance()
                    .reference
                    .child("Chats")
                    .child(contactUser.user_id!!)
                    .child(currentUser.user_id!!)
                    .setValue(chatContactUser)

            FirebaseDatabase.getInstance()
                    .reference
                    .child("Chats")
                    .child(currentUser.user_id!!)
                    .child(contactUser.user_id!!)
                    .setValue(chatCurrentUser)

            // saving the message to Messages
            val chatMessageCurrent = ChatMessage(currentUser.user_id, message, true, time, "text")
            val chatMessageContact = ChatMessage(currentUser.user_id, message, false, time, "text")

            // getting message id.
            val messageId = FirebaseDatabase.getInstance()
                    .reference
                    .child("Messages")
                    .child(currentUser.user_id!!)
                    .child(contactUser.user_id!!)
                    .push().key!!

            FirebaseDatabase.getInstance()
                    .reference
                    .child("Messages")
                    .child(currentUser.user_id!!)
                    .child(contactUser.user_id!!)
                    .child(messageId)
                    .setValue(chatMessageCurrent)

            FirebaseDatabase.getInstance()
                    .reference
                    .child("Messages")
                    .child(contactUser.user_id!!)
                    .child(currentUser.user_id!!)
                    .child(messageId)
                    .setValue(chatMessageContact)

            // making message empty.
            etMessage.setText("")
        }
    }

    /**
     * removes typing data from database.
     */
    private fun removeTypingDataFromDatabase() {

        // making typing false.
        FirebaseDatabase.getInstance()
                .reference
                .child("Chats")
                .child("Typing")
                .child(currentUser.user_id!!)
                .child("is_typing")
                .setValue(false).addOnCompleteListener { task ->

                    // removing typing data from database.
                    if (task.isSuccessful)
                        FirebaseDatabase.getInstance()
                                .reference
                                .child("Chats")
                                .child("Typing")
                                .child(currentUser.user_id!!)
                                .child("is_typing")
                                .removeValue()
                }
    }

    /**
     * shows error toast on the layout.
     * @param error is the error.
     */
    fun showErrorToast(error: DatabaseError) {
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * gets contact users data and adds
     * the required valueEventListener.
     */
    @Subscribe(sticky = true)
    fun getChatUsersData(event: EventBusDataEvent.SendProfileData){
        contactUser = event.profileData
        addValueEventListener()
    }

    /**
     * registers Activity to EventBus.
     * Adds the Auth state listener.
     */
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        FirebaseAuth.getInstance().addAuthStateListener(mAuth)
    }

    /**
     * removes all event listeners and
     * unregisters Activity from EventBus.
     * Removes the Auth state listener.
     */
    override fun onStop() {
        super.onStop()

        FirebaseAuth.getInstance().removeAuthStateListener(mAuth)

        FirebaseDatabase.getInstance()
                .reference
                .child("Chats")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(contactUser.user_id!!)
                .child("_seen")
                .setValue(true)
                .addOnCompleteListener { task ->

                    if(task.isSuccessful) {
                        removeChildEventListener()
                        removeTypingDataFromDatabase()
                        removeValueEventListener()
                        EventBus.getDefault().unregister(this)
                    }
                }
    }
}