package com.fatihsevban.instakotlinapp.Share

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Home.HomeActivity.ActivityHome
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.iceteck.silicompressorr.SiliCompressor
import kotlinx.android.synthetic.main.fragment_progress.view.*
import kotlinx.android.synthetic.main.fragment_share_proceed.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File


class FragmentShareProceed: Fragment() {

    // properties
    lateinit var imageOrVideoURI: String
    lateinit var mainView: View
    lateinit var docType: String
    lateinit var fragmentProgress: FragmentProgress

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_share_proceed, container, false)

        // setting image or video to be shared.
        UniversalImageLoader.setImage(imageOrVideoURI, mainView.imgToBeShared, mainView.progressBar6, "file:/")

        setOnClickListenersToViews()
        return mainView
    }

    /**
     * sets onClick listeners to get back and share views.
     */
    private fun setOnClickListenersToViews() {

        // getting back.
        mainView.imgGetBack.setOnClickListener { view ->
            activity?.onBackPressed()
        }

        // sharing image or video.
        mainView.tvShare.setOnClickListener { view ->

            if (docType.equals("image"))
                ImageCompressorClass().execute(imageOrVideoURI)

            else if (docType.equals("video"))
                VideoCompressorClass().execute(imageOrVideoURI)
        }
    }

    /**
     * uploads image or video to storage
     */
    @SuppressLint("SetTextI18n")
    private fun uploadImageOrVideoToStorage() {

        // constructing and etting the required values.
        val imageUri = Uri.parse("file://$imageOrVideoURI")
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        // constructing a storage reference.
        val ref = FirebaseStorage.getInstance().reference
                .child("Users")
                .child(userId)
                .child("Posts")
                .child(imageUri.lastPathSegment!!)

        ref.putFile(imageUri)

                // indicates the uploading progress.
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    fragmentProgress.mainView.tvProgress.text = "Loading $progress"
                }

                // getting the downloading uri.
                .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful)
                        throw task.exception!!

                    return@Continuation ref.downloadUrl
                })

                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        recordPostToDatabase(task.result) // task.result : download Uri.
                    else
                        Toast.makeText(context, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
    }

    /**
     * records post to database.
     * @param downloadUri is the download
     * uri of the recorded image or video.
     */
    private fun recordPostToDatabase(downloadUri: Uri?) {

        // creating post
        val post = createCorrespondingPost(downloadUri)

        // recording post to database.
        FirebaseDatabase.getInstance().reference
                .child("Posts")
                .child(post.user_id!!)
                .child(post.post_id!!)
                .setValue(post)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Toast.makeText(context, "Posted", Toast.LENGTH_SHORT).show()
                        fragmentProgress.dismiss()

                        val intent = Intent(context, ActivityHome::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)

                    } else {
                        Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                        fragmentProgress.dismiss()
                    }
                }
    }

    /**
     * creates the required post depending on the passed uri.
     * @param downloadUri is the passed download uri.
     */
    private fun createCorrespondingPost(downloadUri: Uri?): Post {

        val user_id = FirebaseAuth.getInstance().currentUser!!.uid
        val explanation = mainView.etExplanation.text.toString()
        val photo_uri = downloadUri.toString()

        val postId = FirebaseDatabase.getInstance().reference
                .child("Posts").child(user_id).push().key

        return Post(user_id, postId, System.currentTimeMillis(), explanation, photo_uri)
    }


    /**
     * gets image or video uri and the specific doc type.
     */
    @Subscribe(sticky = true)
    fun getImageOrVideoURI(imageUriData: EventBusDataEvent.SendImageUriData) {
        imageOrVideoURI= imageUriData.uri
        docType = imageUriData.docType
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

    /**
     * Compresses A Video File and
     * Saves it to a certain directory.
     */
    @SuppressLint("StaticFieldLeak")
    inner class VideoCompressorClass: AsyncTask<String, String, String>(){

        override fun onPreExecute() {
            // showing progress fragment
            fragmentProgress = FragmentProgress()
            fragmentProgress.show(activity?.supportFragmentManager, "FRAGMENT PROGRESS")
            fragmentProgress.isCancelable = false
        }

        override fun doInBackground(vararg videos: String?): String? {

            // creating a destination folder for the compressed file.
            val root = Environment.getExternalStorageDirectory().absolutePath
            val destinationFile = File("$root/Compressed/Video")

            // creating directory and compressing image.
            if(destinationFile.isDirectory || destinationFile.mkdirs()) {
                val compressedVideoUri = SiliCompressor.with(context).compressVideo(videos[0], destinationFile.path)
                return compressedVideoUri
            }

            return null
        }

        override fun onPostExecute(result: String?) {
            // uploading image to storage.
            if (!result.isNullOrEmpty()) {
                imageOrVideoURI = result!!
                uploadImageOrVideoToStorage()
            }
        }
    }

    /**
     * Compresses An Image File and
     * Saves it to a certain directory.
     */
    @SuppressLint("StaticFieldLeak")
    inner class ImageCompressorClass: AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            // showing progress fragment
            fragmentProgress = FragmentProgress()
            fragmentProgress.show(activity?.supportFragmentManager, "FRAGMENT PROGRESS")
            fragmentProgress.isCancelable = false
        }

        override fun doInBackground(vararg images: String?): String? {

            // creating a destination folder for the compressed file.
            val root = Environment.getExternalStorageDirectory().absolutePath
            val destinationFile = File(root + "/Compressed/Image")

            // creating directory and compressing video.
            if (destinationFile.isDirectory || destinationFile.mkdirs()) {
                val compressedImageUri = SiliCompressor.with(context).compress(images[0], destinationFile)
                return compressedImageUri
            }

            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            // uploading video to storage.
            if (!result.isNullOrEmpty()) {
                imageOrVideoURI = result!!
                uploadImageOrVideoToStorage()
            }
        }
    }
}