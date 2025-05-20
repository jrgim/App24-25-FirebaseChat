package es.usj.jglopez.firebasechat.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.R
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.message
import es.usj.jglopez.firebasechat.database.chatroom
import es.usj.jglopez.firebasechat.databinding.ActivityChatScreenBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class chatScreen : AppCompatActivity() {
    private val view by lazy {
        ActivityChatScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)

        val chatroomId = intent.getStringExtra("chatroom")

        val recyclerView = view.rvMessageScreen
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapterL = ChatAdapter()
        recyclerView.adapter = adapterL

        val chatsRef = Firebase.database.getReference("chatrooms")
        var chatroom = chatroom()
        chatsRef.get().addOnSuccessListener { dataSnapshot ->
            for (chatSnapshot in dataSnapshot.children) {
                if (chatSnapshot.key == chatroomId) {
                    // Manually parse chatroom fields
                    val id = chatSnapshot.child("id").value?.toString() ?: ""
                    val name = chatSnapshot.child("name").value?.toString() ?: "Unnamed chat"
                    val participants = chatSnapshot.child("participants").getValue() as? HashMap<String, Boolean>
                    val createdBy = chatSnapshot.child("createdBy").value?.toString() ?: "Unknown"
                    val createdAt = chatSnapshot.child("createdAt").value as? Long

                    // Parse messages list manually
                    val messageList = mutableListOf<message>()
                    val messagesSnapshot = chatSnapshot.child("messages")
                    for (messageSnapshot in messagesSnapshot.children) {
                        val msg = messageSnapshot.getValue(message::class.java)
                        if (msg != null) {
                            messageList.add(msg)
                        }
                    }
                    val safeList = messageList
                        .filter { it.timestamp != null }
                        .sortedBy { it.timestamp }

                    // Create chatroom object
                    val safeChatroom = chatroom(id, name, participants, messageList, createdBy, createdAt, safeList.lastOrNull()?.messageText ?: "No messages")

                    adapterL.submitList(safeList)
                }
            }
        }

        val messagesRef = chatsRef.child(chatroomId.toString()).child("messages")
        view.sendMessage.setOnClickListener {
            val message = message("1", preferences.getUser()!!.name, view.messageBody.text.toString(), System.currentTimeMillis())
            try{ chatsRef.child(chatroomId!!).child("messages").push().setValue(message) }
            catch (e: Exception){ Log.d("adapterList", e.toString()) }
        }

        chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsRef.get().addOnSuccessListener { dataSnapshot ->
                    for (chatSnapshot in dataSnapshot.children) {
                        if (chatSnapshot.key == chatroomId) {
                            // Manually parse chatroom fields instead of using getValue(chatroom::class.java)
                            val id = chatSnapshot.child("id").value?.toString() ?: ""
                            val name = chatSnapshot.child("name").value?.toString() ?: "Unnamed chat"
                            val participants = chatSnapshot.child("participants").getValue() as? HashMap<String, Boolean>
                            val createdBy = chatSnapshot.child("createdBy").value?.toString() ?: "Unknown"
                            val createdAt = chatSnapshot.child("createdAt").value as? Long

                            // Parse messages manually from the messages child node
                            val messageList = mutableListOf<message>()
                            val messagesSnapshot = chatSnapshot.child("messages")
                            for (messageSnapshot in messagesSnapshot.children) {
                                val msg = messageSnapshot.getValue(message::class.java)
                                if (msg != null) {
                                    messageList.add(msg)
                                }
                            }

                            // Filter out messages with null timestamp and sort safely
                            val safeList = messageList
                                .filter { it.timestamp != null }
                                .sortedBy { it.timestamp }

                            // Optional: create a safe chatroom instance if you need it
                            chatroom = chatroom(id, name, participants, messageList, createdBy, createdAt, safeList.lastOrNull()?.messageText ?: "No messages")

                            // Submit sorted and safe message list to adapter
                            adapterL.submitList(safeList)
                        }
                    }
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                // Handle error properly here
                Log.e("Firebase", "Error loading chatroom messages", error.toException())
            }
        })

    }
}
class ChatAdapter : ListAdapter<message, ChatAdapter.ChatroomViewHolder>(DiffCallback()) {
    class ChatroomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sender: TextView = view.findViewById(R.id.senderNameTextView)
        val message: TextView = view.findViewById(R.id.messageTextView)
        val timestamp: TextView = view.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_message_layout, parent, false) // Make sure this matches your layout name
        return ChatroomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatroomViewHolder, position: Int) {
        val messageItem = getItem(position)
        holder.sender.text = messageItem.senderName
        holder.message.text = messageItem.messageText

        // Format timestamp (e.g., "14:30")
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = messageItem.timestamp?.let { timeFormat.format(Date(it)) } ?: ""
        holder.timestamp.text = timeString
    }

    class DiffCallback : DiffUtil.ItemCallback<message>() {
        override fun areItemsTheSame(oldItem: message, newItem: message): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.senderName == newItem.senderName
        }

        override fun areContentsTheSame(oldItem: message, newItem: message): Boolean {
            return oldItem == newItem
        }
    }
}