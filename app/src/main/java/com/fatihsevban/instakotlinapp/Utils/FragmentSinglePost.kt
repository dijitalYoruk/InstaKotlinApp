package com.fatihsevban.instakotlinapp.Utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Home.Main.FragmentHomeComment
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.Profile.ActivityProfile
import com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings.ActivityProfileSettings
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Search.ActivitySearch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile_settings.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.fragment_single_post.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class FragmentSinglePost: Fragment() {


    // constants
    val CURRENT_USER_ID = FirebaseAuth.getInstance().currentUser?.uid!!

    // properties
    lateinit var mainView: View
    lateinit var singlePost: Post
    lateinit var postSender: User

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_single_post, container, false)

        // setting onClick listeners
        mainView.imgGetBack.setOnClickListener { activity?.onBackPressed() }
        mainView.imgLike.setOnClickListener { likePost() }
        mainView.imgComment.setOnClickListener{ goToCommentFragment() }

        // setting user interface
        FirebaseDatabase.getInstance()
                .reference
                .child("Users")
                .child(singlePost.user_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        postSender = dataSnapshot.getValue(User::class.java)!!
                        mainView.tvUserName.text = postSender.user_name

                        // setting explanation with username.
                        val sourceString = "<b><font color=black>${postSender.user_name}" +
                                ":</font></b> ${singlePost.explanation}"
                        mainView.tvUsernameAndExplanation.text = Html.fromHtml(sourceString)

                        setCommentAndLikeData()
                        setProfileImage()
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
                })

        setImageOrVideo()
        setPostDate()
        return mainView
    }

    /**
     * sets post date.
     */
    private fun setPostDate() {
        // date
        val noteDate = singlePost.upload_date!!

        val convertedDate = DateUtils.getRelativeTimeSpanString( noteDate,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, 0)

        mainView.tvDate.text = convertedDate
    }

    /**
     * sets image or video to the corresponding view in the layout.
     */
    private fun setImageOrVideo() {

        val imageOrVideo = singlePost.photo_uri!!
        val lastPointIndex = imageOrVideo.lastIndexOf(".")
        val docType = imageOrVideo.substring(lastPointIndex, lastPointIndex+4)

        // video
        if (docType.equals(".mp4")) {

            mainView.video_layout.visibility = View.VISIBLE
            mainView.imgPost.visibility = View.INVISIBLE

            mainView.videoView.setVideoURI(Uri.parse(singlePost.photo_uri))
            mainView.videoView.seekTo(200)

            mainView.tvImageOrVideo.text = "Video"
            mainView.videoView.setMediaController(mainView.media_controller);


        } else { // photo

            mainView.imgPost.visibility = View.VISIBLE
            mainView.video_layout.visibility = View.INVISIBLE

            mainView.tvImageOrVideo.text = "Image"
            UniversalImageLoader.setImage(imageOrVideo, mainView.imgPost,
                    mainView.progressBarHome, "")
        }
    }

    /**
     * sets profile image of the post sender.
     */
    private fun setProfileImage() {
        // setting profile image
        if (!postSender.profile_picture!!.equals("not determined"))
            UniversalImageLoader.setImage(postSender.profile_picture!!,
                    mainView.imgProfile, mainView.progressBarHome, "")

        else {
            mainView.imgProfile.setImageResource(R.drawable.icon_profile)
        }
    }


    /**
     * records the post as liked by the current user in the database.
     */
    private fun likePost() {

        FirebaseDatabase.getInstance().reference
                .child("PostLikers")
                .child(singlePost.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        // user liked the post before.
                        if (dataSnapshot.hasChild(postSender.user_id!!)) {

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("PostLikers")
                                    .child(singlePost.post_id!!)
                                    .child(postSender.user_id!!)
                                    .removeValue()

                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {

                                            val likeCount = (dataSnapshot.childrenCount - 1).toString()
                                            mainView.tvPostLikeCount.text = "Liked $likeCount times."

                                            mainView.imgLike.setImageResource(R.drawable.icon_like)
                                            Notifications.removeLikedPostNotification(postSender.user_id!!, singlePost.post_id!!)
                                        }
                                    }

                        } else { // user likes the post new.

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("PostLikers")
                                    .child(singlePost.post_id!!)
                                    .child(postSender.user_id!!)
                                    .setValue(postSender.user_id!!)
                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {

                                            val likeCount = (dataSnapshot.childrenCount + 1).toString()
                                            mainView.tvPostLikeCount.text = "Liked $likeCount times."

                                            mainView.imgLike.setImageResource(R.drawable.icon_like_2)
                                            Notifications.likedPostNotification(postSender.user_id!!, singlePost.post_id!!)
                                        }
                                    }
                        }
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
                })
    }

    /**
     * goes to comment fragment and enables to see the comments for the post.
     */
    private fun goToCommentFragment() {

        if (context is ActivityProfile) {

            (context as ActivityProfile).profileContainerLayout.visibility = View.VISIBLE
            (context as ActivityProfile).profileRootLayout.visibility = View.INVISIBLE

            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.replace(R.id.profileContainerLayout, FragmentHomeComment())
            transaction.addToBackStack("ADD FRAG HOME COMMENT")
            transaction.commit()

            EventBus.getDefault().postSticky(EventBusDataEvent.SendPostIdData(singlePost.post_id!!))
        }

        else if (context is ActivitySearch) {

            (context as ActivitySearch).searchContainerLayout.visibility = View.VISIBLE
            (context as ActivitySearch).searchRootLayout.visibility = View.INVISIBLE

            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.replace(R.id.searchContainerLayout, FragmentHomeComment())
            transaction.addToBackStack("ADD FRAG HOME COMMENT")
            transaction.commit()

            EventBus.getDefault().postSticky(EventBusDataEvent.SendPostIdData(singlePost.post_id!!))

        }

        else if (context is ActivityProfileSettings) {

            (context as ActivityProfileSettings).profileSettingsContainerLayout.visibility = View.VISIBLE
            (context as ActivityProfileSettings).profileSettingsRootLayout.visibility = View.INVISIBLE

            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.replace(R.id.profileSettingsContainerLayout, FragmentHomeComment())
            transaction.addToBackStack("ADD FRAG HOME COMMENT")
            transaction.commit()

            EventBus.getDefault().postSticky(EventBusDataEvent.SendPostIdData(singlePost.post_id!!))

        }



    }

    /**
     * sets comment and like data in the layout.
     */
    private fun setCommentAndLikeData() {

        // setting likes text view.
        FirebaseDatabase.getInstance().reference
                .child("PostLikers")
                .child(singlePost.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val likeCount = dataSnapshot.childrenCount.toString()
                        mainView.tvPostLikeCount.text = "Liked $likeCount times."

                        if (dataSnapshot.hasChild(CURRENT_USER_ID)) {
                            mainView.imgLike.setImageResource(R.drawable.icon_like_2)
                        } else {
                            mainView.imgLike.setImageResource(R.drawable.icon_like)
                        }
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
                })


        // setting comments text view
        FirebaseDatabase.getInstance()
                .reference
                .child("Comments")
                .child(singlePost.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val commentCount = dataSnapshot.childrenCount.toString()
                        mainView.tvCommentCount.text = "$commentCount Comments"
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                })
    }

    /**
     * shows Error Message through Toast.
     * @param error is the error to be displayed.
     */
    fun showErrorToast(error: DatabaseError) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }


    /**
     * gets image or video uri and the specific doc type.
     */
    @Subscribe(sticky = true)
    fun getPostData(postData: EventBusDataEvent.SendPostData) {
        singlePost = postData.post
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
        mainView.videoView.stopPlayback()
    }

}