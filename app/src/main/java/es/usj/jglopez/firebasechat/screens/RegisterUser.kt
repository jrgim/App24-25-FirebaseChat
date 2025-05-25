package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.User
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

        /**
         * This first part of the function uses Firebase Authorization to manage the Login of the
         * users of the app.
         */
        view.btnCreateUser.setOnClickListener {
            val username = view.etUserName.text.toString().trim()
            val password = view.etPassword.text.toString()

            /**
             * If the username or password is empty it will show a Toast message
             */
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /**
             * We hardcode the email so that the users only need to remember the user and password,
             * and dont need to enter the full mail every time.
             */
            val email = "$username@example.com"
            val auth = FirebaseAuth.getInstance()
            val dbRef = FirebaseDatabase.getInstance().getReference("users")

            /**
             * This first part checks if a login is possible, meaning that if the user already
             * exists it will directly login, and on failure it will try to register
             */
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show()
                    preferences.saveUser(User(
                        name = username,
                        createdAt = System.currentTimeMillis(),
                        chatrooms = hashMapOf("testRoom" to false)
                    ))
                    goToMainActivity(authResult.user?.uid ?: "", username)
                }
                .addOnFailureListener { loginError ->
                    /**
                     * Now if the login fails, it will try to register the user, and if the
                     * registration fails it will show a Toast message with the error message
                     */
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val uid = authResult.user?.uid ?: return@addOnSuccessListener
                            val user = User(
                                name = username,
                                createdAt = System.currentTimeMillis(),
                                chatrooms = hashMapOf("testRoom" to false)
                            )

                            /**
                             * If the registration is successful, it will save the user data in the
                             * database and in the shared preferences
                             */
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
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", uid)
        intent.putExtra("userName", name)
        startActivity(intent)
        finish()
    }
}