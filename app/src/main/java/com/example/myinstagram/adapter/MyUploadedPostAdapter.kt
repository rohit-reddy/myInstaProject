package com.example.myinstagram.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myinstagram.R
import com.example.myinstagram.fragments.PostDetailsFragment
import com.example.myinstagram.model.Post
import com.squareup.picasso.Picasso

class MyUploadedPostAdapter(private var mContext: Context, private var mPostList: List<Post>) :
    RecyclerView.Adapter<MyUploadedPostAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.my_uploaded_posts_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPostList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPostList[position]

        Picasso.get().load(post.getPostimage()).placeholder(R.drawable.profile).into(holder.uploadedImage)

        holder.uploadedImage.setOnClickListener {
            val fragment = PostDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("postID", post.getPostid())
                }
            }
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var uploadedImage : ImageView

        init {
            uploadedImage = itemView.findViewById(R.id.uploaded_post_image)
        }
    }
}