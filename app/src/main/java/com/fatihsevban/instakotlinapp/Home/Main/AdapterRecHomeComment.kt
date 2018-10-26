package com.fatihsevban.instakotlinapp.Home.Main

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Models.Comment
import com.fatihsevban.instakotlinapp.Models.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.list_item_comment.view.*
import android.text.Html

class AdapterRecHomeComment(options: FirebaseRecyclerOptions<Comment>, val context: Context): FirebaseRecyclerAdapter<Comment, AdapterRecHomeComment.MyViewHolder>(options) {

    // properties
    val inflater = LayoutInflater.from(context)
    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.list_item_comment, parent, false)
        return MyViewHolder(view)
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Comment) {

        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(model.user_id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val commentMaker = dataSnapshot.getValue(User::class.java)!!

                        setListItemUserInterface( commentMaker, holder, model)

                        holder.imgLike.setOnClickListener { view ->
                            likeComment(commentMaker, model, holder)
                        }
                    }

                    // error
                    override fun onCancelled(databaseError: DatabaseError) { showErrorToast(databaseError) }

                })

    }

    /**
     * likes the diplayed comment on the layout and saves to database.
     * @param commentMaker is the user that made the comment.
     * @param holder is the holder that holds the corresponding views.
     * @param model is the comment that will be liked.
     */
    private fun likeComment(commentMaker: User, model: Comment, holder: MyViewHolder) {

        FirebaseDatabase.getInstance().reference
                .child("Comments")
                .child(model.post_id!!)
                .child(model.comment_id!!)
                .child("Likers")
                .addListenerForSingleValueEvent(object : ValueEventListener {


                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        // user liked the comment before.
                        if (dataSnapshot.hasChild(currentUserId)) {

                            FirebaseDatabase.getInstance().reference
                                    .child("Comments")
                                    .child(model.post_id!!)
                                    .child(model.comment_id!!)
                                    .child("Likers")
                                    .child(currentUserId)
                                    .removeValue()

                        } else { // user likes the comment new.

                            FirebaseDatabase.getInstance().reference
                                    .child("Comments")
                                    .child(model.post_id!!)
                                    .child(model.comment_id!!)
                                    .child("Likers")
                                    .child(currentUserId)
                                    .setValue(currentUserId)
                        }
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) {
                        showErrorToast(error)
                    }

                })
    }

    /**
     * set List Items User Interface.
     * @param commentMaker is the user that made the comment.
     * @param holder is the holder that holds the corresponding views.
     * @param model is the comment that will be displayed in the layout.
     */
    private fun setListItemUserInterface(commentMaker: User, holder: MyViewHolder, model: Comment) {

        val sourceString = "<b><font color=black>${commentMaker.user_name}:</font></b> ${model.comment_content}"
        holder.tvUsernameAndExplanation.text = Html.fromHtml(sourceString)

        // setting Date
        val noteDate = model.comment_date

        val convertedDate = DateUtils.getRelativeTimeSpanString( noteDate!!,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, 0)

        holder.tvDate2.text = convertedDate

        // setting profile image
        if (!commentMaker.profile_picture!!.toString().equals("not determined"))
            UniversalImageLoader.setImage(commentMaker.profile_picture!!,
                    holder.imgProfile, null, "")

        else {
            holder.imgProfile.setImageResource(R.drawable.icon_profile)
        }

        // setting like count and state
        FirebaseDatabase.getInstance().reference
                .child("Comments")
                .child(model.post_id!!)
                .child(model.comment_id!!)
                .child("Likers")
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        // setting like count
                        holder.tvLikeCount.text = "${dataSnapshot.childrenCount.toString()} likes."

                        // hiding comment like symbol when comment made by current user.
                        if (commentMaker.user_id.toString().equals(currentUserId))
                            holder.imgLike.visibility = View.INVISIBLE

                        else { // setting like state

                            if (dataSnapshot.hasChild(FirebaseAuth.getInstance().currentUser!!.uid)) {
                                holder.imgLike.setImageResource(R.drawable.icon_like_2)
                            } else {
                                holder.imgLike.setImageResource(R.drawable.icon_like)
                            }
                        }
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                })
    }

    /**
     * shows error toast on the layout.
     * @param error is the error.
     */
    fun showErrorToast(error: DatabaseError) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {
        val listItem = itemView as ConstraintLayout
        val tvUsernameAndExplanation = listItem.tvUsernameAndExplanation
        val tvDate2 = listItem.tvDate2
        val tvLikeCount = listItem.tvLikeCount
        val imgProfile = listItem.imgProfile
        val imgLike = listItem.imgLike
    }

}