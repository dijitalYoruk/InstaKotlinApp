package com.fatihsevban.instakotlinapp.Home.Chatting

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Home.HomeActivity.ActivityHome
import com.fatihsevban.instakotlinapp.Models.Chat
import com.fatihsevban.instakotlinapp.Models.User
import kotlinx.android.synthetic.main.list_item_profile.view.*
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AdapterRecHomeFollowing(val context: Context, var users: ArrayList<User>): RecyclerView.Adapter<AdapterRecHomeFollowing.MyViewHolder>() , Filterable {

    // properties
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
    val inflater = LayoutInflater.from(context)
    var mFilter = FilterHelperFollowing(this, users)

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.list_item_profile, parent, false)
        return MyViewHolder(view)
    }

    /**
     * gets the item count.
     */
    override fun getItemCount(): Int {
        return users.size
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val contactUser = users[position]
        holder.listItem.setOnClickListener { initialiseChat(contactUser) }
        setListItemUI(contactUser, holder)
    }

    /**
     * set list items user interface according to contact user.
     * @param contactUser is the contact user.
     * @param holder holds all the corresponding views.
     */
    private fun setListItemUI(contactUser: User, holder: MyViewHolder) {

        holder.tvNameAndSurname.text = contactUser.name_and_surname
        holder.tvUsername.text = contactUser.user_name

        if (!contactUser.profile_picture!!.equals("not determined"))
            UniversalImageLoader.setImage(contactUser.profile_picture!!,
                    holder.imgProfile, null, "")

        else {
            holder.imgProfile.setImageResource(R.drawable.icon_profile)
        }
    }

    /**
     * initialises a chat with the contact user.
     */
    private fun initialiseChat(contactUser: User) {

        FirebaseDatabase.getInstance()
                .reference
                .child("Chats")
                .child(currentUserId)
                .child(contactUser.user_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(context, databaseError.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (!dataSnapshot.exists()) {

                            // constructing chats
                            val chatCurrentUser = Chat(true, System.currentTimeMillis().toString(),
                                    contactUser.user_id, "no chat available")

                            val chatContactUser = Chat(true, System.currentTimeMillis().toString(),
                                    currentUserId,"no chat available")

                            // saving to database
                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("Chats")
                                    .child(contactUser.user_id!!)
                                    .child(currentUserId)
                                    .setValue(chatContactUser)


                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("Chats")
                                    .child(currentUserId)
                                    .child(contactUser.user_id!!)
                                    .setValue(chatCurrentUser)
                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful)
                                            (context as ActivityHome).onBackPressed()
                                    }

                        } else {
                            (context as ActivityHome).onBackPressed()
                        }
                    }
                })
    }

    /**
     * gets filter for search view.
     */
    override fun getFilter(): Filter { return mFilter }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {
        var listItem = itemView as ConstraintLayout
        val tvUsername = listItem.tvUsername
        val tvNameAndSurname = listItem.tvNameAndSurname
        val imgProfile = listItem.imgProfile
    }

}