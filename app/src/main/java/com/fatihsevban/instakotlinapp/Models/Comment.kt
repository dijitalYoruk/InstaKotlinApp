package com.fatihsevban.instakotlinapp.Models

class Comment {

    // properties
    var user_id: String? = null
    var post_id: String? = null
    var comment_id: String? = null
    var comment_content: String? = null
    var comment_date: Long? = null

    // constructors
    constructor()

    constructor(user_id: String?, post_id: String?, comment_id: String?, comment_content: String?, comment_like_count: String?, comment_date: Long?) {
        this.user_id = user_id
        this.post_id = post_id
        this.comment_id = comment_id
        this.comment_content = comment_content
        this.comment_date = comment_date
    }

    // methods
    override fun toString(): String {
        return "Comment(user_id=$user_id, post_id=$post_id, comment_id=$comment_id, comment_content=$comment_content, comment_date=$comment_date)"
    }

}