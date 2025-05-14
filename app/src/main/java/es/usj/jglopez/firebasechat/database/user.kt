package es.usj.jglopez.firebasechat.database

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties // Optional: Use if you might add fields later and don't want crashes
data class User(
    var id: String,
    var name: String,
    var password: String,
    var createdAt: Long,
    var chatrooms: HashMap<String, Boolean>
)

// For Chatroom
@IgnoreExtraProperties
data class Chatroom(
    var name: String? = "",
    // Use a map for participants (userId -> true)
    var participants: HashMap<String, Boolean>? = HashMap(),
    var createdBy: String? = "",
    var createdAt: Long? = 0L
)

// For Message
@IgnoreExtraProperties
data class Message(
    var senderId: String? = "",
    var senderName: String? = "",
    var text: String? = "",
    var timestamp: Long? = 0L // Using a timestamp (like milliseconds since epoch)
)