package com.fatihsevban.instakotlinapp.News

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Models.Notification
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.Utils.Notifications
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.list_item_notif_follow_request.view.*
import kotlinx.android.synthetic.main.list_item_notif_follow_started.view.*
import kotlinx.android.synthetic.main.list_item_notif_post_liked.view.*

class AdapterRecNewsYou (val context: Context, val notifications: ArrayList<Notification>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // properties
    val layoutInflater = LayoutInflater.from(context)

    /**
     * assigns each list item the corresponding layout.
     * @param position is the positio of the list item.
     */
    override fun getItemViewType(position: Int): Int {

        val notification = notifications[position]

        when {

            // STARTED_TO_FOLLOW_NOTIFICATION
            notification.notification_type!!.equals(Notifications.STARTED_TO_FOLLOW_NOTIFICATION) -> {
                return Notifications.STARTED_TO_FOLLOW_NOTIFICATION.toInt()
            }

            // LIKED_POST_NOTIFICATION
            notification.notification_type!!.equals(Notifications.LIKED_POST_NOTIFICATION) -> {
                return Notifications.LIKED_POST_NOTIFICATION.toInt()
            }

            // NEW_FOLLOW_REQUEST_NOTIFICATION
            notification.notification_type!!.equals(Notifications.NEW_FOLLOW_REQUEST_NOTIFICATION) -> {
                return Notifications.NEW_FOLLOW_REQUEST_NOTIFICATION.toInt()
            }
        }

        return super.getItemViewType(position)
    }

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {

            // STARTED_TO_FOLLOW_NOTIFICATION
            Notifications.STARTED_TO_FOLLOW_NOTIFICATION.toInt() -> {
                val view = layoutInflater.inflate(R.layout.list_item_notif_follow_started, parent, false)
                return ViewHolderFollowStarted(view)
            }

            // LIKED_POST_NOTIFICATION
            Notifications.LIKED_POST_NOTIFICATION.toInt() -> {
                val view = layoutInflater.inflate(R.layout.list_item_notif_post_liked, parent, false)
                return ViewHolderPostLiked(view)
            }

            // NEW_FOLLOW_REQUEST_NOTIFICATION
            Notifications.NEW_FOLLOW_REQUEST_NOTIFICATION.toInt() -> {
                val view = layoutInflater.inflate(R.layout.list_item_notif_follow_request, parent, false)
                return ViewHolderFollowRequest(view)
            }

            else -> {
                val view = layoutInflater.inflate(R.layout.list_item_notif_follow_started, parent, false)
                return ViewHolderFollowStarted(view)
            }
        }
    }

    /**
     * gets the item count.
     */
    override fun getItemCount(): Int {
        return notifications.size
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val notification = notifications[position]

        when(holder) {

            is ViewHolderFollowRequest -> {

                FirebaseDatabase.getInstance()
                        .reference
                        .child("Users")
                        .child(notification.user_id!!)
                        .addListenerForSingleValueEvent(object: ValueEventListener{

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val user = dataSnapshot.getValue(User::class.java)!!
                                setFollowRequestUI(user, holder, notification)
                            }

                            // error
                            override fun onCancelled(databaseError: DatabaseError) { showErrorToast(databaseError) }
                        })
            }

            is ViewHolderFollowStarted -> {

                FirebaseDatabase.getInstance()
                        .reference
                        .child("Users")
                        .child(notification.user_id!!)
                        .addListenerForSingleValueEvent(object: ValueEventListener{

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val user = dataSnapshot.getValue(User::class.java)!!
                                setFollowStartedUI(user, holder, notification)
                            }

                            // error
                            override fun onCancelled(databaseError: DatabaseError) { showErrorToast(databaseError) }
                        })
            }

            is ViewHolderPostLiked -> {
                setPostLikedUI(notification, holder)
            }
        }
    }

    /**
     * sets Post Liked Notification User Interface.
     * @param holder is the holder that holds all the related views.
     * @param notification is the corresponding notification.
     */
    private fun setPostLikedUI(notification: Notification, holder: AdapterRecNewsYou.ViewHolderPostLiked) {

        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(notification.user_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val user = dataSnapshot.getValue(User::class.java)!!

                        if (!user.profile_picture!!.equals("not determined"))
                            UniversalImageLoader.setImage(user.profile_picture!!,
                                    holder.imgUserProfile, null, "")

                        else {
                            holder.imgUserProfile.setImageResource(R.drawable.icon_profile)
                        }

                        holder.tvNotification.text = "${user.user_name} liked your post."

                    }

                    // error
                    override fun onCancelled(databaseError: DatabaseError) {
                        showErrorToast(databaseError)
                    }

                })

        FirebaseDatabase.getInstance()
                .reference
                .child("Posts")
                .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                .child(notification.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val post = dataSnapshot.getValue(Post::class.java)!!
                        UniversalImageLoader.setImage(post.photo_uri!!, holder.imgPost, null, "")
                    }

                    // error
                    override fun onCancelled(databaseError: DatabaseError) {
                        showErrorToast(databaseError)
                    }

                })
    }

    /**
     * sets Follow Started Notification User Interface.
     * @param user is the corresponding user.
     * @param holder is the holder that holds all the related views.
     * @param notification is the corresponding notification.
     */
    private fun setFollowStartedUI(user: User, holder: ViewHolderFollowStarted, notification: Notification) {

        // setting profile image
        if (!user.profile_picture!!.equals("not determined"))
            UniversalImageLoader.setImage(user.profile_picture!!,
                    holder.imgUserProfile, null, "")

        else {
            holder.imgUserProfile.setImageResource(R.drawable.icon_profile)
        }

        holder.tvNotifFollowStarted.text = "${user.user_name} started to follow you."
    }

    /**
     * sets Follow Request Notification User Interface.
     * @param user is the corresponding user.
     * @param holder is the holder that holds all the related views.
     * @param notification is the corresponding notification.
     */
    private fun setFollowRequestUI(user: User, holder: ViewHolderFollowRequest, notification: Notification) {

        holder.tvUsername.text = user.user_name
        holder.tvNameAndSurname.text =user.name_and_surname

        // setting profile image
        if (!user.profile_picture!!.equals("not determined"))
            UniversalImageLoader.setImage(user.profile_picture!!,
                    holder.imgUserProfile, null, "")

        else {
            holder.imgUserProfile.setImageResource(R.drawable.icon_profile)
        }

        // setting onClick listeners.
        holder.btnConfirm.setOnClickListener { view ->
            acceptfollowRequest(user)
            removeFollowRequest(user, notification)
        }

        holder.btnDelete.setOnClickListener { view ->
            removeFollowRequest(user, notification)
        }
    }

    /**
     * shows error toast on the layout.
     * @param error is the error.
     */
    private fun showErrorToast(error: DatabaseError) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * denies the follow request and removes from database.
     * @param user is the corresponding user.
     */
    private fun removeFollowRequest(user: User, notification: Notification) {

        FirebaseDatabase.getInstance()
                .reference
                .child("Follow Requests")
                .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                .child(user.user_id!!)
                .removeValue()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        FirebaseDatabase.getInstance()
                                .reference
                                .child("MyNotifications")
                                .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                                .child(notification.notification_id!!)
                                .removeValue().addOnCompleteListener { task ->

                                    if (task.isSuccessful) {
                                        (context as ActivityNews).refreshNotifications()
                                    }

                                }
                    }
                }

    }

    /**
     * accepts the follow request and records to database.
     * @param user is the corresponding user.
     */
    private fun acceptfollowRequest(user: User) {

        FirebaseDatabase.getInstance()
                .reference
                .child("following")
                .child(user.user_id!!)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(FirebaseAuth.getInstance().currentUser!!.uid)

        FirebaseDatabase.getInstance()
                .reference
                .child("followers")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(user.user_id!!)
                .setValue(user.user_id!!)

        val time = System.currentTimeMillis()
        val notificationId = FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .push().key!!


        val notification = Notification(time, Notifications.STARTED_TO_FOLLOW_NOTIFICATION,
                notificationId, user.user_id!!, null)

        FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(notificationId)
                .setValue(notification)
    }

    /**
     * holds all the required views of each list item.
     */
    inner class ViewHolderFollowRequest(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var listItem = itemView as ConstraintLayout
        val tvUsername = listItem.tvUserName
        val tvNameAndSurname = listItem.tvNameAndSurname
        val imgUserProfile = listItem.imgUserProfile
        val btnConfirm = listItem.btnConfirm
        val btnDelete = listItem.btnDelete
    }

    /**
     * holds all the required views of each list item.
     */
    inner class ViewHolderFollowStarted(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var listItem = itemView as ConstraintLayout
        val imgUserProfile = listItem.imgUserProfile2
        val tvNotifFollowStarted = listItem.tvNotifFollowStarted
    }

    /**
     * holds all the required views of each list item.
     */
    inner class ViewHolderPostLiked(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var listItem = itemView as ConstraintLayout
        val imgUserProfile = listItem.imgUserProfile3
        val imgPost = listItem.imgPost
        val tvNotification = listItem.tvNotification
    }

}