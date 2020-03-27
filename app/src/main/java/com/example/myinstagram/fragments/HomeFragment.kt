package com.example.myinstagram.fragments


import android.os.Bundle
import android.renderscript.Sampler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.myinstagram.R
import com.example.myinstagram.adapter.PostAdapter
import com.example.myinstagram.adapter.StoryAdapter
import com.example.myinstagram.model.Post
import com.example.myinstagram.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.view.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private var postAdapter : PostAdapter? = null
    private var postList : MutableList<Post>? = null
    private var followingList: MutableList<String>? = null
    //stories
    private var storyAdapter : StoryAdapter? = null
    private var storyList : MutableList<Story>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        view.recyclerview_home.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let {
            PostAdapter(it, postList as ArrayList<Post>)
        }
        view.recyclerview_home.adapter = postAdapter

        //stories
        val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        view.recyclerview_story.setHasFixedSize(true)
        view.recyclerview_story.layoutManager = linearLayoutManager2
        storyList = ArrayList()
        storyAdapter = context?.let {
            StoryAdapter(it, storyList as ArrayList<Story>)
        }
        view.recyclerview_story.adapter = storyAdapter

        checkFollowings()

        return view
    }

    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")

        followingRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    (followingList as ArrayList<String>).clear()

                    for (snapShot in p0.children){
                        snapShot.key?.let {
                            (followingList as ArrayList<String>).add(it)
                        }
                    }

                    retrieveAllPosts()
                    retrieveStories()
                }

            }
        })
    }

    private fun retrieveStories() {
        val storyRef =
            FirebaseDatabase.getInstance().reference
                .child("Story")

        storyRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                storyList?.clear()
                storyList?.add(Story("",0,0,"",FirebaseAuth.getInstance().currentUser!!.uid))

                for (id in followingList!!){
                    var countStory = 0
                    var story : Story? = null

                    for (snapShot in p0.child(id).children){
                        story = snapShot.getValue(Story::class.java)

                        if (timeCurrent > story!!.getTimeStart() && timeCurrent < story!!.getTimeEnd()){
                            countStory++
                        }
                    }

                    if (countStory > 0){
                        storyList?.add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }
        })
    }

    private fun retrieveAllPosts() {
        val postsRef =
            FirebaseDatabase.getInstance().reference
                .child("Posts")

        postsRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()

                for (snapShot in p0.children){
                    val post = snapShot.getValue(Post::class.java)

                    for (id in  followingList!!){
                        if (post!!.getPublisher() == id){
                            postList!!.add(post)
                        }

                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        })

    }


}
