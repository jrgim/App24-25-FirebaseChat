package es.usj.jglopez.firebasechat.screens

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import es.usj.jglopez.firebasechat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val view by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root) // Establece la vista correcta

        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)
        val user = preferences.getUser()
        if (user != null) {
            view.text.text = "Bienvenido ${user.name}"
        }
        val myRef = Firebase.database.getReference("users")

        // Este listener se activa cada vez que cambia algo en los users de firebase
        myRef.addValueEventListener(object : ValueEventListener {
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

//        // Reference to "users" node
//        val usersRef = Firebase.database.getReference("users")
//
//        // Create a new User without ID
//        val user = User(
//            id = "",
//            name = "Arkaitz",
//            pass
//            createdAt = System.currentTimeMillis(),
//            lastSeen = System.currentTimeMillis(),
//            chatrooms = HashMap()
//        )
//
//        // Push a new child (generates a unique key)
//        val newUserRef = usersRef.push()
//
//        // Copy the user object and add the generated ID
//        val userWithId = user.copy(id = newUserRef.key!!)
//
//        // Set the value
//        newUserRef.setValue(userWithId)
//            .addOnSuccessListener {
//                Toast.makeText(this, "User saved with ID ${userWithId.name}", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Error saving user", Toast.LENGTH_SHORT).show()
//            }
    }
}