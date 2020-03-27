package com.example.myinstagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddStoryActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageStoryPicRef: StorageReference? = null
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        storageStoryPicRef = FirebaseStorage.getInstance().reference.child("Story Pictures")
        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        CropImage.activity()
            .setAspectRatio(9, 16)
            .start(this@AddStoryActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                imageUri = result.uri
                uploadStory()
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun uploadStory() {
        when (imageUri) {
            null -> Toast.makeText(
                this,
                "image should be selected",
                Toast.LENGTH_LONG
            ).show()
            else -> {
                var progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Story")
                progressDialog.setMessage("please wait..")
                progressDialog.show()

                val fileRef = storageStoryPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
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

                            val ref = FirebaseDatabase.getInstance().reference.child("Story")
                                .child(firebaseUser.uid)
                            val storyId = (ref.push().key).toString()

                            val timeEnd = System.currentTimeMillis() + 86400000 //one day 24 hr

                            val storyMap = HashMap<String, Any>()
                            storyMap["userid"] = firebaseUser.uid
                            storyMap["timestart"] = ServerValue.TIMESTAMP
                            storyMap["timeend"] = timeEnd
                            storyMap["imageurl"] = myUrl
                            storyMap["storyid"] = storyId

                            ref.child(storyId).updateChildren(storyMap)

                            Toast.makeText(
                                this,
                                "Story uploaded successfully",
                                Toast.LENGTH_LONG
                            ).show()

                            startActivity(
                                Intent(this@AddStoryActivity, MainActivity::class.java)
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
