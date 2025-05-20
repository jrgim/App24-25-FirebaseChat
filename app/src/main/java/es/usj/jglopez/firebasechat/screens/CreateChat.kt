package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.message
import es.usj.jglopez.firebasechat.database.chatroom
import es.usj.jglopez.firebasechat.databinding.ActivityCreateChatBinding
import es.usj.jglopez.firebasechat.screens.MainActivity.Companion.adapter
import es.usj.jglopez.firebasechat.screens.MainActivity.Companion.chatList

class CreateChat : AppCompatActivity() {
    private val view by lazy {
        ActivityCreateChatBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)
        val initialMessage = message("0", preferences.getUser()!!.name, "Created the chat", System.currentTimeMillis())
        val messages = mutableListOf(initialMessage)

        view.submit.setOnClickListener {
            val chat1 = chatroom(
                id = "",
                name = view.name.text.toString(),
                messages = messages.toList(),
                lastMessage = "Created the chat",
                participants = hashMapOf(preferences.getUser()!!.name to true),
                createdBy = preferences.getUser()!!.name.toString(),
                createdAt = System.currentTimeMillis()
            )
            Log.d("chatroom", chat1.toString())
            val chatsRef = Firebase.database.getReference("chatrooms")

            val newChatRef = chatsRef.push()

            val chatroomWithId = chat1.copy(id = newChatRef.key!!)

            newChatRef.setValue(chatroomWithId)
                .addOnSuccessListener {
                    Toast.makeText(this, "Chatroom saved: ${chatroomWithId.name}", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving chatroom", Toast.LENGTH_SHORT).show()
                }

            chatsRef.get().addOnSuccessListener { dataSnapshot ->
                chatList.clear()
                for (chatSnapshot in dataSnapshot.children) {
                    try {
                        val chat = chatSnapshot.getValue(chatroom::class.java)
                        if (chat != null) {
                            chatList.add(chat)
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error parsing chatroom", e)
                    }
                }
                adapter.submitList(chatList.toList()) // Actualizamos la lista en el adapter
            }

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}