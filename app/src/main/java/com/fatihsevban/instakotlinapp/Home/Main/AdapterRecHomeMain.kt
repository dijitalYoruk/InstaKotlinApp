package com.fatihsevban.instakotlinapp.Home.Main

import android.annotation.SuppressLint
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Models.PostWithUser
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.list_item_post.view.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import android.media.MediaMetadataRetriever
import android.os.Build
import android.graphics.Bitmap
import android.os.AsyncTask
import android.text.Html
import com.fatihsevban.instakotlinapp.Home.HomeActivity.ActivityHome
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.Utils.Notifications

class AdapterRecHomeMain (var postsWithUser: ArrayList<PostWithUser>, private val fragmentHomeMain: FragmentHomeMain) : RecyclerView.Adapter<AdapterRecHomeMain.MyViewHolder>() {

    // properties
    val inflater = LayoutInflater.from(fragmentHomeMain.context)
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.list_item_post, parent, false)
        return MyViewHolder(view)
    }

    /**
     * gets the item count of the list.
     */
    override fun getItemCount(): Int {
        return postsWithUser.size
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val post = postsWithUser[position].post!!
        val user = postsWithUser[position].user!!

        setPostImage(user, post, holder)
        setListUserInterface(user, post, holder)
        setCommentAndLikeData(user, post, holder)

        holder.imgLike.setOnClickListener { likePost(post, user, holder) }
        holder.imgComment.setOnClickListener { goToCommentFragment(user, post) }
    }

    /**
     * sets user interface of the list item.
     * @param post is the post whose user interface will be setted.
     * @param user is the user that sended the post.
     * @param holder is the view holder that holds the required items.
     */
    private fun setListUserInterface(user: User, post: Post, holder: AdapterRecHomeMain.MyViewHolder) {

        // username
        holder.tvUserName.setText(user.user_name)

        val sourceString = "<b><font color=black>${user.user_name}:</font></b> ${post.explanation}"
        holder.tvUsernameAndExplanation.text = Html.fromHtml(sourceString)

        // profile image
        UniversalImageLoader.setImage(user.profile_picture!!,
                holder.imgProfile, null,"")

        // date
        val noteDate = post.upload_date!!

        val convertedDate = DateUtils.getRelativeTimeSpanString( noteDate,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, 0)

        holder.tvDate.text = convertedDate
    }

    /**
     * sets comments and likes count and enables to display the in layout.
     * @param post is the post whose like and comment count will be displayed.
     * @param user is the user that sended the post.
     * @param holder is the view holder that holds the required items.
     */
    private fun setCommentAndLikeData(user: User, post: Post, holder: MyViewHolder) {

        // setting likes text view.
        FirebaseDatabase.getInstance().reference
                .child("PostLikers")
                .child(post.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val likeCount = dataSnapshot.childrenCount.toString()
                        holder.tvPostLikeCount.text = "Liked $likeCount times."

                        if (dataSnapshot.hasChild(currentUserId)) {
                            holder.imgLike.setImageResource(R.drawable.icon_like_2)
                        } else {
                            holder.imgLike.setImageResource(R.drawable.icon_like)
                        }
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }
                })


        // setting comments text view
        FirebaseDatabase.getInstance()
                .reference
                .child("Comments")
                .child(post.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val commentCount = dataSnapshot.childrenCount.toString()
                        holder.tvCommentCount.text = "$commentCount Comments"
                    }

                    // error
                    override fun onCancelled(error: DatabaseError) { showErrorToast(error) }

                })
    }

    /**
     * sets post image to the corresponding view in the layout.
     * @param post is the post whose image will be displayed.
     * @param user is the user that sended the post.
     * @param holder is the view holder that holds the required items.
     */
    private fun setPostImage(user: User, post: Post, holder: MyViewHolder) {

        val imageOrVideo = post.photo_uri!!
        val lastPointIndex = imageOrVideo.lastIndexOf(".")
        val docType = imageOrVideo.substring(lastPointIndex, lastPointIndex + 4)

        // video
        if (docType.equals(".mp4")) {
            CreateThumbnail(holder, imageOrVideo).execute(imageOrVideo)
        }

        // photo
        else {
            UniversalImageLoader.setImage(imageOrVideo, holder.imgPost, null, "")
        }
    }

    /**
     * goes to comment fragment and enables to see the comments for the post.
     * @param post is the post whose comments will be displayed.
     * @param user is the user that sended the post.
     */
    private fun goToCommentFragment(user: User, post: Post) {

        (fragmentHomeMain.activity as ActivityHome).homeContainerLayout?.visibility = View.VISIBLE
        (fragmentHomeMain.activity as ActivityHome).viewPagerHome?.visibility = View.GONE

        val transaction = fragmentHomeMain.activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.homeContainerLayout, FragmentHomeComment())
        transaction.addToBackStack("ADD FRAG HOME COMMENT")
        transaction.commit()

        EventBus.getDefault().postSticky( EventBusDataEvent.SendPostIdData(post.post_id!!) )
    }

    /**
     * records the post as liked by the current user in the database.
     * @param post is the post that is liked by the current user.
     * @param user is the user that sended the post.
     * @param holder is the view holder that holds the required items.
     */
    private fun likePost(post: Post, user: User, holder: MyViewHolder) {

        FirebaseDatabase.getInstance().reference
                .child("PostLikers")
                .child(post.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        // user liked the post before.
                        if (dataSnapshot.hasChild(currentUserId)) {

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("PostLikers")
                                    .child(post.post_id!!)
                                    .child(currentUserId)
                                    .removeValue()

                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {

                                            val likeCount = (dataSnapshot.childrenCount - 1).toString()
                                            holder.tvPostLikeCount.text = "Liked $likeCount times."

                                            holder.imgLike.setImageResource(R.drawable.icon_like)
                                            Notifications.removeLikedPostNotification(user.user_id!!, post.post_id!!)
                                        }
                                    }

                        } else { // user likes the post new.

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("PostLikers")
                                    .child(post.post_id!!)
                                    .child(currentUserId)
                                    .setValue(currentUserId)
                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {

                                            val likeCount = (dataSnapshot.childrenCount + 1).toString()
                                            holder.tvPostLikeCount.text = "Liked $likeCount times."

                                            holder.imgLike.setImageResource(R.drawable.icon_like_2)
                                            Notifications.likedPostNotification(user.user_id!!, post.post_id!!)
                                        }
                                    }
                        }

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
        Toast.makeText(fragmentHomeMain.context, error.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {

        var listItem = itemView as ConstraintLayout

        var progressBarHome = listItem.progressBarHome

        var tvPostLikeCount = listItem.tvPostLikeCount
        var tvCommentCount = listItem.tvCommentCount
        var tvUserName = listItem.tvUserName
        var tvDate = listItem.tvDate
        var tvUsernameAndExplanation =
                listItem.tvUsernameAndExplanation

        var imgProfile = listItem.imgProfile
        var imgComment = listItem.imgComment
        var imgLike = listItem.imgLike
        var imgPlay = listItem.imgPlay
        var imgPost = listItem.imgPost
    }

    @SuppressLint("StaticFieldLeak")
    inner class CreateThumbnail(var holder: MyViewHolder, var videoUri: String): AsyncTask<String, Void, Bitmap>() {

        override fun onPreExecute() {
            super.onPreExecute()
            holder.progressBarHome.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: String?): Bitmap? {

            val videoPath = p0[0]
            var bitmap: Bitmap? = null
            var mediaMetadataRetriever: MediaMetadataRetriever? = null
            try {
                mediaMetadataRetriever = MediaMetadataRetriever()
                if (Build.VERSION.SDK_INT >= 14)
                    mediaMetadataRetriever.setDataSource(videoPath, HashMap())
                else
                    mediaMetadataRetriever.setDataSource(videoPath)

                bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
            } catch (e: Exception) {
                e.printStackTrace()
                throw Throwable(
                        "Exception in retriveVideoFrameFromVideo(String videoPath)" + e.message)

            } finally {
                mediaMetadataRetriever?.release()
            }
            return bitmap

        }

        override fun onPostExecute(result: Bitmap?) {
            holder.imgPost.setImageBitmap(result)
            holder.imgPlay.visibility = View.VISIBLE
            holder.progressBarHome.visibility = View.INVISIBLE

            holder.imgPost.setOnClickListener { view ->
                EventBus.getDefault().postSticky(EventBusDataEvent.SendVideoUriData(videoUri))
                FragmentHomeVideo().show(fragmentHomeMain.activity!!.supportFragmentManager, "ADD FRAG VIDEO")
            }
        }
    }

}