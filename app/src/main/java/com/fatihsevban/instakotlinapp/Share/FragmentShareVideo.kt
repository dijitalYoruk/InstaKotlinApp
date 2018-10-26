package com.fatihsevban.instakotlinapp.Share

import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.otaliastudios.cameraview.CameraListener
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.android.synthetic.main.fragment_share_video.view.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class FragmentShareVideo: Fragment() {

    // properties
    var mainView: View? = null

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_share_video, container, false)

        setOnClickListenersToViews()
        setupVideoView()

        return mainView
    }

    /**
     * adds camera listener to videoView
     */
    fun setupVideoView() {

        mainView!!.videoView.addCameraListener(object: CameraListener(){

            override fun onVideoTaken(video: File?) {

                // directing user to proceed fragment.
                activity?.shareRootLayout?.visibility = View.GONE
                val transaction = activity!!.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.shareContainerLayout, FragmentShareProceed())
                transaction.addToBackStack("ADD FRAG SHARE PROCEED")
                transaction.commit()

                // sending corresponding sata to proceed fragment
                EventBus.getDefault().postSticky(EventBusDataEvent.SendImageUriData(
                        video?.path, "video"))
            }
        })
    }

    /**
     * sets onClick and onTouch listeners to close and share video views.
     */
    private fun setOnClickListenersToViews() {

        // onClick
        mainView!!.imgClose.setOnClickListener { view ->
            activity?.onBackPressed()
        }

        // onTouch
        mainView!!.imgShareVideo.setOnTouchListener{ view, motionEvent ->

            // DOWN
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                // creating folder in which the video will be recorded.
                val videoName = "${System.currentTimeMillis()}.mp4"
                val root = Environment.getExternalStorageDirectory().path
                val recordedVideoPath = "$root/DCIM/Camera/$videoName"
                val recordedVideoFile = File(recordedVideoPath)

                // starting to record video
                Toast.makeText(context, "Started", Toast.LENGTH_SHORT).show()
                mainView!!.videoView.startCapturingVideo(recordedVideoFile)
                return@setOnTouchListener true
            }

            // UP
            else if (motionEvent.action == MotionEvent.ACTION_UP) {
                // finishing to record video
                Toast.makeText(context, "Finished", Toast.LENGTH_SHORT).show()
                mainView!!.videoView.stop()
                return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }
    }

    /**
     * starts the videoView
     */
    fun startVideoView() {
        if (mainView != null) {
            mainView!!.videoView.start()
        }
    }

    /**
     * stops the videoView
     */
    fun stopVideoView() {
        if (mainView != null) {
            mainView?.videoView?.stop()
        }
    }

    /**
     * stops the videoView while onPause.
     */
    override fun onPause() {
        super.onPause()
        stopVideoView()
    }

    /**
     * destroys the videoView while onDestroy.
     */
    override fun onDestroy() {
        super.onDestroy()
        mainView?.videoView?.destroy()
    }

}