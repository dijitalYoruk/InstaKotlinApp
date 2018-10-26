package com.fatihsevban.instakotlinapp.Profile

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings.ActivityProfileSettings
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Search.ActivitySearch
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.FragmentSinglePost
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile_settings.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.grid_profile_cell_layout.view.*
import org.greenrobot.eventbus.EventBus

class AdapterRecProfilePostsGrid(val context: Context, val posts: ArrayList<Post>) : RecyclerView.Adapter<AdapterRecProfilePostsGrid.MyViewHolder>() {

    // properties
    private val inflater = LayoutInflater.from(context)

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.grid_profile_cell_layout, parent, false)
        return MyViewHolder(view)
    }

    /**
     * gets the item count of the list.
     */
    override fun getItemCount(): Int {
        return posts.size
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: AdapterRecProfilePostsGrid.MyViewHolder, position: Int) {

        val imageOrVideo = posts[position].photo_uri!!
        val lastPointIndex = imageOrVideo.lastIndexOf(".")
        val docType = imageOrVideo.substring(lastPointIndex, lastPointIndex+4)

        if (docType.equals(".mp4")) {
            CreateThumbnail(holder).execute(imageOrVideo)
        } else {
            UniversalImageLoader.setImage(imageOrVideo, holder.imgProfileGridCell, holder.gridProgressBar, "")
        }

        holder.gridItem.setOnClickListener { view ->

            if (context is ActivityProfile) {

                context.profileRootLayout.visibility = View.INVISIBLE
                val transaction = context.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.profileContainerLayout, FragmentSinglePost())
                transaction.addToBackStack("ADD FRAG SINGLE POST")
                transaction.commit()

                EventBus.getDefault().postSticky(EventBusDataEvent.SendPostData(posts[position]))
            }

            else if (context is ActivitySearch) {


                context.searchRootLayout.visibility = View.INVISIBLE
                val transaction = context.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.searchContainerLayout, FragmentSinglePost())
                transaction.addToBackStack("ADD FRAG SINGLE POST")
                transaction.commit()

                EventBus.getDefault().postSticky(EventBusDataEvent.SendPostData(posts[position]))

            }

            else if (context is ActivityProfileSettings) {

                context.profileSettingsRootLayout.visibility = View.INVISIBLE
                val transaction = context.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.profileSettingsContainerLayout, FragmentSinglePost())
                transaction.addToBackStack("ADD FRAG SINGLE POST")
                transaction.commit()

                EventBus.getDefault().postSticky(EventBusDataEvent.SendPostData(posts[position]))
            }

        }
    }

    /**
     * Creates thumbnail of a video.
     * @param holder holds the corresponding list items views.
     */
    class CreateThumbnail(var holder: MyViewHolder): AsyncTask<String, Void, Bitmap>() {

        override fun onPreExecute() {
            super.onPreExecute()
            holder.gridProgressBar.visibility = View.VISIBLE
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
            holder.gridProgressBar.visibility = View.INVISIBLE
            holder.imgProfileGridCell.setImageBitmap(result)
            holder.imgPlay.visibility = View.VISIBLE
        }
    }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {
        var gridItem = itemView as ConstraintLayout
        var imgProfileGridCell = gridItem.imgProfileGridCell
        var gridProgressBar = gridItem.gridProgressBar
        var imgPlay = gridItem.imgPlay
    }

}