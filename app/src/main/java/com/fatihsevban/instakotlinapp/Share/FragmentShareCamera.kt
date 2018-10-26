package com.fatihsevban.instakotlinapp.Share

import android.content.Context
import android.graphics.Camera
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Gesture
import com.otaliastudios.cameraview.GestureAction
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.android.synthetic.main.fragment_share_camera.view.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream

class FragmentShareCamera: Fragment() {

    // properties
    var mainView: View? = null

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_share_camera, container, false)

        setupCameraView()
        setOnClickListenersToViews()
        return mainView
    }

    /**
     * adds camera listener to cameraView and functionality.
     */
    private fun setupCameraView() {

        // adding listener.
        mainView!!.cameraView.addCameraListener(object: CameraListener(){

            override fun onPictureTaken(jpeg: ByteArray?) {

                // creating file for the taken picture.
                val photoName = "${System.currentTimeMillis()}.jpg"
                val root = Environment.getExternalStorageDirectory().path
                val takenPicturePath = "$root/DCIM/Camera/$photoName"
                val takenPictureFile = File(takenPicturePath)

                // writing photo to the file
                val recordFile = FileOutputStream(takenPictureFile)
                recordFile.write(jpeg)
                recordFile.close()

                // directing user to proceed fragment.
                activity?.shareRootLayout?.visibility = View.GONE
                val transaction = activity!!.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.shareContainerLayout, FragmentShareProceed())
                transaction.addToBackStack("ADD FRAG SHARE PROCEED")
                transaction.commit()

                // BUNUN PATH LERİNE BAKILACAK
                EventBus.getDefault().postSticky(EventBusDataEvent.SendImageUriData(
                        takenPictureFile.absolutePath, "image"))
            }
        })

        // adding functionality to camera view.
        mainView!!.cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM) // Pinch to zoom!
        mainView!!.cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER) // Tap to focus!
    }

    /**
     * sets onClick listeners to close and share camera views.
     */
    private fun setOnClickListenersToViews() {

        mainView!!.imgClose.setOnClickListener { view ->
            activity?.onBackPressed()
        }

        mainView!!.imgShareCamera.setOnClickListener { view ->
            mainView!!.cameraView.capturePicture()
        }
    }

    /**
     * starts the cameraView
     */
    fun startCameraView() {
        if (mainView != null) {
            mainView?.cameraView?.start()
        }
    }

    /**
     * stops the cameraView
     */
    fun stopCameraView() {
        if (mainView != null) {
            mainView?.cameraView?.stop()
        }
    }

    /**
     * stops the cameraView while onPause.
     */
    override fun onPause() {
        super.onPause()
        stopCameraView()
    }

    /**
     * destroys the cameraView while onDestroy.
     */
    override fun onDestroy() {
        super.onDestroy()
        mainView?.cameraView?.destroy()
    }

}