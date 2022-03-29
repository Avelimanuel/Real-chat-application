package com.example.a2chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.parcel.Parcelize
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var userName:TextView
    private lateinit var Email:TextView
    private lateinit var Password:TextView
    private lateinit var reg_btn:Button
    private lateinit var reg_login_btn:Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var profile_pic_btn:Button
    private lateinit var profile_imageview:ImageView
    private lateinit var circularImageView:ImageView
    private lateinit var database: FirebaseDatabase





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userName = findViewById(R.id.register_username)
        Email = findViewById(R.id.register_email)
        Password = findViewById(R.id.register_password)
        reg_btn = findViewById(R.id.register_button)
        profile_pic_btn = findViewById(R.id.select_photo_button)
        reg_login_btn = findViewById(R.id.regiter_login_button)
        profile_imageview = findViewById(R.id.profile_pic_imageView)
        circularImageView = findViewById(R.id.circular_image_view)

        firebaseAuth = FirebaseAuth.getInstance()




       reg_btn.setOnClickListener {
           performregistration()
           //uploadProfilePicture()
       }

        reg_login_btn.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        profile_pic_btn.setOnClickListener {
            selectPhoto()
        }


    }

  /*  private fun uploadProfilePicture() {
        if (selectedPhotouri != null){
            var progress = ProgressDialog(this)
            progress.setTitle("Registering user...please wait")
            progress.show()

            var imageRef: StorageReference = FirebaseStorage.getInstance().reference.child("images/profilepic.jpg")
            imageRef.putFile(selectedPhotouri!!)
                .addOnSuccessListener { p0 ->
                    progress.dismiss()
                    Toast.makeText(this,"Image uploaded successful",Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{ p0 ->
                    Toast.makeText(this,"${p0.message}",Toast.LENGTH_SHORT).show()

                }
                .addOnProgressListener { p0 ->
                    var prog:Double = (100.0 * p0.bytesTransferred)/p0.totalByteCount
                    progress.setMessage("Uploaded ${progress.toInt()}%")

                }
        }

    }*/

    private fun selectPhoto() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,30)

       // var intent = Intent()
        //intent.setType("image/*")
       // intent.setAction(Intent.ACTION_PICK)
       // startActivityForResult(Intent.createChooser(intent,"select Photo"),30)

    }

    var selectedPhotouri:Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 30 && resultCode == Activity.RESULT_OK && data != null){

            selectedPhotouri = data.data!!
            var bitmap:Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotouri)
            circularImageView.setImageBitmap(bitmap)
            profile_pic_btn.alpha = 0f
           // val bitMapdrawable = BitmapDrawable(bitmap)
            //profile_imageview.setImageBitmap(bitmap)
            //profile_imageview.setBackgroundDrawable(bitMapdrawable)


        }
    }

    private fun performregistration() {
        val mail = Email.text.toString().trim()
        val password = Password.text.toString().trim()

        if (mail.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please enter email and password",Toast.LENGTH_SHORT).show()
            return
        }else{
            Log.d("MainActivity","Email is: $mail")
            Log.d("MainActivity","Password is: $password")
        }

        firebaseAuth.createUserWithEmailAndPassword(mail,password)
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    Toast.makeText(this,"User registration failed",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this,"Registration successfull",Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity","Succesfully created user with uid:${it.result.user?.uid}")
                }

                uploadImageTofirebase()


            }
            .addOnFailureListener {
                //Toast.makeText(this,"User registration failled",Toast.LENGTH_SHORT).show()
                Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show()
            }

    }

    private fun uploadImageTofirebase() {

        if (selectedPhotouri == null) return


        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")


        ref.putFile(selectedPhotouri!!)
            .addOnSuccessListener { it ->
                Log.d("MainActivity","Successfuly uploaded image to ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("MainActivity","file location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{

            }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl:String) {
        val uid = FirebaseAuth.getInstance().uid?:" "
        val ref = FirebaseDatabase.getInstance().getReference("/Users/$uid")

        val user = User(uid,userName.text.toString().trim(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity","Finally user saved to firebase database.")

                val intent = Intent(Intent(this,LatestMessagesActivity::class.java))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)



            }
            .addOnFailureListener {
                Log.d("MainActivity","Failled to save user to firebase database")
            }
    }

}

@Parcelize
class User(val uid:String,val username:String,val profileImageUrl:String):Parcelable{
    constructor() : this("","","")
}



