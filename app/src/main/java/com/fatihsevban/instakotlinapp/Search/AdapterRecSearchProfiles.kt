package com.fatihsevban.instakotlinapp.Search

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatihsevban.instakotlinapp.Models.User
import com.fatihsevban.instakotlinapp.R
import com.fatihsevban.instakotlinapp.Utils.EventBusDataEvent
import com.fatihsevban.instakotlinapp.Utils.UniversalImageLoader
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.list_item_profile.view.*
import org.greenrobot.eventbus.EventBus

class AdapterRecSearchProfiles(val context: Context, var users: ArrayList<User>): RecyclerView.Adapter<AdapterRecSearchProfiles.MyViewHolder>(), Filterable {

    // properties
    val inflater = LayoutInflater.from(context)
    var mFilter = FilterHelperSearch( this , users )

    /**
     * determines which layout will be inflated.
     * @param parent is the parent layout of each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterRecSearchProfiles.MyViewHolder {
        val view = inflater.inflate(R.layout.list_item_profile, parent, false)
        return MyViewHolder(view)
    }

    /**
     * gets the item count.
     */
    override fun getItemCount(): Int {
        return users.size
    }

    /**
     * adds functionality and sets each list item.
     * @param holder holds the all required views.
     * @param position is the position of the list item.
     */
    override fun onBindViewHolder(holder: AdapterRecSearchProfiles.MyViewHolder, position: Int) {
        val user = users[position]
        holder.listItem.setOnClickListener { displayProfile(user) }
        holder.tvNameAndSurname.text = user.name_and_surname
        holder.tvUsername.text = user.user_name
        setProfileImage(user, holder)
    }

    /**
     * sets profile image of the user.
     * @param user is the user whose profile image will be displayed.
     * @param holder holds all the corresponding views.
     */
    private fun setProfileImage(user: User, holder: MyViewHolder) {

        if (!user.profile_picture!!.equals("not determined"))
            UniversalImageLoader.setImage(user.profile_picture!!,
                    holder.imgProfile, null, "")

        else {
            holder.imgProfile.setImageResource(R.drawable.icon_profile)
        }
    }

    /**
     * diplays the profile in another fragment.
     * @param user is the user whose profile will be displayed.
     */
    private fun displayProfile(user: User) {

        (context as ActivitySearch).searchRootLayout?.visibility = View.INVISIBLE
        val transaction = context.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.searchContainerLayout, FragmentSearchProfile())
        transaction.addToBackStack("ADD FRAG SEARCH PROFILE")
        transaction.commit()

        EventBus.getDefault().postSticky(EventBusDataEvent.SendProfileData(user))
    }

    /**
     * gets filter for search view.
     */
    override fun getFilter(): Filter { return mFilter }

    /**
     * holds all the required views of each list item.
     */
    inner class MyViewHolder( itemView: View? ) : RecyclerView.ViewHolder( itemView ) {
        var listItem = itemView as ConstraintLayout
        val tvUsername = listItem.tvUsername
        val tvNameAndSurname = listItem.tvNameAndSurname
        val imgProfile = listItem.imgProfile

    }

}