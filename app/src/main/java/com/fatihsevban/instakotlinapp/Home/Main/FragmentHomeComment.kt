package com.fatihsevban.instakotlinapp.Home.Main

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Models.Comment
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home_comment.*
import kotlinx.android.synthetic.main.fragment_home_comment.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class FragmentHomeComment: Fragment() {

    // properties
    lateinit var post_id: String
    lateinit var mainView: View
    lateinit var adapterRecHomeComment: AdapterRecHomeComment
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_home_comment, container, false)

        setRecyclerView()

        // setting profile image
        setProfileImage()

        // setting onClick listeners.
        mainView.tvShareComment.setOnClickListener { view -> shareComment() }
        mainView.imgGetBack.setOnClickListener {activity?.onBackPressed()}

        return mainView
    }

    private fun setProfileImage() {

        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(currentUserId)
                .child("user_details")
                .child("profile_picture")
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val profileImageUri = dataSnapshot.value.toString()

                        UniversalImageLoader.setImage(profileImageUri, mainView.imgProfile,
                                null, "")
                    }
                })
    }

    /**
     * sets RecyclerView with FirebaseUI
     */
    private fun setRecyclerView() {

        // getting reference
        val mRef = FirebaseDatabase.getInstance()
                .reference
                .child("Comments")
                .child(post_id)

        // setting options.
        val options = FirebaseRecyclerOptions.Builder<Comment>()
                .setQuery(mRef, Comment::class.java).build()

        // setting adapter
        adapterRecHomeComment = AdapterRecHomeComment(options, context!!)
        mainView.rcvComments.adapter = adapterRecHomeComment

        // setting layout manager.
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mainView.rcvComments.setLayoutManager(linearLayoutManager)
    }

    /**
     * shares the comment and saves to the database.
     */
    private fun shareComment() {

        if (!etComment.text.isNullOrEmpty()) {

            val mRef = FirebaseDatabase.getInstance()
                    .reference
                    .child("Comments")
                    .child(post_id)

            // constructing comment Data
            val user_id = FirebaseAuth.getInstance().currentUser?.uid
            val post_id = this.post_id
            val comment_id = mRef.push().key
            val comment_content = mainView.etComment.text.toString()
            val comment_like_count = "0"
            val comment_date = System.currentTimeMillis()

            val comment = Comment(user_id, post_id, comment_id, comment_content, comment_like_count, comment_date)

            // saving comment to database.
            mRef.child(comment_id!!).setValue(comment).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Comment Uploaded", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            mainView.etComment.setText("")
        }
    }

    /**
     * gets post id data through Event Bus
     * @param postIdData is the post id data.
     */
    @Subscribe(sticky = true)
    fun getPostId(postIdData: EventBusDataEvent.SendPostIdData) {
        post_id = postIdData.postId
    }

    /**
     * stars listening database.
     */
    override fun onStart() {
        super.onStart()
        adapterRecHomeComment.startListening()
    }

    /**
     * stops listening database.
     */
    override fun onStop() {
        super.onStop()
        adapterRecHomeComment.stopListening()
    }

    /**
     * registers EventBus from the fragment.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        EventBus.getDefault().register(this)

    }

    /**
     * unregisters EventBus from the fragment.
     */
    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

}