package com.example.myinstagram.adapter

import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myinstagram.CommentsActivity
import com.example.myinstagram.MainActivity
import com.example.myinstagram.R
import com.example.myinstagram.ShowUsersActivity
import com.example.myinstagram.fragments.PostDetailsFragment
import com.example.myinstagram.fragments.ProfileFragment
import com.example.myinstagram.model.Post
import com.example.myinstagram.model.User
import com.google.android.gms.dynamic.IFragmentWrapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class PostAdapter(private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        if (TextUtils.isEmpty(post.getDescription())) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.getDescription()
        }

        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())
        isLikes(post.getPostid(), holder.likeButton)
        numberOfLikes(holder.likes, post.getPostid())
        getTotalComments(holder.comments, post.getPostid())
        checkSavedStatus(post.getPostid(), holder.saveButton)

        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag == "Like") {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .setValue(true)

                addNotifications(post.getPublisher(), post.getPostid())
            } else {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .removeValue()

            }
            notifyItemChanged(position)
        }

        holder.profileImage.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", post.getPublisher())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.postImage.setOnClickListener {
            val fragment = PostDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("postID", post.getPostid())
                }
            }
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

        holder.publisher.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            pref.putString("profileId", post.getPublisher())
            pref.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }



        holder.commentButton.setOnClickListener {
            var commentIntent = Intent(mContext, CommentsActivity::class.java)
            commentIntent.putExtra("postId", post.getPostid())
            commentIntent.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(commentIntent)
        }

        holder.comments.setOnClickListener {
            var commentIntent = Intent(mContext, CommentsActivity::class.java)
            commentIntent.putExtra("postId", post.getPostid())
            commentIntent.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(commentIntent)
        }

        holder.saveButton.setOnClickListener {
            if (holder.saveButton.tag == "Save") {

                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid).child(post.getPostid())
                    .setValue(true)

            } else {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid).child(post.getPostid())
                    .removeValue()

            }
        }

        holder.likes.setOnClickListener {
            mContext.startActivity(
                Intent(mContext, ShowUsersActivity::class.java)
                    .putExtra("id", post.getPostid())
                    .putExtra("title", "likes")
            )
        }


    }

    private fun numberOfLikes(likes: TextView, postid: String) {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postid)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    likes.text = p0.childrenCount.toString() + " likes"
                } else {

                }
            }
        })
    }

    private fun getTotalComments(comments: TextView, postid: String) {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postid)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    comments.text = "view all " + p0.childrenCount.toString() + " comments"
                } else {

                }
            }
        })
    }

    private fun isLikes(postid: String, likeButton: ImageView) {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postid)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(firebaseUser!!.uid).exists()) {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                } else {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }
        })
    }


    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var userName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            userName = itemView.findViewById(R.id.user_name_post)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)

        }
    }

    private fun publisherInfo(
        profileImage: CircleImageView,
        userName: TextView,
        publisher: TextView,
        publisherID: String
    ) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    publisher.setText(user.getFullName())
                    userName.setText(user.getUserName())
                }

            }
        })
    }

    private fun checkSavedStatus(postid: String, imageView: ImageView) {
        val savesRef = FirebaseDatabase.getInstance().reference.child("Saves")
            .child(firebaseUser!!.uid)

        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }
        })
    }

    private fun addNotifications(userId: String, postid: String) {
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(userId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "liked your post"
        notiMap["postid"] = postid
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)

    }
}