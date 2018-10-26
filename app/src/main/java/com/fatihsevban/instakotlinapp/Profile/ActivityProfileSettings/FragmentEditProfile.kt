package com.fatihsevban.instakotlinapp.Profile.ActivityProfileSettings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.iceteck.silicompressorr.SiliCompressor
import kotlinx.android.synthetic.main.fragment_profile_edit_profile.*
import kotlinx.android.synthetic.main.fragment_profile_edit_profile.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File

class FragmentEditProfile: Fragment() {

    // constants
    private val GALLERY = 100

    // properties
    lateinit var currentUser: User
    lateinit var initialStateOfCurrentUser: User
    lateinit var mainView: View
    var newProfileImageUri: Uri? = null
    private val fragmentLoading = FragmentLoading()

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflating the required layout
        mainView = inflater.inflate(R.layout.fragment_profile_edit_profile, container, false)

        setOnClickListenersToViews()
        setFragmentUIAccordingToCurrentUser()

        return mainView
    }

    /**
     * sets onClick listeners to required views.
     */
    private fun setOnClickListenersToViews() {
        mainView.imgClose.setOnClickListener { activity?.onBackPressed() }
        mainView.tvChangeProfilePhoto.setOnClickListener { changeProfileImage() }
        mainView.imgRecordUser.setOnClickListener { uploadUser() }
    }

    /**
     * gets the necessary informations through EventBus.
     */
    @Subscribe(sticky = true)
    fun getEmailData(currentUserData: EventBusDataEvent.SendCurrentUserData) {
        currentUser = currentUserData.user
        initialStateOfCurrentUser = User(currentUserData.user)
    }

    /**
     * sets Fragment User Interface
     */
    private fun setFragmentUIAccordingToCurrentUser() {

        mainView.etNameAndSurname.setText(currentUser.name_and_surname)
        mainView.etUsername.setText(currentUser.user_name)
        mainView.etWebsite.setText(currentUser.web_site)
        mainView.etBiography.setText(currentUser.biography)

        if (!currentUser.profile_picture!!.equals("not determined"))
            UniversalImageLoader.setImage(currentUser.profile_picture!!,
                    mainView.imgProfile, progressBar2, "")
    }

    /**
     * changes the profile image view and
     * saves the changed image to database.
     */
    fun changeProfileImage() { goToGallery() }

    /**
     * directs the user to gallery.
     */
    private fun goToGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY)
    }

    /**
     * according to the response, directs the user to gallery or camera.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // gallery
        if ( requestCode == GALLERY && resultCode == AppCompatActivity.RESULT_OK && data != null ) {
            val imageURI = data.data
            mainView.imgProfile.setImageURI(imageURI)
            newProfileImageUri = imageURI
        }

    }

    private fun uploadUser() {

        fragmentLoading.show(activity?.supportFragmentManager, "FRAGMENT LOADING")
        fragmentLoading.isCancelable = false
        val user_name = mainView.etUsername.text.toString()

        // checking whether typed username exists in database.
        FirebaseDatabase.getInstance().reference.
                child("Users").
                addListenerForSingleValueEvent( object: ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        var userNameExistsInDatabase = false

                        for (data in dataSnapshot.children) {

                            val temp = data.getValue(User::class.java)

                            // checking whether typed username exists in database.
                            if (temp?.user_id != currentUser.user_id && temp?.user_name.toString().equals(user_name)) {
                                userNameExistsInDatabase = true; break
                            }
                        }

                        // doesn't exist
                        if (!userNameExistsInDatabase) {
                            uploadUserProfileImageToFirebaseStorage()
                        }

                        else { // exists
                            Toast.makeText(context, "User name already exists in database.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    /**
     * uploads profile image to storage.
     */
    private fun uploadUserProfileImageToFirebaseStorage() {

        if (newProfileImageUri != null) {

            val ref = FirebaseStorage.getInstance().reference
                    .child("Users")
                    .child(currentUser.user_id!!)
                    .child("profile_image")

            ref.putFile(newProfileImageUri!!)

                    .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful)
                            throw task.exception!!

                        return@Continuation ref.downloadUrl
                    })

                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            currentUser.profile_picture = task.result.toString()
                            uploadUserToDatabase()
                        } else {
                            Toast.makeText(context, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
        } else
            uploadUserToDatabase()
    }

    /**
     * Compresses An Image File and
     * Saves it to a certain directory.
     */
    @SuppressLint("StaticFieldLeak")
    inner class ImageCompressorClass: AsyncTask<String, String, String>() {

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
                newProfileImageUri = Uri.parse("file://$result")
                uploadUserProfileImageToFirebaseStorage()
            }
        }
    }

    /**
     * uploads user to database.
     */
    private fun uploadUserToDatabase() {

        updateCurrentUser()

        if (currentUser.toString().equals(initialStateOfCurrentUser.toString())) {
            Toast.makeText(context, "Please Type New Values", Toast.LENGTH_SHORT).show()
            fragmentLoading.dismiss()
        }

        else {

            FirebaseDatabase.getInstance().reference
                    .child("Users")
                    .child(currentUser.user_id!!)
                    .setValue(currentUser)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                            activity?.onBackPressed()
                        }

                        else {
                            Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                        }

                        fragmentLoading.dismiss()
                    }
        }
    }

    /**
     * updates current user with new typed values.
     */
    private fun updateCurrentUser() {
        val biography = mainView.etBiography.text.toString()
        val name_and_surname = mainView.etNameAndSurname.text.toString()
        val user_name = mainView.etUsername.text.toString()
        val website = mainView.etWebsite.text.toString()

        currentUser.user_name = user_name
        currentUser.name_and_surname = name_and_surname
        currentUser.biography = biography
        currentUser.web_site = website
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