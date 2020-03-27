package com.example.myinstagram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_in_button.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        signup_button.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = fullname_signup.text.toString()
        val userName = username_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()

        when {
            TextUtils.isEmpty(fullName) -> Toast.makeText(
                this,
                "Full name required",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(
                this,
                "user name required",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(email) -> Toast.makeText(
                this,
                "email required",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(password) -> Toast.makeText(
                this,
                "password required",
                Toast.LENGTH_LONG
            ).show()

            else -> {
                progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("Sign Up")
                progressDialog.setCancelable(false)
                progressDialog.setMessage("Please wait..")
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            saveUserInfo(fullName, userName, email, password)
                        } else {
                            val message = it.exception.toString()
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                            progressDialog.dismiss()
                            mAuth.signOut()
                        }
                    }
            }

        }

    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, password : String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["password"] = password
        userMap["bio"] = "Hey I am new to this app"
        userMap["image"] =
            "https://firebasestorage.googleapis.com/v0/b/myinstagram-c7c62.appspot.com/o/Default%20images%2Fprofile.png?alt=media&token=f7c91491-ac30-4137-aede-33bd0b933d5e"


        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_LONG).show()

                    val followingRef =
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(currentUserId)
                            .child("Following").child(currentUserId)
                            .setValue(true)

                    startActivity(
                        Intent(this@SignUpActivity, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    finish()
                } else {
                    val message = it.exception.toString()
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    progressDialog.dismiss()
                    FirebaseAuth.getInstance().signOut()
                }
            }

    }
}
