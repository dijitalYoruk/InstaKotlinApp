package com.fatihsevban.instakotlinapp.Share

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatihsevban.instakotlinapp.R

class FragmentProgress: DialogFragment() {

    // properties
    lateinit var mainView: View

    /**
     * creates the required fragment.
     * @param inflater is the layout inflater that inflates the corresponding layout.
     * @param container is the parent layout of the fragment that will be added.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_progress, container, false)
        return mainView
    }

}