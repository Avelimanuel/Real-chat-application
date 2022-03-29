package com.example.a2chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.a2chat.NewMessageactivity.Companion.USER_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser:User? = null
    }

    private lateinit var recyclerViewLatestMsg:RecyclerView
    private lateinit var usernameLatestMessages:TextView
    private lateinit var latestMessage:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerViewLatestMsg = findViewById(R.id.recyclerview_latest_messages)
       // usernameLatestMessages = findViewById(R.id.username_latest_messages)
        //latestMessage = findViewById(R.id.message_latest_messages)

        recyclerViewLatestMsg.adapter = adapter

        recyclerViewLatestMsg.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        //setting item click listener on the adapter.
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this,ChatLogActivity::class.java)

            val row = item as LatestMessagesRow

            intent.putExtra(USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }

        //setupDummyRows()

        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserisLoggedIn()
    }

    val latestMessagesMap = HashMap<String,ChatLogActivity.ChatMessage>()

    private fun  refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach(){
            adapter.add(LatestMessagesRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    class LatestMessagesRow (val chatMessage: ChatLogActivity.ChatMessage):Item<ViewHolder>(){
        var chatPartnerUser:User? = null

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.message_latest_messages.text = chatMessage.text

            val chatPartnerId:String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            }else{
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/Users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser = snapshot.getValue(User::class.java)
                    viewHolder.itemView.username_latest_messages.text = chatPartnerUser?.username


                    //loading the user profile
                    val targetImageView = viewHolder.itemView.imageView_latest_messages
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })



        }

        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }

    }
    val adapter = GroupAdapter<ViewHolder>()

   /* private fun setupDummyRows() {


        adapter.add(LatestMessagesRow())
        adapter.add(LatestMessagesRow())
        adapter.add(LatestMessagesRow())


    }*/

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")
       ref.addListenerForSingleValueEvent(object : ValueEventListener{
           override fun onDataChange(snapshot: DataSnapshot) {
               currentUser = snapshot.getValue(User::class.java)

           }

           override fun onCancelled(error: DatabaseError) {

           }

       })
    }

    private fun verifyUserisLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(Intent(this,MainActivity::class.java))
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item?.itemId){
            R.id.menu_new_message ->{

                val intent = Intent(this,NewMessageactivity::class.java)
                startActivity(intent)

            }
            R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(Intent(this,MainActivity::class.java))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)

    }
}