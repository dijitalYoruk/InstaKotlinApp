package com.fatihsevban.instakotlinapp.Utils

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.fatihsevban.instakotlinapp.R
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener

/**
 * Created by Emre on 9.04.2018.
 */
class UniversalImageLoader(val mContext:Context) {

    // properties
    val config:ImageLoaderConfiguration
        get(){

            // setting default options of the Universal image loader.
            val defaultOptions = DisplayImageOptions.Builder()
                    //.showImageOnLoading(defaultImage)
                    .showImageForEmptyUri(defaultImage)
                    .showImageOnFail(defaultImage)
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .resetViewBeforeLoading(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    //.displayer(FadeInBitmapDisplayer(400))
                    .build()

            return ImageLoaderConfiguration.Builder(mContext)
                    .defaultDisplayImageOptions(defaultOptions)
                    .memoryCache(WeakMemoryCache())
                    .diskCacheSize(100*1024*1024).build()
        }

    companion object {

        // static properties
        private val defaultImage = R.drawable.icon_profile

        /**
         * sets an image to an imageView.
         * @param imgURL is the Url of the image that is desired to be displayed.
         * @param mProgressBar is the progressbar of the image view.
         * @param heading indicates the source.(sd, storage, interner, url...)
         */
        fun setImage(imgURL:String, imageView:ImageView, mProgressBar:ProgressBar?, heading:String?) {

            ImageLoader.getInstance().displayImage("$heading$imgURL", imageView, object: ImageLoadingListener {

                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    if(mProgressBar!=null)
                        mProgressBar.visibility=View.GONE
                }

                override fun onLoadingStarted(imageUri: String?, view: View?) {
                    if(mProgressBar!=null)
                        mProgressBar.visibility=View.VISIBLE
                }

                override fun onLoadingCancelled(imageUri: String?, view: View?) {
                    if(mProgressBar!=null)
                        mProgressBar.visibility=View.GONE
                }

                override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                    if(mProgressBar!=null)
                        mProgressBar.visibility=View.GONE
                }

            })
        }
    }
}