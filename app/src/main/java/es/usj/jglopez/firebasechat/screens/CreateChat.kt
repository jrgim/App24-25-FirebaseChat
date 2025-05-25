package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.adapterCheckbox.CheckboxAdapter
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.chatroom
import es.usj.jglopez.firebasechat.database.message
import es.usj.jglopez.firebasechat.databinding.ActivityCreateChatBinding
import es.usj.jglopez.firebasechat.screens.MainActivity.Companion.adapter
import es.usj.jglopez.firebasechat.screens.MainActivity.Companion.chatList

class CreateChat : AppCompatActivity() {

    private val view by lazy { ActivityCreateChatBinding.inflate(layoutInflater) }

    // Adapter for the list of users with checkboxes
    private lateinit var usersAdapter: CheckboxAdapter

    // List of all available users to add to the chat
    private val allUsers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        // Access the current user's name from shared preferences
        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)
        val currentUser = preferences.getUser()!!.name

        // Set up the RecyclerView with the checkbox adapter
        usersAdapter = CheckboxAdapter(allUsers)
        view.usersRecyclerView.layoutManager = LinearLayoutManager(this)
        view.usersRecyclerView.adapter = usersAdapter

        // Load users from Firebase (excluding current user)
        loadUsers(currentUser)

        // When the submit button is clicked
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

            // Create the initial message
            val initialMessage = message(
                senderName = currentUser,
                messageText = "Created the chat",
                timestamp = System.currentTimeMillis()
            )

            // Create the chatroom object
            val chat = chatroom(
                id = "",
                name = chatName,
                messages = mutableListOf(initialMessage),
                lastMessage = "Created the chat",
                participants = selectedParticipants,
                createdBy = currentUser,
                createdAt = System.currentTimeMillis()
            )

            // Store the chatroom in Firebase
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

            // Reload chat list from Firebase and update UI
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

    // Load all users from Firebase, except the current user
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
