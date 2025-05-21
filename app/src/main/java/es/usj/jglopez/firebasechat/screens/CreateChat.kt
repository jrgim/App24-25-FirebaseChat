package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.adapters.CheckboxAdapter
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.chatroom
import es.usj.jglopez.firebasechat.database.message
import es.usj.jglopez.firebasechat.databinding.ActivityCreateChatBinding
import es.usj.jglopez.firebasechat.screens.MainActivity.Companion.adapter
import es.usj.jglopez.firebasechat.screens.MainActivity.Companion.chatList

class CreateChat : AppCompatActivity() {

    private val view by lazy { ActivityCreateChatBinding.inflate(layoutInflater) }
    private lateinit var usersAdapter: CheckboxAdapter
    private val allUsers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)
        val currentUser = preferences.getUser()!!.name

        usersAdapter = CheckboxAdapter(allUsers)
        view.usersRecyclerView.layoutManager = LinearLayoutManager(this)
        view.usersRecyclerView.adapter = usersAdapter

        loadUsers(currentUser)

        view.submit.setOnClickListener {
            val chatName = view.name.text.toString().trim()
            if (chatName.isEmpty()) {
                Toast.makeText(this, "Chat name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedParticipants = hashMapOf(currentUser to true)
            val selectedUsers = usersAdapter.getSelectedUsers()

            if (selectedUsers.isEmpty()) {
                Toast.makeText(this, "Select at least one other user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedUsers.forEach { selectedParticipants[it] = true }

            val initialMessage = message(
                id = "0",
                senderName = currentUser,
                messageText = "Created the chat",
                timestamp = System.currentTimeMillis()
            )

            val chat = chatroom(
                id = "",
                name = chatName,
                messages = mutableListOf(initialMessage),
                lastMessage = "Created the chat",
                participants = selectedParticipants,
                createdBy = currentUser,
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

            // Actualizar lista de chats
            chatsRef.get().addOnSuccessListener { dataSnapshot ->
                chatList.clear()
                for (chatSnapshot in dataSnapshot.children) {
                    val c = chatSnapshot.getValue(chatroom::class.java)
                    if (c != null) chatList.add(c)
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
            allUsers.clear()
            for (userSnapshot in dataSnapshot.children) {
                val name = userSnapshot.child("name").getValue(String::class.java)
                if (name != null && name != currentUserName) {
                    allUsers.add(name)
                }
            }
            usersAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
        }
    }
}
