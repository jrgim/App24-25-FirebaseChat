package es.usj.jglopez.firebasechat.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.R
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.Message
import es.usj.jglopez.firebasechat.database.User
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
        setContentView(view.root) // Establece la vista correcta

        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)
        val user = preferences.getUser()

        val recyclerView = view.rvChatList
        val adapter = CustomAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
//        chatList.add(Chatroom("Chatroom 1", HashMap(), "User 1", System.currentTimeMillis(), "hola"))
//        chatList.add(Chatroom("Chatroom 2", HashMap(), "User 1", System.currentTimeMillis(), "aa"))
//        chatList.add(Chatroom("Chatroom 3", HashMap(), "User 1", System.currentTimeMillis(), "sdfdsaf"))


        val usersRef = Firebase.database.getReference("users")
        // Este listener se activa cada vez que cambia algo en los users de firebase
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Toast.makeText(this@MainActivity, "Data changed", Toast.LENGTH_SHORT).show()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    // Do something with the message
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })

        view.fbAddChat.setOnClickListener {
            val intent = Intent(this, CreateChat::class.java)
            startActivity(intent)
        }
    }
}
class CustomAdapter(
) : ListAdapter<chatroom, CustomAdapter.ChatroomViewHolder>(DiffCallback()) {

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
        holder.chatLastMessage.text = chatroom.lastMessage
    }

    // Este override es opcional, ya que ListAdapter lo maneja
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
