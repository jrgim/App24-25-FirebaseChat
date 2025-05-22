package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    private val view by lazy { ActivityMainBinding.inflate(layoutInflater) }

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
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val chatsRef = Firebase.database.getReference("chatrooms")
        chatsRef.get().addOnSuccessListener { dataSnapshot ->
            chatList.clear()
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
    }

    fun deleteChat(chatroom: chatroom) {
        val chatsRef = Firebase.database.getReference("chatrooms")
        val chatId = chatroom.id ?: return

        chatsRef.child(chatId).removeValue()
            .addOnSuccessListener {
                chatList.removeAll { it.id == chatId }
                adapter.submitList(chatList.toList())
                Toast.makeText(this, "Chat eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar el chat", Toast.LENGTH_SHORT).show()
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

            // NUEVO: Pulsación larga para borrar chat
            holder.itemView.setOnLongClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Eliminar chat")
                    .setMessage("¿Seguro que quieres eliminar este chat?")
                    .setPositiveButton("Sí") { _, _ ->
                        (holder.itemView.context as? MainActivity)?.deleteChat(chatroom)
                    }
                    .setNegativeButton("No", null)
                    .show()
                true
            }
        }

        override fun getItemCount(): Int {
            return currentList.size
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<chatroom>() {
        override fun areItemsTheSame(oldItem: chatroom, newItem: chatroom): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: chatroom, newItem: chatroom): Boolean {
            return oldItem == newItem
        }
    }
}
