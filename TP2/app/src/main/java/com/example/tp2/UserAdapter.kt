package com.example.tp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val users: ArrayList<User>, private val onItemClick: (User) -> Unit): RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    val favoriteUsers = ArrayList<User>()
    private var showOnlyFavorites = false

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.username)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.users_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = if (showOnlyFavorites) favoriteUsers[position] else users[position]
        holder.username.text = user.username

        holder.itemView.setOnClickListener {
            onItemClick.invoke(user)
        }
    }

    override fun getItemCount(): Int {
        return if (showOnlyFavorites) favoriteUsers.size else users.size
    }

    fun toggleFavoritesOnly(showOnlyFavorites: Boolean) {
        this.showOnlyFavorites = showOnlyFavorites
        notifyDataSetChanged()
    }
}