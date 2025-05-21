package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
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

        loadUsers(preferences.getUser()!!.name) // Mostrar usuarios, excluyendo al actual

        view.submit.setOnClickListener {
            val chatName = view.name.text.toString().trim()

            if (chatName.isEmpty()) {
                Toast.makeText(this, "Chat name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedParticipants = hashMapOf(preferences.getUser()!!.name to true)

            for (i in 0 until view.checkboxContainer.childCount) {
                val child = view.checkboxContainer.getChildAt(i)
                if (child is CheckBox && child.isChecked) {
                    selectedParticipants[child.tag as String] = true
                }
            }

            if (selectedParticipants.size < 2) {
                Toast.makeText(this, "Select at least one other user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val initialMessage = message(
                id = "0",
                senderName = preferences.getUser()!!.name,
                messageText = "Created the chat",
                timestamp = System.currentTimeMillis()
            )
            val messages = mutableListOf(initialMessage)

            val chat = chatroom(
                id = "",
                name = chatName,
                messages = messages,
                lastMessage = "Created the chat",
                participants = selectedParticipants,
                createdBy = preferences.getUser()!!.name,
                createdAt = System.currentTimeMillis()
            )

            val chatsRef = Firebase.database.getReference("chatrooms")
            val newChatRef = chatsRef.push()
            val chatWithId = chat.copy(id = newChatRef.key!!)

            newChatRef.setValue(chatWithId)
                .addOnSuccessListener {
                    Toast.makeText(this, "Chatroom created!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving chatroom", Toast.LENGTH_SHORT).show()
                }

            // Refrescar la lista de chats
            chatsRef.get().addOnSuccessListener { dataSnapshot ->
                chatList.clear()
                for (chatSnapshot in dataSnapshot.children) {
                    val c = chatSnapshot.getValue(chatroom::class.java)
                    if (c != null) {
                        chatList.add(c)
                    }
                }
                adapter.submitList(chatList.toList())
            }

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loadUsers(currentUserName: String) {
        val usersRef = Firebase.database.getReference("users")

        usersRef.get().addOnSuccessListener { dataSnapshot ->
            for (userSnapshot in dataSnapshot.children) {
                val name = userSnapshot.child("name").getValue(String::class.java)
                if (name != null && name != currentUserName) {
                    val checkBox = CheckBox(this)
                    checkBox.text = name
                    checkBox.tag = name
                    view.checkboxContainer.addView(checkBox)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
        }
    }
}
