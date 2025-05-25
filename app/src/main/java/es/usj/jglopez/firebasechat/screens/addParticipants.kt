package es.usj.jglopez.firebasechat.screens

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.usj.jglopez.firebasechat.R
import es.usj.jglopez.firebasechat.adapterCheckbox.CheckboxAdapter

class AddParticipants : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CheckboxAdapter

    private val allUsers = mutableListOf<String>()
    private val currentParticipants = mutableSetOf<String>()

    private lateinit var chatroomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_participants)

        recyclerView = findViewById(R.id.usersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get chatroom ID passed from previous activity
        chatroomId = intent.getStringExtra("chatroomId") ?: ""

        // Load current participants first before showing available users
        loadCurrentParticipants()
    }

    // Load users who are already participants in this chatroom
    private fun loadCurrentParticipants() {
        val chatRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("chatrooms")
            .child(chatroomId)

        chatRef.get().addOnSuccessListener { snapshot ->
            val participantsMap = snapshot.child("participants").getValue() as? Map<String, Boolean> ?: emptyMap()

            currentParticipants.clear()
            currentParticipants.addAll(participantsMap.keys)

            // Now load users that are NOT already in the chat
            loadAllUsers()

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load participants", Toast.LENGTH_SHORT).show()
        }
    }

    // Load all users from Firebase who are not in the currentParticipants list
    private fun loadAllUsers() {
        val usersRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users")

        usersRef.get().addOnSuccessListener { snapshot ->
            allUsers.clear()

            for (userSnapshot in snapshot.children) {
                val name = userSnapshot.child("name").getValue(String::class.java)
                if (name != null && !currentParticipants.contains(name)) {
                    allUsers.add(name)
                }
            }

            // Set up adapter with users available to add
            adapter = CheckboxAdapter(allUsers)
            recyclerView.adapter = adapter

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
        }
    }

    fun onAddParticipantsClicked(view: android.view.View) {
        val selectedUsers = adapter.getSelectedUsers()

        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "Select at least one user to add", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare update map where each selected user is marked as a participant
        val updates = selectedUsers.associateWith { true }

        val chatRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("chatrooms")
            .child(chatroomId)
            .child("participants")

        // Add selected users to Firebase under the "participants" node
        chatRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Participants added!", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to add participants", Toast.LENGTH_SHORT).show()
        }
    }
}
