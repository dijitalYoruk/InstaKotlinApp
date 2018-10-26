package com.fatihsevban.instakotlinapp.Home.Chatting

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatihsevban.instakotlinapp.Home.HomeActivity.ActivityHome
import com.fatihsevban.instakotlinapp.Models.Chat
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.BottomNavigationViewHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_chats.view.*

class FragmentHomeChats: Fragment() {

    // properties
    lateinit var mainView: View
    lateinit var adapterRecHomeChats: AdapterRecHomeChats
    lateinit var childEventListener: ChildEventListener
    var isChildEventListenerAdded = false
    var chatData = ArrayList<Chat>()

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout and adding listeners.
        mainView = inflater.inflate(R.layout.fragment_home_chats, container, false)
        mainView.floatingActionButton.setOnClickListener { goToFragmentFollowing() }

        setRecyclerView()
        setChildEventListener()
        setupBottomNavigationView()

        return mainView
    }

    /**
     * goes to FragmentHomeFollowing to add new chat users.
     */
    private fun goToFragmentFollowing() {
        (activity as ActivityHome).homeContainerLayout?.visibility = View.VISIBLE
        (activity as ActivityHome).viewPagerHome?.visibility = View.GONE
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.homeContainerLayout, FragmentHomeFollowing())
        transaction.addToBackStack("ADD FRAG HOME FOLLOWING")
        transaction.commit()
    }

    /**
     * sets recyclerview chats.
     */
    private fun setRecyclerView() {
        // setting adapter
        adapterRecHomeChats = AdapterRecHomeChats(context!!, chatData)
        mainView.rcvChats.adapter = adapterRecHomeChats

        // setting layout manager.
        val linearLayoutManager = LinearLayoutManager(context!!)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mainView.rcvChats.setLayoutManager(linearLayoutManager)
    }

    /**
     * sets child event listener for upcoming chats.
     */
    private fun setChildEventListener() {

        childEventListener = object: ChildEventListener{

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

            // updating last chat message.
            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {

                val updatedChat = dataSnapshot.getValue(Chat::class.java)

                for (position in 0 until chatData.size) {

                    // finding the corresponding message
                    if (chatData[position].contact_id.toString().equals(updatedChat?.contact_id.toString())) {

                        chatData.removeAt(position)
                        adapterRecHomeChats.notifyItemRemoved(position)
                        adapterRecHomeChats.notifyItemRangeChanged(position, chatData.size)

                        chatData.add(0, updatedChat!!)
                        adapterRecHomeChats.notifyItemInserted( 0 )
                        adapterRecHomeChats.notifyItemRangeChanged(0, chatData.size)

                        break
                    }
                }
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {

                // adding new Chat.
                val newChat = dataSnapshot.getValue(Chat::class.java)!!
                chatData.add(0, newChat)
                adapterRecHomeChats.notifyItemInserted( 0 )
                adapterRecHomeChats.notifyItemRangeChanged(0, chatData.size)
            }
        }
    }

    /**
     * setting up bottom navigation view.
     */
    private fun setupBottomNavigationView() {
        val activityNo = (activity as ActivityHome).ACTIVITY_NO
        BottomNavigationViewHelper.setupBottomNavigationView(context!!, mainView.bottomNavigationViewEx)
        mainView.bottomNavigationViewEx.menu.getItem(activityNo).isChecked = true
    }

    /**
     * adds child event listener.
     */
    private fun addChildEventListener() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Chats")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .orderByChild("time")
                .addChildEventListener(childEventListener)

        isChildEventListenerAdded = true
    }

    /**
     * removes child event listener.
     */
    private fun removeChildEventListener() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Chats")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .orderByChild("time")
                .removeEventListener(childEventListener)

        isChildEventListenerAdded = false
        chatData.clear()
        adapterRecHomeChats.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        // setting bottomNavigationView current item.
        val activityNo = (activity as ActivityHome).ACTIVITY_NO
        mainView.bottomNavigationViewEx.menu.getItem(activityNo).isChecked = true

        if (!isChildEventListenerAdded)
            addChildEventListener()
    }

    override fun onPause() {
        super.onPause()

        if (isChildEventListenerAdded)
            removeChildEventListener()
    }
}