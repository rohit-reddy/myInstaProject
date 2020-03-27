package com.example.myinstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.TextureView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myinstagram.adapter.CommentsAdapter
import com.example.myinstagram.model.Comment
import com.example.myinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var commentsAdapter: CommentsAdapter? = null
    private var firebaseUser: FirebaseUser? = null
    private var commentList: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        intent.apply {
            postId = getStringExtra("postId")
            publisherId = getStringExtra("publisherId")
        }

        userInfo()
        fetchComments()
        getPostImage()

        val layoutManager = LinearLayoutManager(this@CommentsActivity)
        layoutManager.reverseLayout = true
        recyclerview_comments.layoutManager = layoutManager

        commentList = ArrayList()
        commentsAdapter = CommentsAdapter(this@CommentsActivity, commentList as ArrayList)
        recyclerview_comments.adapter = commentsAdapter

        post_comment.setOnClickListener {
            if (TextUtils.isEmpty(add_comment.text.toString())) {
                Toast.makeText(
                    this@CommentsActivity,
                    "Please enter the comments",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                addComment()
            }
        }

    }

    private fun addComment() {
        val commentsRef =
            FirebaseDatabase.getInstance().getReference().child("Comments").child(postId)

        var commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = add_comment.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)
        addNotifications()
        add_comment.text.clear()
    }

    private fun userInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser!!.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_comment)


                }
            }
        })
    }


    private fun getPostImage() {
        val postRef =
            FirebaseDatabase.getInstance().getReference().child("Posts").child(postId)
                .child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val image = dataSnapshot.getValue().toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile)
                        .into(post_image_comment)


                }
            }
        })
    }

    private fun fetchComments() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    commentList!!.clear()

                    for (snapShot in dataSnapshot.children) {
                        val comments = snapShot.getValue(Comment::class.java)
                        commentList!!.add(comments!!)
                    }

                    commentsAdapter!!.notifyDataSetChanged()
                }


            }
        })
    }

    private fun addNotifications(){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(publisherId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "Commented: ${add_comment.text}"
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)

    }
}
