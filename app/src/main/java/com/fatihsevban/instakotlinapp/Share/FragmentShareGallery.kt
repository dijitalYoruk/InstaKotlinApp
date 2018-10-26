package com.fatihsevban.instakotlinapp.Share

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings.FragmentLikedPosts
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.FileOperations
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.android.synthetic.main.fragment_share_gallery.*
import kotlinx.android.synthetic.main.fragment_share_gallery.view.*
import kotlinx.android.synthetic.main.grid_share_cell_layout.*
import org.greenrobot.eventbus.EventBus

class FragmentShareGallery: Fragment() {

    // properties
    lateinit var mainView: View
    lateinit var selectedImageOrVideoUri: String
    lateinit var documentType: String
    lateinit var rcvShareAdapter: AdapterRecShare
    private val folderPaths = ArrayList<String>()
    private val folderNames =ArrayList<String>()

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_share_gallery, container, false)

        getFolderPathsAndNames()
        setOnClickListenersToViews()
        setRecyclerView()
        setSpinnerItemSelectedListener()
        setFoldersSpinner()

        return mainView
    }

    /**
     * sets onClick listeners to close and proceed views.
     */
    private fun setOnClickListenersToViews() {

        // enables the close image to go back.
        mainView.imgClose.setOnClickListener { view ->
            activity?.onBackPressed()
        }

        // enables the proceed text view to share images and videos.
        mainView.tvProceed.setOnClickListener { view ->

            activity?.shareRootLayout?.visibility = View.GONE
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.shareContainerLayout, FragmentShareProceed())
            transaction.addToBackStack("ADD FRAG SHARE PROCEED")
            transaction.commit()

            if (documentType.equals("image")) {
                val cropped = image_layout.croppedImage
                selectedImageOrVideoUri = FileOperations.saveCroppedBitmapToExternalStorage(cropped)!!
            }

            video_view.stopPlayback()

            // sending the data to share fragment.
            EventBus.getDefault().postSticky(EventBusDataEvent.SendImageUriData(
                    selectedImageOrVideoUri, documentType))
        }
    }

    /**
     * sets the recyclerViews functionality, adapter and  .
     */
    private fun setRecyclerView() {

        // setting functionality to increase performance
        mainView.rcvShareGrid.setHasFixedSize(true)
        mainView.rcvShareGrid.setItemViewCacheSize(10)
        mainView.rcvShareGrid.setDrawingCacheEnabled(true)
        mainView.rcvShareGrid.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW)

        // setting adapter.
        rcvShareAdapter = AdapterRecShare(ArrayList(), this)
        mainView.rcvShareGrid.adapter = rcvShareAdapter

        // setting layout manager.
        val layoutManager = GridLayoutManager(activity,3)
        mainView.rcvShareGrid.layoutManager = layoutManager
    }

    /**
     * set the folders spinner on the layout.
     */
    private fun setFoldersSpinner() {
        val spinnerLayout = android.R.layout.simple_spinner_dropdown_item
        val spinnerFoldersAdapter = ArrayAdapter<String>(context!!, spinnerLayout, folderNames)
        spinnerFoldersAdapter.setDropDownViewResource(spinnerLayout)
        mainView.spnFolders.adapter = spinnerFoldersAdapter
    }

    /**
     * set the spinners item selected listener.
     * the spinner arranges the corresponding
     * category and updates the rcvShareAdapter.
     */
    private fun setSpinnerItemSelectedListener() {

        mainView.spnFolders.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // nothing done
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                rcvShareAdapter.imageOrVideoURIs = FileOperations.getDocsInFolder(folderPaths[position])
                rcvShareAdapter.notifyDataSetChanged()

                if (rcvShareAdapter.imageOrVideoURIs.isNotEmpty())
                    displaySelectedImageOrVideo(0)
            }
        }
    }

    /**
     * displays selected Image or video.
     * @param position is the position value of the selected item.
     */
    fun displaySelectedImageOrVideo(position: Int) {

        val imagesAndVideosPath = rcvShareAdapter.imageOrVideoURIs

        // getting document type extension.
        val lastPointIndex = imagesAndVideosPath[position].lastIndexOf(".")
        documentType = imagesAndVideosPath[position].substring(lastPointIndex)

        // displaying image or video.
        if( documentType.equals(".jpg") || documentType.equals(".jpeg") || documentType.equals(".png") )
            displayImage(imagesAndVideosPath[position])

        else if (documentType.equals(".mp4"))
            displayVideo(imagesAndVideosPath[position])

        selectedImageOrVideoUri = imagesAndVideosPath[position]
    }

    /**
     * displays the video on the videoView.
     */
    private fun displayVideo(videoUri: String) {
        mainView.video_view.visibility = View.VISIBLE
        mainView.image_layout.visibility = View.GONE
        mainView.video_view.setVideoURI(Uri.parse("file://$videoUri"))
        mainView.video_view.start()
        documentType = "video"
    }

    /**
     * displays the image on the imageView.
     */
    private fun displayImage(imageUri: String) {
        UniversalImageLoader.setImage(imageUri, mainView.image_layout, null, "file://")
        mainView.video_view.visibility = View.GONE
        mainView.image_layout.visibility = View.VISIBLE
        documentType = "image"
    }

    /**
     * gets the required folder paths and names.
     */
    private fun getFolderPathsAndNames() {

        val root = Environment.getExternalStorageDirectory().path
        val cameraPhotosPath = "$root/DCIM/Camera"
        val downloadedPhotosPath = "$root/Download"
        val whatsappPhotosPath = "$root/WhatsApp/Media/WhatsApp Images"
        val screenShotPhotosPath = "$root/PICTURES/Screenshots"

        if (FileOperations.doesFolderExist(cameraPhotosPath)) {
            folderPaths.add(cameraPhotosPath)
            folderNames.add("Camera")
        }

        if (FileOperations.doesFolderExist(downloadedPhotosPath)) {
            folderPaths.add(downloadedPhotosPath)
            folderNames.add("Downloads")
        }

        if (FileOperations.doesFolderExist(whatsappPhotosPath)) {
            folderPaths.add(whatsappPhotosPath)
            folderNames.add("Whats App")
        }

        if (FileOperations.doesFolderExist(screenShotPhotosPath)) {
            folderPaths.add(screenShotPhotosPath)
            folderNames.add("Screenshots")
        }
    }

}