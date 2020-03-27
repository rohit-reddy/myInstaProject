package com.example.myinstagram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        sign_up_button.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        login_button.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = email_login.text.toString()
        val password = password_login.text.toString()

        when {
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
                progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("Sign Up")
                progressDialog.setCancelable(false)
                progressDialog.setMessage("Please wait..")
                progressDialog.show()

                val mAuth = FirebaseAuth.getInstance()
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            progressDialog.dismiss()
                            startActivity(
                                Intent(this@SignInActivity, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            finish()
                        }else{
                            val message = it.exception.toString()
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                            progressDialog.dismiss()
                            FirebaseAuth.getInstance().signOut()
                        }
                    }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(
                Intent(this@SignInActivity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }
    }
}
