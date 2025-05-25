package es.usj.jglopez.firebasechat.database

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var name: String,
    var createdAt: Long,
    var chatrooms: HashMap<String, Boolean>
)
