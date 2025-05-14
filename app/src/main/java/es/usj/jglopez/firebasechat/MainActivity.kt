package es.usj.jglopez.firebasechat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.auth.User
import es.usj.jglopez.firebasechat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val view by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        setContentView(R.layout.activity_main)

        // Inicializa la referencia a la base de datos
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child("users").setValue(1)

    }

}