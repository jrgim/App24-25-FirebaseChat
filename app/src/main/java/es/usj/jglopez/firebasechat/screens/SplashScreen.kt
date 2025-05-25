package es.usj.jglopez.firebasechat.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import es.usj.jglopez.firebasechat.database.ForPreferencesStorageImpl
import es.usj.jglopez.firebasechat.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private val view by lazy{
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        /**
         * We used shared preferences to store the user data necessary to load the
         * chatrooms, so in firebase we have all the users and in the local memory of
         * phone it will only store the necessary info of the user, and when logging out it will
         * be deleted from the local memory.
         */
        val sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
        val preferences = ForPreferencesStorageImpl(sharedPreferences)

        val currentUser = preferences.getUser()

        if (currentUser != null) {
            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
        } else {
            startActivity(Intent(this@SplashScreen, RegisterUser::class.java))
        }
        finish()
    }
}