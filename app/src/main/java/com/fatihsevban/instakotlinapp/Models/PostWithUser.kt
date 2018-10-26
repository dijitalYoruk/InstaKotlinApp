package com.fatihsevban.instakotlinapp.Models

class PostWithUser {

    // properties
    var user: User? = null
    var post: Post? = null

    // constructors
    constructor()

    constructor(user: User?, post: Post?) {
        this.user = user
        this.post = post
    }

    // methods
    override fun toString(): String {
        return "UserWithPosts(user=$user, post=$post)"
    }

}