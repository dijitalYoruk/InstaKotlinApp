package com.fatihsevban.instakotlinapp.Models

class Notification {

    // properties
    var time : Long? = null
    var notification_type : String? = null
    var notification_id : String? = null
    var user_id : String? = null
    var post_id: String? = null

    // constructors
    constructor()

    constructor(time: Long?, notification_type: String?, notification_id: String?, user_id: String?, post_id: String?) {
        this.time = time
        this.notification_type = notification_type
        this.notification_id = notification_id
        this.user_id = user_id
        this.post_id = post_id
    }

    // methods
    override fun toString(): String {
        return "Notification(time=$time, notification_type=$notification_type, notification_id=$notification_id, user_id=$user_id, post_id=$post_id)"
    }

}