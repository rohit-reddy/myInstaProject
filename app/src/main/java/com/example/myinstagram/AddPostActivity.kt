package com.example.myinstagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null
    private lateinit var firebaseUser: FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        publish_post_btn.setOnClickListener {
            uploadImage()
        }

        CropImage.activity()
            .setAspectRatio(2, 1)
            .start(this@AddPostActivity)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                imageUri = result.uri
                image_post.setImageURI(imageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun uploadImage() {
        when{
            TextUtils.isEmpty(description_post.text) -> Toast.makeText(
                this,
                "description cannot be empty",
                Toast.LENGTH_LONG
            ).show()
            imageUri == null -> Toast.makeText(
                this,
                "image should be selected",
                Toast.LENGTH_LONG
            ).show()
            else ->{
                var progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("please wait..")
                progressDialog.show()

                val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
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

                            val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                            val postId = ref.push().key

                            val postMap = HashMap<String, Any>()
                            postMap["postid"] = postId!!
                            postMap["description"] =
                                description_post.text.toString().toLowerCase()
                            postMap["publisher"] = firebaseUser.uid
                            postMap["postimage"] = myUrl

                            ref.child(postId).updateChildren(postMap)

                            Toast.makeText(
                                this,
                                "Post uploaded successfully",
                                Toast.LENGTH_LONG
                            ).show()

                            startActivity(
                                Intent(this@AddPostActivity, MainActivity::class.java)
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
}
