package com.fatihsevban.instakotlinapp.Home.HomeActivity

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_home.*

class ActivityHome : AppCompatActivity() {

    // constants
    val ACTIVITY_NO = 0
    val ACTIVITY_TAG = "ACTIVITY HOME"

    // properties
    private var permissionRequest = false
    lateinit var mAuth: FirebaseAuth.AuthStateListener

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupAuthStateListener()
        getRequiredPermissions()
    }

    /**
     * sets up auth state listener for this activity.
     */
    private fun setupAuthStateListener() {

        mAuth = FirebaseAuth.AuthStateListener { firebaseAuth ->

            if (FirebaseAuth.getInstance().currentUser == null) {

                // sending user to ActivityLogin
                val intent = Intent(this@ActivityHome, ActivityLogin::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)

                // sending user to App Details for permissions.
                if(permissionRequest) {
                    val intentAppDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intentAppDetails.data = uri
                    startActivity(intentAppDetails)
                }
            }
        }
    }

    /**
     * initialises Universal Image Loader.
     */
    private fun initUniversalImageLoader() {
        val universalImageLoader = UniversalImageLoader(this)
        ImageLoader.getInstance().init(universalImageLoader.config)
    }

    /**
     * setup's ViewPager and arranges screen compatibility.
     */
    private fun setupFragmentViewPager() {

        // initialising
        val pagerAdapter = PagerAdapterHome(supportFragmentManager)
        viewPagerHome.adapter = pagerAdapter
        viewPagerHome.currentItem = pagerAdapter.currentItemPosition

        // adding listener for screen compatibility.
        viewPagerHome.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {

                when(position) {

                    0 -> {
                        this@ActivityHome.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        this@ActivityHome.window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    }

                    1 -> {
                        this@ActivityHome.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        this@ActivityHome.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    }

                    2 -> {
                        this@ActivityHome.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        this@ActivityHome.window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                    }
                }
            }
        })
    }

    /**
     * sets the visibility of view pager.
     * disables the back animation.
     */
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
        viewPagerHome.visibility = View.VISIBLE
    }

    /**
     * Adds the Auth state listener.
     */
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuth)
    }

    /**
     * Removes the Auth state listener.
     */
    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuth)
    }

    /**
     * gets the required permissions for the ActivityShare
     * Dexter: A library that makes easy to request permissions
     */
    private fun getRequiredPermissions() {

        Dexter.withActivity(this)

                // Permissions that will be requested
                .withPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO)

                .withListener(object: MultiplePermissionsListener {

                    // is triggered every time when permissions are checked.
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                        // permissions given
                        if (report!!.areAllPermissionsGranted()) {
                            initUniversalImageLoader()
                            setupFragmentViewPager()
                        }

                        // permission denied permanently. Trying to convincing user.
                        else if (report.isAnyPermissionPermanentlyDenied) {

                            AlertDialog.Builder(this@ActivityHome)
                                    .setTitle("Permissions Are Essential!")

                                    .setMessage("In order to use the application, Permissions " +
                                            "are essential. Could you please give permissions?")

                                    .setPositiveButton("Yes") { dialog, which ->
                                        dialog.cancel()
                                        permissionRequest = true
                                        FirebaseAuth.getInstance().signOut()
                                    }

                                    .setNegativeButton("Cancel") { dialog, which ->
                                        dialog.cancel()
                                        FirebaseAuth.getInstance().signOut()
                                    }
                                    .show()
                        }

                        else {
                            FirebaseAuth.getInstance().signOut()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {

                        // one of the permissions is denied. Trying to convincing user.

                        AlertDialog.Builder(this@ActivityHome)
                                .setTitle("Permissions Are Essential!")

                                .setMessage("In order to use the application, Permissions " +
                                        "are essential. Could you please give permissions?")

                                .setPositiveButton("Yes") { dialog, which ->
                                    dialog.cancel()
                                    token?.continuePermissionRequest()
                                }

                                .setNegativeButton("Cancel") { dialog, which ->
                                    dialog.cancel()
                                    token?.cancelPermissionRequest()
                                    FirebaseAuth.getInstance().signOut()
                                }
                                .show()
                    }

                }).check()
    }

}