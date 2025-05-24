package es.usj.jglopez.firebasechat.database

import com.google.firebase.database.IgnoreExtraProperties

// For Message
@IgnoreExtraProperties
data class message(
    var senderName: String? = "",
    var messageText: String? = "",
    var timestamp: Long? = 0L
)