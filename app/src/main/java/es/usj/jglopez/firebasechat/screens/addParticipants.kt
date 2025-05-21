package es.usj.jglopez.firebasechat.screens

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.R
import es.usj.jglopez.firebasechat.adapters.CheckboxAdapter

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

        chatroomId = intent.getStringExtra("chatroomId") ?: ""

        loadCurrentParticipants()
    }

    private fun loadCurrentParticipants() {
        val chatRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("chatrooms").child(chatroomId)
        chatRef.get().addOnSuccessListener { snapshot ->
            val participantsMap = snapshot.child("participants").getValue() as? Map<String, Boolean> ?: emptyMap()
            currentParticipants.clear()
            currentParticipants.addAll(participantsMap.keys)
            loadAllUsers()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load participants", Toast.LENGTH_SHORT).show()
        }
    }

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

        val chatRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("chatrooms")
            .child(chatroomId)
            .child("participants")

        val updates = selectedUsers.associateWith { true }

        chatRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Participants added!", Toast.LENGTH_SHORT).show()
            finish() // Cerramos pantalla y volvemos atrás
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to add participants", Toast.LENGTH_SHORT).show()
        }
    }
}
