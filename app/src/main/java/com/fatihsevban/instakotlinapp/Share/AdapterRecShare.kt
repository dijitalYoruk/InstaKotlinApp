package com.fatihsevban.instakotlinapp.Share

import android.content.Context
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import kotlinx.android.synthetic.main.fragment_share_gallery.view.*
import kotlinx.android.synthetic.main.grid_share_cell_layout.view.*

class AdapterRecShare (var imageOrVideoURIs : ArrayList<String>, private val fragmentShare: FragmentShareGallery ) : RecyclerView.Adapter<AdapterRecShare.MyViewHolder>() {

    // properties
    private val inflater = LayoutInflater.from(fragmentShare.context)

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRecShare.MyViewHolder {
        val view = inflater.inflate(R.layout.grid_share_cell_layout, parent, false)
        return MyViewHolder(view)
    }

    /**
     * gets the item count.
     */
    override fun getItemCount(): Int { return imageOrVideoURIs.size }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: AdapterRecShare.MyViewHolder, position: Int) {

        holder.gridItem.setOnClickListener { view ->
            fragmentShare.displaySelectedImageOrVideo(position)
        }

        setGridItem(position, holder.gridItem)

        UniversalImageLoader.setImage(imageOrVideoURIs[position], holder.imgShareGridCell,
                holder.gridProgressBar, "file:/")
    }

    /**
     * sets each grid items layout depending on being video or image.
     */
    private fun setGridItem(position: Int, gridItem: ConstraintLayout) {

        // getting document type extension.
        val lastPointIndex = imageOrVideoURIs[position].lastIndexOf(".")
        val documentType = imageOrVideoURIs[position].substring(lastPointIndex)

        // setting grid item
        if( documentType.equals(".jpg") || documentType.equals(".jpeg") || documentType.equals(".png") )
            gridItem.imgPlay?.visibility = View.INVISIBLE

        else if (documentType.equals(".mp4"))
            gridItem.imgPlay?.visibility = View.VISIBLE
    }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {
        var gridItem = itemView as ConstraintLayout
        var imgShareGridCell = gridItem.imgShareGridCell
        var gridProgressBar = gridItem.gridProgressBar
        var imgPlay = gridItem.imgPlay
    }

}