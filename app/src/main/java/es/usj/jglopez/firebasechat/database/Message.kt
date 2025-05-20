package es.usj.jglopez.firebasechat.database

import com.google.firebase.database.IgnoreExtraProperties

// For Message
@IgnoreExtraProperties
data class Message(
    var senderId: String? = "",
    var senderName: String? = "",
    var text: String? = "",
    var timestamp: Long? = 0L // Using a timestamp (like milliseconds since epoch)
)