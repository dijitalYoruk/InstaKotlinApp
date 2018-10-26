package com.fatihsevban.instakotlinapp.Home.Chatting

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import org.greenrobot.eventbus.EventBus
import com.fatihsevban.instakotlinapp.Models.Chat
import com.fatihsevban.instakotlinapp.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.list_item_chat.view.*


class AdapterRecHomeChats (val context: Context, var chats: ArrayList<Chat>): RecyclerView.Adapter<AdapterRecHomeChats.MyViewHolder>(){

    // properties
    val inflater = LayoutInflater.from(context)

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.list_item_chat, parent, false)
        return MyViewHolder(view)
    }

    /**
     * gets the item count.
     */
    override fun getItemCount(): Int {
        return chats.size
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val chat = chats[position]

        // getting contact user data.
        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(chat.contact_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val contactUser = dataSnapshot.getValue(User::class.java)!!
                        setListUI(contactUser, holder, chat)
                        holder.listItem.setOnClickListener { directUserToChatActivity(contactUser) }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(context, databaseError.message, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun directUserToChatActivity(contactUser: User) {

        // recording chat as seen
        FirebaseDatabase.getInstance()
                .reference
                .child("Chats")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(contactUser.user_id!!)
                .child("_seen")
                .setValue(true)
                .addOnCompleteListener { task ->

                    // directing user ActivityHomeChat
                    EventBus.getDefault().postSticky(EventBusDataEvent.SendProfileData(contactUser))
                    val intent = Intent(context, ActivityHomeChat::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    context.startActivity(intent)

                }
    }

    /**
     * sets list user interface.
     * @param contactUser is the user whose profile will be displayed.
     * @param holder is the holder that holds the corresponding views.
     * @param chat is the chat that will be displayed in the list.
     */
    private fun setListUI(contactUser: User, holder: MyViewHolder, chat: Chat) {

        holder.tvLastChatMessage.text = chat.last_message
        holder.tvUsername.text = contactUser.user_name

        // date
        val noteDate = chat.time!!.toLong()

        val convertedDate = DateUtils.getRelativeTimeSpanString( noteDate,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, 0)

        holder.tvDate.text = convertedDate

        // setting layout according to chat is seen or not.
        if (!chat.is_seen!!) {
            holder.imgIsSeen.visibility = View.VISIBLE
            holder.tvUsername.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            holder.tvLastChatMessage.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            holder.tvUsername.setTextColor(ContextCompat.getColor(context, android.R.color.black))
            holder.tvLastChatMessage.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        } else {
            holder.imgIsSeen.visibility = View.INVISIBLE
            holder.tvUsername.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            holder.tvLastChatMessage.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            holder.tvUsername.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            holder.tvLastChatMessage.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }

        // setting profile image
        if (!contactUser.profile_picture!!.equals("not determined")) {
            UniversalImageLoader.setImage(contactUser.profile_picture!!,
                    holder.imgProfile, null, "")
        } else {
            holder.imgProfile.setImageResource(R.drawable.icon_profile)
        }
    }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {
        var listItem = itemView as ConstraintLayout
        val tvUsername = listItem.tvUsername
        val tvLastChatMessage = listItem.tvLastChatMessage
        val tvDate = listItem.tvDate
        val imgProfile = listItem.imgProfile
        val imgIsSeen = listItem.imgIsSeen
    }
}