package com.fatihsevban.instakotlinapp.Models

class Post {

    // properties
    var user_id: String? = null
    var post_id: String? = null
    var upload_date: Long? = null
    var explanation: String? = null
    var photo_uri: String? = null

    // constructors
    constructor()

    constructor(user_id: String?, post_id: String?, upload_date: Long?, explanation: String?, photo_uri: String?) {
        this.user_id = user_id
        this.post_id = post_id
        this.upload_date = upload_date
        this.explanation = explanation
        this.photo_uri = photo_uri
    }

    // methods
    override fun toString(): String {
        return "Post(user_id=$user_id, post_id=$post_id, upload_date=$upload_date, explanation=$explanation, photo_uri=$photo_uri)"
    }

}