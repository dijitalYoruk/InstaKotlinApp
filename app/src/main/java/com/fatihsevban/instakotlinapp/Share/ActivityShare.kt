package com.fatihsevban.instakotlinapp.Share

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.fatihsevban.instakotlinapp.Login.ActivityLogin
import com.fatihsevban.instakotlinapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_share.*

class ActivityShare : AppCompatActivity() {

    // constants
    private val ACTIVITY_NO = 2
    private val ACTIVITY_TAG = "ACTIVITY SHARE"

    // properties
    lateinit var mAuth: FirebaseAuth.AuthStateListener
    lateinit var pagerAdapter: PagerAdapterShare

    /**
     * creates the initial view of the activity.
     * @param savedInstanceState is the bundle
     * that contains additional information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        setupFragmentViewPager()
        setupAuthStateListener()
    }

    /**
     * sets up Fragment View Pager
     * and the corresponding tab layout.
     */
    private fun setupFragmentViewPager() {

        // setting the pagerAdapter to the view pager.
        pagerAdapter = PagerAdapterShare(supportFragmentManager)
        viewPgrShare.adapter = pagerAdapter

        // stopping camera views
        pagerAdapter.videoFragment.stopVideoView()
        pagerAdapter.cameraFragment.stopCameraView()

        // setting page change listener.
        viewPgrShare.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            // sets the cameras in each fragment.
            override fun onPageSelected(position: Int) {

                when (position) {

                    0 -> {
                        pagerAdapter.cameraFragment.stopCameraView()
                        pagerAdapter.videoFragment.stopVideoView()
                    }

                    1 -> {
                        pagerAdapter.videoFragment.stopVideoView()
                        pagerAdapter.cameraFragment.startCameraView()
                    }

                    2 -> {
                        pagerAdapter.cameraFragment.stopCameraView()
                        pagerAdapter.videoFragment.startVideoView()
                    }
                }
            }
        })

        // setting tab layout.
        setupTabLayout()
    }

    /**
     * sets up the tabLayout for the view pager.
     */
    private fun setupTabLayout() {
        tabLayoutShare.setupWithViewPager(viewPgrShare)
        tabLayoutShare.getTabAt(0)?.text = pagerAdapter.getPageTitle(0)
        tabLayoutShare.getTabAt(1)?.text = pagerAdapter.getPageTitle(1)
        tabLayoutShare.getTabAt(2)?.text = pagerAdapter.getPageTitle(2)
    }


    /**
     * sets up auth state listener for this activity.
     */
    private fun setupAuthStateListener() {
        mAuth = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (FirebaseAuth.getInstance().currentUser == null) {
                val intent = Intent(this@ActivityShare, ActivityLogin::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }

    /**
     * registers the auth state listener to the activity.
     */
    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuth)
    }

    /**
     * unregisters the auth state listener from the activity.
     */
    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuth)
    }

    /**
     * sets the visibility of the root layout.
     */
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,0)
        shareRootLayout.visibility = View.VISIBLE
    }
}