package com.fatihsevban.instakotlinapp.Models

class User {

    // properties
    var email: String? = null
    var user_name: String? = null
    var name_and_surname: String? = null
    var phone_number: String? = null
    var user_id: String? = null
    var _hidden: Boolean? = null
    var profile_picture: String? = null
    var biography: String? = null
    var web_site: String? = null

    // constructors

    constructor(){}


    constructor(user: User) {
        this.email = user.email
        this.user_name = user.user_name
        this.name_and_surname = user.name_and_surname
        this.phone_number = user.phone_number
        this.user_id = user.user_id
        this._hidden = user._hidden
        this.profile_picture = user.profile_picture
        this.biography = user.biography
        this.web_site = user.web_site
    }

    constructor(email: String?, user_name: String?, name_and_surname: String?, phone_number: String?, user_id: String?, _hidden: Boolean?, profile_picture: String?, biography: String?, web_site: String?) {
        this.email = email
        this.user_name = user_name
        this.name_and_surname = name_and_surname
        this.phone_number = phone_number
        this.user_id = user_id
        this._hidden = _hidden
        this.profile_picture = profile_picture
        this.biography = biography
        this.web_site = web_site
    }

    override fun toString(): String {
        return "User(email=$email, user_name=$user_name, name_and_surname=$name_and_surname, phone_number=$phone_number, user_id=$user_id, _hidden=$_hidden, profile_picture=$profile_picture, biography=$biography, web_site=$web_site)"
    }

    // methods

}