package com.fatihsevban.instakotlinapp.Utils

import com.fatihsevban.instakotlinapp.Models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object Notifications {

    // constants
    val NEW_FOLLOW_REQUEST_NOTIFICATION = "0"
    val STARTED_TO_FOLLOW_NOTIFICATION = "2"
    val LIKED_POST_NOTIFICATION = "3"

    // properties

    /**
     * records a new follow request notification in database.
     * @param profileUserId is the corresponding users id.
     */
    fun newFollowRequestNotification(profileUserId: String) {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        val time = System.currentTimeMillis()
        val notificationId = FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .push().key!!


        val notification = Notification(time, NEW_FOLLOW_REQUEST_NOTIFICATION,
                notificationId, currentUserId, null)

        FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .child(notificationId)
                .setValue(notification)
    }

    /**
     * removes an existing follow request notification in database.
     * @param profileUserId is the corresponding users id.
     */
    fun removeFollowRequestNotification(profileUserId: String) {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .orderByChild("notification_type")
                .equalTo(NEW_FOLLOW_REQUEST_NOTIFICATION)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (data in dataSnapshot.children) {

                            val followRequestNotification = data.getValue(Notification::class.java)!!

                            if ( followRequestNotification.user_id.toString().equals(currentUserId) ) {

                                FirebaseDatabase.getInstance()
                                        .reference
                                        .child("MyNotifications")
                                        .child(profileUserId)
                                        .child(followRequestNotification.notification_id!!)
                                        .removeValue()

                                break
                            }
                        }
                    }

                    // error
                    override fun onCancelled(databaseError: DatabaseError) {
                        println(databaseError.message)
                    }
                })
    }

    /**
     * records a new follow notification in database.
     * @param profileUserId is the corresponding users id.
     */
    fun startedToFollowNotification(profileUserId: String) {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        val time = System.currentTimeMillis()
        val notificationId = FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .push().key!!

        val notification = Notification(time, STARTED_TO_FOLLOW_NOTIFICATION,
                notificationId, currentUserId, null)

        FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .child(notificationId)
                .setValue(notification)
    }

    /**
     * removes a follow notification in database.
     * @param profileUserId is the corresponding users id.
     */
    fun abandonedToFollowNotification(profileUserId: String) {

        FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .orderByChild("notification_type")
                .equalTo(STARTED_TO_FOLLOW_NOTIFICATION)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (data in dataSnapshot.children) {

                            val notification = data.getValue(Notification::class.java)!!

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("MyNotifications")
                                    .child(profileUserId)
                                    .child(notification.notification_id!!)
                                    .removeValue()
                        }
                    }

                    // error
                    override fun onCancelled(databaseError: DatabaseError) {
                        println(databaseError.message)
                    }
                })
    }

    /**
     * records a liked post notification in database.
     * @param profileUserId is the corresponding users id.
     * @param postId is the corresponding post id.
     */
    fun likedPostNotification(profileUserId: String, postId: String) {

        val time = System.currentTimeMillis()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        val notificationId = FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .push().key!!

        val notification = Notification(time, LIKED_POST_NOTIFICATION,
                notificationId, currentUserId, postId)

        FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .child(notificationId)
                .setValue(notification)
    }

    /**
     * removes a liked post notification in database.
     * @param profileUserId is the corresponding users id.
     * @param postId is the corresponding post id.
     */
    fun removeLikedPostNotification(profileUserId: String, postId: String) {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        FirebaseDatabase.getInstance()
                .reference
                .child("MyNotifications")
                .child(profileUserId)
                .orderByChild("post_id")
                .equalTo(postId)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (data in dataSnapshot.children) {

                            val notification = data.getValue(Notification::class.java)!!

                            if (notification.user_id.toString().equals(currentUserId)) {

                                FirebaseDatabase.getInstance()
                                        .reference
                                        .child("MyNotifications")
                                        .child(profileUserId)
                                        .child(notification.notification_id!!)
                                        .removeValue()
                            }
                        }
                    }

                    // error
                    override fun onCancelled(databaseError: DatabaseError) {
                        println(databaseError.message)
                    }

                })
    }
}