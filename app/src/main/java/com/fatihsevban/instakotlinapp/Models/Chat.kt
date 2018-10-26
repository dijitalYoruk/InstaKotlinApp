package com.fatihsevban.instakotlinapp.Models

class Chat {

    // properties
    var is_seen: Boolean? = null
    var time: String? = null
    var contact_id: String? = null
    var last_message: String? = null

    // constructors
    constructor()

    constructor(is_seen: Boolean?, time: String?, contact_id: String?, last_message: String?) {
        this.is_seen = is_seen
        this.time = time
        this.contact_id = contact_id
        this.last_message = last_message
    }

    // methods
    override fun toString(): String {
        return "Chat(is_seen=$is_seen, time=$time, contact_id=$contact_id, last_message=$last_message)"
    }

}