package es.usj.jglopez.firebasechat.screens

import android.content.Intent
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
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.R
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.message
import es.usj.jglopez.firebasechat.database.chatroom
import es.usj.jglopez.firebasechat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val view by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val chatList = mutableListOf<chatroom>()

    companion object {
        val adapter = CustomAdapter()
        val chatList = mutableListOf<chatroom>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)
        val user = preferences.getUser()
        val userName = user?.name

        val recyclerView = view.rvChatList
        val adapter = CustomAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val usersRef = Firebase.database.getReference("users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Toast.makeText(this@MainActivity, "Data changed", Toast.LENGTH_SHORT).show()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        val chatsRef = Firebase.database.getReference("chatrooms")
        chatsRef.get().addOnSuccessListener { dataSnapshot ->
            chatList.clear()
            for (chatSnapshot in dataSnapshot.children) {
                val chat = chatSnapshot
                val messageList = mutableListOf<message>()
                val messagesSnapshot = chatSnapshot.child("messages")
                for (messageSnapshot in messagesSnapshot.children) {
                    val msg = messageSnapshot.getValue(message::class.java)
                    if (msg != null) {
                        messageList.add(msg)
                    }
                }
                messageList.sortBy { it.timestamp }
                val lastMessage = messageList.lastOrNull()?.messageText ?: "No messages"
                val id = chatSnapshot.child("id")
                val name = chatSnapshot.child("name")
                val participants = chatSnapshot.child("participants")
                val createdBy = chatSnapshot.child("createdBy")
                val createdAt = chatSnapshot.child("createdAt")
                val safechat = chatroom(id.value.toString(), name.value.toString(), participants.value as? HashMap<String, Boolean>, messageList, createdBy.value.toString(), createdAt.value as? Long, lastMessage)
                if (chat != null && safechat.participants?.containsKey(userName) == true) {chatList.add(safechat)}

            }
            adapter.submitList(chatList.toList())
        }

        chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsRef.get().addOnSuccessListener { dataSnapshot ->
                    chatList.clear()
                    try {
                        for (chatSnapshot in dataSnapshot.children) {
                            val messageList = mutableListOf<message>()
                            val messagesSnapshot = chatSnapshot.child("messages")
                            for (messageSnapshot in messagesSnapshot.children) {
                                val msg = messageSnapshot.getValue(message::class.java)
                                if (msg != null) {
                                    messageList.add(msg)
                                }
                            }
                            messageList.sortBy { it.timestamp }
                            val lastMessage = messageList.lastOrNull()?.messageText ?: "No messages"

                            val id = chatSnapshot.child("id").value?.toString() ?: ""
                            val name = chatSnapshot.child("name").value?.toString() ?: "Unnamed chat"
                            val participants = chatSnapshot.child("participants").getValue() as? HashMap<String, Boolean>
                            val createdBy = chatSnapshot.child("createdBy").value?.toString() ?: "Unknown"
                            val createdAt = chatSnapshot.child("createdAt").value as? Long

                            val safechat = chatroom(id, name, participants, messageList, createdBy, createdAt, lastMessage)

                            if (safechat.participants?.containsKey(userName) == true) {
                                chatList.add(safechat)
                            }
                        }
                        adapter.submitList(chatList.toList())
                    } catch (e: Exception) {
                        Log.d("adapterList", e.toString())
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Error: ${error.message}")
            }
        })


        view.fbAddChat.setOnClickListener {
            val intent = Intent(this, CreateChat::class.java)
            startActivity(intent)
        }

        view.fbLogout.setOnClickListener {
            preferences.clearUser()
            val intent = Intent(this, SplashScreen::class.java)
            startActivity(intent)
            finish()
        }

    }
}

class CustomAdapter : ListAdapter<chatroom, CustomAdapter.ChatroomViewHolder>(DiffCallback()) {
    class ChatroomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatName: TextView = view.findViewById(R.id.tvChatName)
        val chatLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_preview, parent, false)
        return ChatroomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatroomViewHolder, position: Int) {
        val chatroom = getItem(position)
        holder.chatName.text = chatroom.name ?: "Unnamed chat"
        val list = chatroom.messages
        val lastSender = list?.lastOrNull()?.senderName ?: "No Sender"
        holder.chatLastMessage.text = "${lastSender}: ${chatroom.lastMessage}"

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, chatScreen::class.java)
            intent.putExtra("chatroomID", chatroom.id)
            intent.putExtra("chatroomName", chatroom.name)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class DiffCallback : DiffUtil.ItemCallback<chatroom>() {
        override fun areItemsTheSame(oldItem: chatroom, newItem: chatroom): Boolean {
            return oldItem.name == newItem.name // Cambiar por `id` si lo agregas en el futuro
        }
        override fun areContentsTheSame(oldItem: chatroom, newItem: chatroom): Boolean {
            return oldItem == newItem
        }
    }
}
