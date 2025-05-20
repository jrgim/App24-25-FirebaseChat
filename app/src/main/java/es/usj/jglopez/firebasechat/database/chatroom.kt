package es.usj.jglopez.firebasechat.database

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class chatroom(
    var id: String? = "",
    var name: String? = "",
    var participants: HashMap<String, Boolean>? = HashMap(),
    var messages: List<message>? = emptyList<message>(),
    var createdBy: String? = "",
    var createdAt: Long? = 0L,
    var lastMessage: String = ""
)

