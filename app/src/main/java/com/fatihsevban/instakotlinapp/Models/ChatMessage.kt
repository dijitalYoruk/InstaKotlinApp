package com.fatihsevban.instakotlinapp.Models

class ChatMessage {

    // properties
    var message_sender_uid: String? = null
    var chat_message: String? = null
    var is_seen: Boolean? = null
    var time: String? = null
    var type: String? = null

    // constructors
    constructor()

    constructor(message_sender_uid: String?, chat_message: String?, is_seen: Boolean?, time: String?, type: String?) {
        this.message_sender_uid = message_sender_uid
        this.chat_message = chat_message
        this.is_seen = is_seen
        this.time = time
        this.type = type
    }

    // methods
    override fun toString(): String {
        return "ChatMessage(message_sender_uid=$message_sender_uid, chat_message=$chat_message, is_seen=$is_seen, time=$time, type=$type)"
    }

}