package com.example.myinstagram.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.myinstagram.R
import com.example.myinstagram.model.Comment
import com.example.myinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private var mContext : Context, private var mCommentList: MutableList<Comment>)
    : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private val firebaseUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comments_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mCommentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = mCommentList[position]

        holder.userComment.text = comment.getComment()
        getUserInfo(holder.userNameComment, holder.imageProfile, comment.getPublisher())

    }

    private fun getUserInfo(userNameComment: TextView, imageProfile: CircleImageView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(publisher)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user = p0.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageProfile)
                    userNameComment.text = user.getUserName()
                }
            }
        })
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageProfile : CircleImageView
        var userNameComment : TextView
        var userComment : TextView

        init {
            imageProfile = itemView.findViewById(R.id.user_profile_image_comment)
            userNameComment = itemView.findViewById(R.id.user_name_comment)
            userComment = itemView.findViewById(R.id.user_comment)
        }
    }
}