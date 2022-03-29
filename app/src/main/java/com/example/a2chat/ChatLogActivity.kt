package com.example.a2chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()


    private lateinit var recyclerViewchatlog: RecyclerView
    private lateinit var enterTextChatlog:TextView
    private lateinit var sendmessageBtn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerViewchatlog = findViewById(R.id.recyclerView_chatLog)
        enterTextChatlog = findViewById(R.id.enter_message_chatLog)
        sendmessageBtn = findViewById(R.id.send_message_btn)

        recyclerViewchatlog.adapter = adapter

        //supportActionBar?.title = "Chat log"

       // val username = intent.getStringExtra(NewMessageactivity.USER_KEY)
       // supportActionBar?.title = username

        val user = intent.getParcelableExtra<User>(NewMessageactivity.USER_KEY)
        supportActionBar?.title = user?.username

      //setupDummyData()

        listenForMessagess()

        sendmessageBtn.setOnClickListener {
            Log.d(TAG,"Attempt to send a message...")

            performSendmessage()


        }

    }

    private fun listenForMessagess() {

        val FromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageactivity.USER_KEY)
        val ToId = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$FromId/$ToId")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null){

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser?:return
                        adapter.add(chatFromItem(chatMessage.text,currentUser ))
                    }else{
                        val touser = intent.getParcelableExtra<User>(NewMessageactivity.USER_KEY)
                        adapter.add(chatToItem(chatMessage.text,touser!!))

                    }

                    recyclerViewchatlog.scrollToPosition(adapter.itemCount -1)



                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    class ChatMessage(val id:String,val text:String,val fromId:String,val toId:String,
                      val timeStamp:Long){
        constructor():this(" "," ","","",-1)

    }

    private fun performSendmessage() {
        val FromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageactivity.USER_KEY)
        val ToId = user?.uid

        if (FromId == null) return

            // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$FromId/$ToId").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$ToId/$FromId").push()

        val text = enterTextChatlog.text.toString()
        val chatMessage = ChatMessage(reference.key!!,text,FromId,ToId!!,System.currentTimeMillis()/10000)
        reference.setValue(chatMessage)

        toReference.setValue(chatMessage)
            .addOnSuccessListener {
                enterTextChatlog.setText("")
                recyclerViewchatlog.scrollToPosition(adapter.itemCount-1)
            }

        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$FromId/$ToId")
        latestMessagesRef.setValue(chatMessage)

        val latestMessagesToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$ToId/$FromId")
        latestMessagesToRef.setValue(chatMessage)


    }

   /* private fun setupDummyData() {
        val adapter = GroupAdapter<ViewHolder>()

        adapter.add(chatFromItem("This is from message"))
        adapter.add(chatToItem("This is to message\n to message"))


        recyclerViewchatlog.adapter = adapter
    }*/
}
class chatFromItem(val text:String,val user:User):Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_chat_from
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class chatToItem(val text:String,val user:User):Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text

        //Loading the user image into the chatlog activity.

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_chat_to_row
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}