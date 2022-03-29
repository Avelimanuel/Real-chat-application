package com.example.a2chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row_newmessage.view.*

class NewMessageactivity : AppCompatActivity() {

    private lateinit var recyclerView:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_messageactivity)

        supportActionBar?.title = "Select user"

        recyclerView = findViewById(R.id.recyclerview_newmessage)


      /*  val adapter = GroupAdapter<ViewHolder>()
        adapter.add(UserItem())
        adapter.add(UserItem())
        adapter.add(UserItem())

        recyclerView.adapter = adapter*/

        fetchUsers()
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/Users")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()


                snapshot.children.forEach{
                    Log.d("NewMessage","${it.toString()}")
                    val user = it.getValue(User::class.java)
                    if (user != null){
                        adapter.add(UserItem(user))
                    }
                }
                recyclerView.adapter = adapter
                
                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem

                    val intent = Intent( view.context,ChatLogActivity::class.java)
                   // intent.putExtra(USER_KEY,userItem.user.username)

                    intent.putExtra(USER_KEY,userItem.user)

                    startActivity(intent)

                    finish()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}

class UserItem(val user:User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_newmessage.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.userProfileImage)

    }

    override fun getLayout(): Int {
       return R.layout.user_row_newmessage
    }

}