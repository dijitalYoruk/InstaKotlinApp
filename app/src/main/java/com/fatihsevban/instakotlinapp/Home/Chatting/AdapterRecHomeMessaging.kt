package com.fatihsevban.instakotlinapp.Home.Chatting

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatihsevban.instakotlinapp.Models.ChatMessage
import kotlinx.android.synthetic.main.list_item_message_contact.view.*
import com.fatihsevban.instakotlinapp.R
import com.google.firebase.auth.FirebaseAuth

class AdapterRecHomeMessaging (val context: Context, var messages: ArrayList<ChatMessage>): RecyclerView.Adapter<AdapterRecHomeMessaging.MyViewHolder>(){

    // constants
    val CURRENT = 100
    val CONTACT = 200

    // properties
    val layoutInflater = LayoutInflater.from(context)
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        // inflating layout for current user.
        if (viewType == CURRENT) {
            val view = layoutInflater.inflate(R.layout.list_item_message_current, parent, false)
            return MyViewHolder(view)
        }

        else { // inflating layout for contact user.
            val view = layoutInflater.inflate(R.layout.list_item_message_contact, parent, false)
            return MyViewHolder(view)
        }

    }

    /**
     * assigns each list item the corresponding layout.
     * @param position is the positio of the list item.
     */
    override fun getItemViewType(position: Int): Int {

        val message = messages[position]

        if (message.message_sender_uid!!.equals(currentUserId))
            return CURRENT

        else return CONTACT
    }

    /**
     * gets the item count.
     */
    override fun getItemCount(): Int {
        return messages.size
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvMessage.text = messages[position].chat_message
    }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {
        var listItem = itemView as ConstraintLayout
        val tvMessage = listItem.tvMessage
    }

}