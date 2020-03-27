package com.example.myinstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.myinstagram.model.Story
import com.example.myinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {

    var currentUserid: String = ""
    var userId: String = ""
    var imageList: List<String>? = null
    var storyIdsList: List<String>? = null
    var counter = 0
    var pressTime = 0L
    var limit = 500L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserid = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId")

        if (userId == currentUserid){
            layout_seen.visibility = View.VISIBLE
            story_delete.visibility = View.VISIBLE
        }else{
            layout_seen.visibility = View.GONE
            story_delete.visibility = View.GONE
        }

        reverse.setOnClickListener { stories_progress.reverse() }
        reverse.setOnTouchListener(View.OnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_UP ->
                {
                    var now = System.currentTimeMillis()
                    stories_progress!!.resume()
                    return@OnTouchListener limit < now - pressTime
                }
                MotionEvent.ACTION_DOWN ->
                {
                    pressTime = System.currentTimeMillis()
                    stories_progress!!.pause()
                    return@OnTouchListener false
                }
            }
            false
        })

        //skip
        skip.setOnClickListener { stories_progress.skip() }
        skip.setOnTouchListener(View.OnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_UP ->
                {
                    var now = System.currentTimeMillis()
                    stories_progress!!.resume()
                    return@OnTouchListener limit < now - pressTime
                }
                MotionEvent.ACTION_DOWN ->
                {
                    pressTime = System.currentTimeMillis()
                    stories_progress!!.pause()
                    return@OnTouchListener false
                }
            }
            false
        })

        seen_number.setOnClickListener {  startActivity(
            Intent(this@StoryActivity, ShowUsersActivity::class.java)
                .putExtra("id", userId)
                .putExtra("storyid", storyIdsList!![counter])
                .putExtra("title", "views")
        ) }

        story_delete.setOnClickListener { val ref = FirebaseDatabase.getInstance().reference.child("Story")
            .child(userId)
            .child(storyIdsList!![counter])

        ref.removeValue().addOnCompleteListener {
            if (it.isSuccessful)
                Toast.makeText(this@StoryActivity, "Story deleted successfully", Toast.LENGTH_LONG).show()
        }}

        getStories(userId)
        userInfo(userId)
    }

    private fun seenNumber(storyId: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Story")
            .child(userId)
            .child(storyId)
            .child("views")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                seen_number.text = p0.childrenCount.toString()
            }
        })
    }

    private fun addViewToStory(storyId: String) {
        FirebaseDatabase.getInstance().reference.child("Story")
            .child(userId)
            .child(storyId)
            .child("views")
            .child(currentUserid)
            .setValue(true)

    }


    private fun userInfo(userId: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(story_profile_image)
                    story_username.text = user.getUserName()

                }
            }
        })
    }

    private fun getStories(userId: String) {
        imageList = ArrayList()
        storyIdsList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (imageList as ArrayList).clear()
                (storyIdsList as ArrayList).clear()

                for (snapShot in p0.children){
                    val story : Story? = snapShot.getValue(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()){
                        (imageList as ArrayList).add(story.getImageUrl())
                        (storyIdsList as ArrayList).add(story.getStoryId())
                    }
                }
                stories_progress.setStoriesCount((imageList as ArrayList).size)
                stories_progress.setStoryDuration(6000L)
                stories_progress.setStoriesListener(this@StoryActivity)
                stories_progress.startStories( counter)

                Picasso.get().load(imageList!!.get(counter)).placeholder(R.drawable.profile).into(image_story)
                addViewToStory(storyIdsList!!.get(counter))
                seenNumber(storyIdsList!!.get(counter))
            }
        })
    }


    override fun onComplete() {
        finish()
    }

    override fun onPrev() {
        if (counter - 1 < 0) return
        Picasso.get().load(imageList!![--counter]).placeholder(R.drawable.profile).into(image_story)
        seenNumber(storyIdsList!![counter])
    }

    override fun onNext() {
        Picasso.get().load(imageList!![++counter]).placeholder(R.drawable.profile).into(image_story)
        addViewToStory(storyIdsList!![counter])
        seenNumber(storyIdsList!![counter])
    }


    override fun onResume() {
        super.onResume()
        stories_progress.resume()
    }

    override fun onPause() {
        super.onPause()
        stories_progress.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stories_progress.destroy()
    }

}
