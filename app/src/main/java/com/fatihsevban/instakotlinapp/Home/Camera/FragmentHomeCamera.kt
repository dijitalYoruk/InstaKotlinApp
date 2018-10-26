package com.fatihsevban.instakotlinapp.Home.Camera

import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Models.Post
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.Facing
import com.otaliastudios.cameraview.Gesture
import com.otaliastudios.cameraview.GestureAction
import kotlinx.android.synthetic.main.fragment_home_camera.view.*
import java.io.File
import java.io.FileOutputStream

class FragmentHomeCamera : Fragment() {

    // properties
    lateinit var mainView: View

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_home_camera, container, false)

        // setting onClick Listeners.
        mainView.imgSwapCamera.setOnClickListener { swapCamera() }
        mainView.imgCapturePhoto.setOnClickListener { capturePhoto() }

        setupCameraListener()

        // adding functionality to camera view.
        mainView.cameraViewHome.mapGesture(Gesture.PINCH, GestureAction.ZOOM) // Pinch to zoom!
        mainView.cameraViewHome.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER) // Tap to focus!


        return mainView
    }

    /**
     * adds Camera Listener to Camera View.
     */
    private fun setupCameraListener() {

        mainView.cameraViewHome.addCameraListener(object : CameraListener() {

            override fun onPictureTaken(jpeg: ByteArray?) {
                super.onPictureTaken(jpeg)

                // creating file for the taken picture.
                val photoName = "${System.currentTimeMillis()}.jpg"
                val root = Environment.getExternalStorageDirectory().path
                val takenPicturePath = "$root/DCIM/Camera/$photoName"
                val takenPictureFile = File(takenPicturePath)

                // writing photo to the file
                val recordFile = FileOutputStream(takenPictureFile)
                recordFile.write(jpeg)
                recordFile.close()

                Toast.makeText(context, "Picture Taken", Toast.LENGTH_SHORT).show()
            }
        })

    }

    /**
     * captures photo.
     */
    private fun capturePhoto() {

        if (mainView.cameraViewHome.facing == Facing.BACK)
            mainView.cameraViewHome.capturePicture()

        else if (mainView.cameraViewHome.facing == Facing.FRONT)
            mainView.cameraViewHome.captureSnapshot()
    }

    /**
     * swaps the back and front camera.
     */
    private fun swapCamera() {

        if (mainView.cameraViewHome.facing == Facing.BACK)
            mainView.cameraViewHome.facing = Facing.FRONT

        else if(mainView.cameraViewHome.facing == Facing.FRONT)
            mainView.cameraViewHome.facing = Facing.BACK
    }


    /**
     * starts the cameraView
     */
    override fun onResume() {
        super.onResume()
        mainView.cameraViewHome.start()
    }

    /**
     * stops the cameraView while onPause.
     */
    override fun onPause() {
        super.onPause()
        mainView.cameraViewHome.stop()
    }

    /**
     * destroys the cameraView while onDestroy.
     */
    override fun onDestroy() {
        super.onDestroy()
        mainView.cameraViewHome.destroy()
    }

}