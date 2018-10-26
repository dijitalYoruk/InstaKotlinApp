package com.fatihsevban.instakotlinapp.Profile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Home.Main.FragmentHomeComment
import com.fatihsevban.instakotlinapp.Home.Main.FragmentHomeVideo
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Search.ActivitySearch
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.Notifications
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.list_item_post.view.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList

class AdapterRecProfilePostsList (val context: Context, val userPosts: ArrayList<Post>, val currentUser: User) : RecyclerView.Adapter<AdapterRecProfilePostsList.MyViewHolder>() {

    // properties
    private val inflater = LayoutInflater.from(context)

    // constructor
    init {
        // sorts list according to time.
        userPosts.sortWith(Comparator { file1, file2 ->

            if(file1.upload_date!! > file2.upload_date!!){
                -1
            }else {
                1
            }
        })
    }

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRecProfilePostsList.MyViewHolder {
        val view = inflater.inflate(R.layout.list_item_post, parent, false)
        return MyViewHolder(view)
    }

    /**
     * gets the item count of the list.
     */
    override fun getItemCount(): Int {
        return userPosts.size
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: AdapterRecProfilePostsList.MyViewHolder, position: Int) {

        val post = userPosts[position]

        setPostImage(post, holder)
        setListUserInterface(post, holder)
        setCommentAndLikeData(post, holder)

        holder.imgLike.setOnClickListener { likePost(post, holder) }
        holder.imgComment.setOnClickListener { goToCommentFragment(post) }
    }

    /**
     * sets post image to the corresponding view in the layout.
     * @param post is the post whose image will be displayed.
     * @param user is the user that sended the post.
     * @param holder is the view holder that holds the required items.
     */
    private fun setPostImage(post: Post, holder: MyViewHolder) {

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
     * sets user interface of the list item.
     * @param post is the post whose user interface will be setted.
     * @param user is the user that sended the post.
     * @param holder is the view holder that holds the required items.
     */
    private fun setListUserInterface(post: Post, holder: MyViewHolder) {

        // username
        holder.tvUserName.setText(currentUser.user_name)

        val sourceString = "<b><font color=black>${currentUser.user_name}:</font></b> ${post.explanation}"
        holder.tvUsernameAndExplanation.text = Html.fromHtml(sourceString)

        // profile image
        UniversalImageLoader.setImage(currentUser.profile_picture!!,
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
    private fun setCommentAndLikeData(post: Post, holder: MyViewHolder) {

        // setting likes text view.
        FirebaseDatabase.getInstance().reference
                .child("PostLikers")
                .child(post.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val likeCount = dataSnapshot.childrenCount.toString()
                        holder.tvPostLikeCount.text = "Liked $likeCount times."

                        if (dataSnapshot.hasChild(currentUser.user_id!!)) {
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
     * records the post as liked by the current user in the database.
     * @param post is the post that is liked by the current user.
     * @param user is the user that sended the post.
     * @param holder is the view holder that holds the required items.
     */
    private fun likePost(post: Post, holder: MyViewHolder) {

        FirebaseDatabase.getInstance().reference
                .child("PostLikers")
                .child(post.post_id!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        // user liked the post before.
                        if (dataSnapshot.hasChild(currentUser.user_id!!)) {

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("PostLikers")
                                    .child(post.post_id!!)
                                    .child(currentUser.user_id!!)
                                    .removeValue()

                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {

                                            val likeCount = (dataSnapshot.childrenCount - 1).toString()
                                            holder.tvPostLikeCount.text = "Liked $likeCount times."

                                            holder.imgLike.setImageResource(R.drawable.icon_like)
                                            Notifications.removeLikedPostNotification(currentUser.user_id!!, post.post_id!!)
                                        }
                                    }

                        } else { // user likes the post new.

                            FirebaseDatabase.getInstance()
                                    .reference
                                    .child("PostLikers")
                                    .child(post.post_id!!)
                                    .child(currentUser.user_id!!)
                                    .setValue(currentUser.user_id!!)
                                    .addOnCompleteListener { task ->

                                        if (task.isSuccessful) {

                                            val likeCount = (dataSnapshot.childrenCount + 1).toString()
                                            holder.tvPostLikeCount.text = "Liked $likeCount times."

                                            holder.imgLike.setImageResource(R.drawable.icon_like_2)
                                            Notifications.likedPostNotification(currentUser.user_id!!, post.post_id!!)
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
     * @param post is the post whose comments will be displayed.
     * @param user is the user that sended the post.
     */
    private fun goToCommentFragment(post: Post) {

        if (context is ActivityProfile) {

            context.profileContainerLayout.visibility = View.VISIBLE
            context.profileRootLayout.visibility = View.INVISIBLE

            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.profileContainerLayout, FragmentHomeComment())
            transaction.addToBackStack("ADD FRAG HOME COMMENT")
            transaction.commit()

            EventBus.getDefault().postSticky( EventBusDataEvent.SendPostIdData(post.post_id!!) )
        }

        else if (context is ActivitySearch) {

            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.searchContainerLayout, FragmentHomeComment())
            transaction.addToBackStack("ADD FRAG HOME COMMENT")
            transaction.commit()

            EventBus.getDefault().postSticky(EventBusDataEvent.SendPostIdData(post.post_id))

        }
    }

    /**
     * shows Error Message through Toast.
     * @param error is the error to be displayed.
     */
    fun showErrorToast(error: DatabaseError) {
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {
        var listItem = itemView as ConstraintLayout
        var tvUsernameAndExplanation = listItem.tvUsernameAndExplanation
        var tvUserName = listItem.tvUserName
        var tvDate = listItem.tvDate
        var tvPostLikeCount = listItem.tvPostLikeCount
        var imgComment = listItem.imgComment
        var imgLike = listItem.imgLike
        var imgProfile = listItem.imgProfile
        var imgPlay = listItem.imgPlay
        var imgPost = listItem.imgPost
        var tvCommentCount = listItem.tvCommentCount
        var progressBarHome = listItem.progressBarHome
    }

    /**
     * Creates thumbnail of a video.
     * @param holder holds the corresponding list items views.
     */
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
                FragmentHomeVideo().show((context as ActivityProfile).supportFragmentManager, "ADD FRAG VIDEO")
            }
        }
    }


}