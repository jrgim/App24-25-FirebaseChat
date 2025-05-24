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
import com.google.firebase.database.database
import es.usj.jglopez.firebasechat.R
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.database.User
import es.usj.jglopez.firebasechat.databinding.ActivityMainBinding
import es.usj.jglopez.firebasechat.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private val view by lazy{
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root) // Establece la vista correcta

        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)

        val currentUser = preferences.getUser()

        if (currentUser != null) {
            // Usuario ya está logueado, ir al MainActivity
            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
        } else {
            // Usuario no logueado, ir al Register/Login Activity
            startActivity(Intent(this@SplashScreen, RegisterUser::class.java))
        }
        finish()
    }
}