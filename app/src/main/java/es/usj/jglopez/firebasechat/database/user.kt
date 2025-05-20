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
