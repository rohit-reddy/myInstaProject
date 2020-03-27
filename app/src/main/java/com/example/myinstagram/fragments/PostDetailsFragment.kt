package com.example.myinstagram.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.myinstagram.R
import com.example.myinstagram.adapter.PostAdapter
import com.example.myinstagram.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_post_details.view.*

/**
 * A simple [Fragment] subclass.
 */
class PostDetailsFragment : Fragment() {

    private var postAdapter : PostAdapter? = null
    private var postList : MutableList<Post>? = null
    private var postId : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_post_details, container, false)

        arguments?.getString("postID")?.let {
            postId = it
        }

        val linearLayoutManager = LinearLayoutManager(context)
        view.recyclerview_post_details.layoutManager = linearLayoutManager
        postList = ArrayList()
        postAdapter = context?.let {
            PostAdapter(it, postList as ArrayList<Post>)
        }
        view.recyclerview_post_details.adapter = postAdapter

        retrieveAllPosts()

        return view
    }

    private fun retrieveAllPosts() {
        val postsRef =
            FirebaseDatabase.getInstance().reference
                .child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()
                val post = p0.getValue(Post::class.java)
                postList!!.add(post!!)
                postAdapter!!.notifyDataSetChanged()

            }
        })

    }

}
