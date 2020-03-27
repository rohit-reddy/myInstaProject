package com.example.myinstagram.adapter

import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myinstagram.R
import com.example.myinstagram.fragments.PostDetailsFragment
import com.example.myinstagram.fragments.ProfileFragment
import com.example.myinstagram.model.Notification
import com.example.myinstagram.model.Post
import com.example.myinstagram.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class NotificationAdapter(private var mContext: Context, private var mNotificationList: List<Notification>)
    :RecyclerView.Adapter<NotificationAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotificationList.size
    }

    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
        val notification = mNotificationList[position]
        userInfo(holder.profileImage, holder.userName, notification.getUserId())

        if (notification.getIsPost()){
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.getPostId())
        }else{
            holder.postImage.visibility = View.GONE
        }

        if (notification.getText().equals("started following you")){
            holder.text.text = "started following you"
        }else if(notification.getText().equals("liked your post")){
            holder.text.text = "liked your post"
        }else if(notification.getText().contains("Commented:")){
            holder.text.text = notification.getText()
        }else{
            holder.text.text = notification.getText()
        }

        holder.itemView.setOnClickListener {
            if (notification.getIsPost()){
                val fragment = PostDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putString("postID", notification.getPostId())
                    }
                }
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            }else{
                val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", notification.getUserId())
                pref.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }


    }

    class ViewHolder(@NonNull itemvView: View) : RecyclerView.ViewHolder(itemvView) {
        var postImage : ImageView
        var profileImage : CircleImageView
        var userName : TextView
        var text : TextView

        init {
            profileImage = itemView.findViewById(R.id.notifications_profile_image)
            userName = itemView.findViewById(R.id.notifications_username)
            text = itemView.findViewById(R.id.notifications_comment)
            postImage = itemvView.findViewById(R.id.notifications_post_image)
        }
    }

    private fun userInfo(imageView: ImageView, userName: TextView, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(imageView)
                    userName.text = user.getFullName()

                }
            }
        })
    }

    private fun getPostImage(imageView: ImageView, postId: String) {
        val postRef =
            FirebaseDatabase.getInstance().getReference().child("Posts").child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val post = dataSnapshot.getValue<Post>(Post::class.java)

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.profile)
                        .into(imageView)


                }
            }
        })
    }

}