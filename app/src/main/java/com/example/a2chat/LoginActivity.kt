package com.example.a2chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {


    private lateinit var login_email: TextView
    private lateinit var login_password: TextView
    private lateinit var back_to_reg: TextView
    private lateinit var login_btn: Button
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        back_to_reg = findViewById(R.id.back_to_register)
        login_btn = findViewById(R.id.login_button)
        login_email = findViewById(R.id.Login_email)
        login_password = findViewById(R.id.Login_password)
        firebaseAuth = FirebaseAuth.getInstance()


        login_btn.setOnClickListener {
            signInUser()
            //val intent = Intent(this,NewMessageactivity::class.java)
            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
           // startActivity(intent)
        }


        back_to_reg.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun signInUser() {
        val mail = login_email.text.toString().trim()
        val Password = login_password.text.toString().trim()

        if (mail.isEmpty() || Password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
            return
        } else {

            firebaseAuth.signInWithEmailAndPassword(mail, Password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(this, "Failed to login user", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "User login successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this,NewMessageactivity::class.java)
                        startActivity(intent)
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show()

                }

        }

    }
}