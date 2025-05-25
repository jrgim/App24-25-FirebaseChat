package es.usj.jglopez.firebasechat.adapterCheckbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import es.usj.jglopez.firebasechat.R

class CheckboxAdapter(
    private val users: List<String>,
    private val selectedUsers: MutableSet<String> = mutableSetOf()
) : RecyclerView.Adapter<CheckboxAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.userCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.checkbox_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val userName = users[position]
        holder.checkBox.text = userName
        holder.checkBox.isChecked = selectedUsers.contains(userName)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedUsers.add(userName)
            else selectedUsers.remove(userName)
        }
    }

    override fun getItemCount(): Int = users.size

    fun getSelectedUsers(): Set<String> = selectedUsers
}