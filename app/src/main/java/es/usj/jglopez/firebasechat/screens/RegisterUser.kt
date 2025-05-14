package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.R
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.User
import es.usj.jglopez.firebasechat.databinding.ActivityMainBinding
import es.usj.jglopez.firebasechat.databinding.ActivityRegisterUserBinding

class RegisterUser : AppCompatActivity() {
    private val view by lazy{
        ActivityRegisterUserBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root) // Establece la vista correcta

        // Reference to "users" node
        val usersRef = Firebase.database.getReference("users")

        // Create a new User without ID
        view.btnCreateUser.setOnClickListener {
            val user = User(
                id = "",
                name = view.etUserName.text.toString(),
                password = view.etPassword.text.toString(),
                createdAt = System.currentTimeMillis(),
                chatrooms = HashMap()
            )

            // Push a new child (generates a unique key)
            val newUserRef = usersRef.push()

            // Copy the user object and add the generated ID
            val userWithId = user.copy(id = newUserRef.key!!)

            // Set the value
            newUserRef.setValue(userWithId)
                .addOnSuccessListener {
                    Toast.makeText(this, "User saved with ID ${userWithId.name}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving user", Toast.LENGTH_SHORT).show()
                }
            val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
            val preferences = ForPreferencesStorageImpl(sharedPreferences)
            preferences.saveUser(user)
            startActivity(Intent(this@RegisterUser, MainActivity::class.java))

        }
    }
}