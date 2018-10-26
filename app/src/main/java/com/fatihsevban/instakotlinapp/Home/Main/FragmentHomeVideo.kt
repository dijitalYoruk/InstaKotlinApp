package com.fatihsevban.instakotlinapp.Home.Main

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import kotlinx.android.synthetic.main.fragment_home_video.view.*
import kotlinx.android.synthetic.main.fragment_share_video.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class FragmentHomeVideo: DialogFragment() {

    // properties
    lateinit var mainView: View
    lateinit var videoUri: String

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_home_video, container, false)
        mainView.video_view.setZOrderOnTop(true);
        mainView.video_view.setVideoURI(Uri.parse(videoUri))
        mainView.video_view.start()
        return mainView
    }

    /**
     * gets image or video uri and the specific doc type.
     */
    @Subscribe(sticky = true)
    fun getVideoURI(videoUriData: EventBusDataEvent.SendVideoUriData) {
        videoUri = videoUriData.videoUri
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
        mainView.video_view.stopPlayback()
    }

}