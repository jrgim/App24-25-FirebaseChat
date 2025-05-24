package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.R
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.User
import es.usj.jglopez.firebasechat.databinding.ActivityMainBinding
import es.usj.jglopez.firebasechat.databinding.ActivityRegisterUserBinding

class RegisterUser : AppCompatActivity() {
    private val view by lazy {
        ActivityRegisterUserBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)
        preferences.clearUser()

        view.btnCreateUser.setOnClickListener {
            val username = view.etUserName.text.toString().trim()
            val password = view.etPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "$username@example.com"
            val auth = FirebaseAuth.getInstance()
            val dbRef = FirebaseDatabase.getInstance().getReference("users")

            // 1. Try to log in
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show()
                    preferences.saveUser(User(
                        id = authResult.user?.uid ?: "",
                        name = username,
                        password = "",
                        createdAt = System.currentTimeMillis(),
                        chatrooms = hashMapOf("testRoom" to false)
                    ))
                    goToMainActivity(authResult.user?.uid ?: "", username)
                }
                .addOnFailureListener { loginError ->
                    // 2. If login fails, try to register
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val uid = authResult.user?.uid ?: return@addOnSuccessListener
                            val user = User(
                                id = uid,
                                name = username,
                                password = "",
                                createdAt = System.currentTimeMillis(),
                                chatrooms = hashMapOf("testRoom" to false)
                            )

                            preferences.saveUser(user)

                            dbRef.child(uid).setValue(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "User registered!", Toast.LENGTH_SHORT)
                                        .show()
                                    goToMainActivity(uid, username)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Failed to save user data",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Auth failed: ${it.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
        }
    }

    private fun goToMainActivity(uid: String, name: String) {
        // You can also save user locally if needed
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", uid)
        intent.putExtra("userName", name)
        startActivity(intent)
        finish()
    }
}