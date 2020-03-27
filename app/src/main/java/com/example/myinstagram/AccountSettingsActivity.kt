package com.example.myinstagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.myinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask


class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        logout_profile_frag.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            startActivity(
                Intent(this@AccountSettingsActivity, SignInActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )

            finish()
        }

        change_image_text_btn.setOnClickListener {
            checker = "clicked"

            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@AccountSettingsActivity)
        }

        save_info_profile_btn.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    private fun uploadImageAndUpdateInfo() {


        when {
            TextUtils.isEmpty(full_name_profile_frag.text) -> Toast.makeText(
                this,
                "Fullname cannot be empty",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(username_profile_frag.text) -> Toast.makeText(
                this,
                "Username cannot be empty",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(bio_porfile_frag.text) -> Toast.makeText(
                this,
                "bio cannot be empty",
                Toast.LENGTH_LONG
            ).show()
            imageUri == null -> Toast.makeText(
                this,
                "image should be selected",
                Toast.LENGTH_LONG
            ).show()

            else -> {
                var progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Updating..please wait")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it

                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(
                    OnCompleteListener<Uri> { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result
                            myUrl = downloadUrl.toString()

                            val ref = FirebaseDatabase.getInstance().reference.child("Users")

                            val userMap = HashMap<String, Any>()
                            userMap["fullname"] =
                                full_name_profile_frag.text.toString().toLowerCase()
                            userMap["username"] =
                                username_profile_frag.text.toString().toLowerCase()
                            userMap["bio"] = bio_porfile_frag.text.toString().toLowerCase()
                            userMap["image"] = myUrl

                            ref.child(firebaseUser.uid).updateChildren(userMap)

                            Toast.makeText(
                                this,
                                "Account info updated successfully",
                                Toast.LENGTH_LONG
                            ).show()

                            startActivity(
                                Intent(this@AccountSettingsActivity, MainActivity::class.java)
                            )
                            finish()
                            progressDialog.dismiss()
                        } else {
                            progressDialog.dismiss()
                        }
                    })
            }
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                imageUri = result.uri
                profile_image_view_profile_frag.setImageURI(imageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun updateUserInfoOnly() {
        val usersRef =
            FirebaseDatabase.getInstance().getReference().child("Users")

        if (!TextUtils.isEmpty(full_name_profile_frag.text) || !TextUtils.isEmpty(
                username_profile_frag.text
            ) || !TextUtils.isEmpty(bio_porfile_frag.text)
        ) {
            val userMap = HashMap<String, Any>()
            userMap["fullname"] = full_name_profile_frag.text.toString().toLowerCase()
            userMap["username"] = username_profile_frag.text.toString().toLowerCase()
            userMap["bio"] = bio_porfile_frag.text.toString().toLowerCase()

            usersRef.child(firebaseUser.uid).updateChildren(userMap)

            Toast.makeText(this, "Account info updated successfully", Toast.LENGTH_LONG).show()

            startActivity(
                Intent(this@AccountSettingsActivity, MainActivity::class.java)
            )
            finish()
        } else {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_LONG).show()
        }

    }

    private fun userInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_view_profile_frag)
                    full_name_profile_frag.setText(user.getFullName())
                    bio_porfile_frag.setText(user.getBio())
                    username_profile_frag.setText(user.getUserName())

                }
            }
        })
    }
}
