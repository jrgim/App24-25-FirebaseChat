package es.usj.jglopez.firebasechat.database

import com.google.firebase.database.IgnoreExtraProperties

// For Message
@IgnoreExtraProperties
data class message(
    var id: String? = "0",
    var senderName: String? = "",
    var messageText: String? = "",
    var timestamp: Long? = 0L, // Using a timestamp (like milliseconds since epoch)
    var nextMessageID: String? = ""
)