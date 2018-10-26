package com.fatihsevban.instakotlinapp.Home.Chatting

import android.widget.Filter
import com.fatihsevban.instakotlinapp.Models.User

class FilterHelperFollowing ( var adapter : AdapterRecHomeFollowing , var usersList : ArrayList<User> ) : Filter() {

    override fun performFiltering(constraints: CharSequence?): FilterResults {

        if ( constraints!= null && constraints.isNotEmpty() ) {

            val enteredText = constraints.toString().toLowerCase()
            val filteredUsers = ArrayList<User>()

            for ( user in usersList )
                if (user.user_name?.toLowerCase()!!.trim().contains(enteredText) )
                    filteredUsers.add( user )

            val result = FilterResults()
            result.values = filteredUsers
            result.count = filteredUsers.size

            return result
        }

        val result = FilterResults()
        result.values = usersList
        result.count = usersList.size
        return result
    }

    override fun publishResults(p0: CharSequence?, filterResults: FilterResults?) {
        adapter.users = (filterResults?.values as ArrayList<User>)
        adapter.notifyDataSetChanged()
    }
}