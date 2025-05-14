package es.usj.jglopez.firebasechat.database

import android.content.SharedPreferences
import com.google.gson.Gson

const val USER = "userData"

class ForPreferencesStorageImpl(private val sharedPreferences: SharedPreferences) {


    private val gson = Gson()

    // Save user to SharedPreferences
    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(USER, userJson).apply()
    }

    // Load user from SharedPreferences
    fun getUser(): User? {
        val userJson = sharedPreferences.getString(USER, null)
        return userJson?.let {
            try {
                gson.fromJson(it, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Clear user from SharedPreferences
    fun clearUser() {
        sharedPreferences.edit().remove(USER).apply()
    }

}
