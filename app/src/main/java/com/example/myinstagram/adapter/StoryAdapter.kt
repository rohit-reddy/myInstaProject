package com.example.myinstagram.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.myinstagram.AddStoryActivity
import com.example.myinstagram.MainActivity
import com.example.myinstagram.R
import com.example.myinstagram.StoryActivity
import com.example.myinstagram.model.Story
import com.example.myinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.view.*

class StoryAdapter(private val mContext: Context, private val mStoryList: List<Story>) :
    RecyclerView.Adapter<StoryAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryAdapter.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false)
            ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(mContext).inflate(R.layout.story_items_layout, parent, false)
            ViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return mStoryList.size
    }

    override fun onBindViewHolder(holder: StoryAdapter.ViewHolder, position: Int) {
        val story = mStoryList[position]

        userInfo(holder, story.getUserId(), position)

        if (holder.adapterPosition !== 0){
            seenStory(holder, story.getUserId())
        }
        if (holder.adapterPosition === 0){
            myStories(holder.addstory_text!!, holder.story_plus_btn!!, false)

        }

        holder.itemView.setOnClickListener {
           if (holder.adapterPosition == 0){
               myStories(holder.addstory_text!!, holder.story_plus_btn!!, true)

           }else{
               mContext.startActivity(
                   Intent(mContext, StoryActivity::class.java)
                       .putExtra("userId", story.getUserId())
               )
           }
        }
    }


    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 0
        }
        return 1
    }

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        //story item
        var story_image_seen: CircleImageView? = null
        var story_image: CircleImageView? = null
        var story_username: TextView? = null

        //add story item
        var story_plus_btn: CircleImageView? = null
        var addstory_text: TextView? = null

        init {
            //story item
            story_image_seen = itemView.findViewById(R.id.story_image_seen)
            story_image = itemView.findViewById(R.id.story_image)
            story_username = itemView.findViewById(R.id.story_username)

            //add story item
            story_plus_btn = itemView.findViewById(R.id.story_add)
            addstory_text = itemView.findViewById(R.id.add_story_text)
        }

    }

    private fun userInfo(viewHolder: ViewHolder, userId: String, position: Int) {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId)

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(viewHolder.story_image)

                    if (position != 0) {
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                            .into(viewHolder.story_image_seen)

                        viewHolder.story_username!!.text = user.getUserName()
                    }

                }
            }
        })
    }

    private fun seenStory(viewHolder: ViewHolder, userId: String) {
        val storyRef =
            FirebaseDatabase.getInstance().reference
                .child("Story").child(userId)

        storyRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var i = 0
                for (snapShot in p0.children) {
                    if (!snapShot.child("views").child(FirebaseAuth.getInstance().currentUser!!.uid).exists()
                        && System.currentTimeMillis() < snapShot.getValue(Story::class.java)!!.getTimeEnd()
                    ) {
                        i++
                    }

                    if (i > 0) {
                        viewHolder.story_image!!.visibility = View.VISIBLE
                        viewHolder.story_image_seen!!.visibility = View.GONE
                    } else {
                        viewHolder.story_image!!.visibility = View.GONE
                        viewHolder.story_image_seen!!.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun myStories(textView: TextView, imageView: ImageView, click: Boolean) {
        val storyRef =
            FirebaseDatabase.getInstance().reference
                .child("Story").child(FirebaseAuth.getInstance().currentUser!!.uid)

        storyRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()
                var countStory = 0
                var story: Story? = null

                for (snapShot in p0.children) {
                    story = snapShot.getValue(Story::class.java)

                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story!!.getTimeEnd()) {
                        countStory++
                    }
                }

                if (click) {
                    if (countStory > 0) {
                        val alertDialog = AlertDialog.Builder(mContext).create()
                        alertDialog.setButton(
                            AlertDialog.BUTTON_NEUTRAL,
                            "View Story"
                        ) { dialog: DialogInterface?, which: Int ->
                            mContext.startActivity(
                                Intent(mContext, StoryActivity::class.java)
                                    .putExtra(
                                        "userId",
                                        FirebaseAuth.getInstance().currentUser!!.uid
                                    )
                            )
                            dialog!!.dismiss()
                        }

                        alertDialog.setButton(
                            AlertDialog.BUTTON_POSITIVE,
                            "Add Story"
                        ) { dialog: DialogInterface?, which: Int ->
                            mContext.startActivity(
                                Intent(mContext, AddStoryActivity::class.java)
                                    .putExtra(
                                        "userId",
                                        FirebaseAuth.getInstance().currentUser!!.uid
                                    )
                            )
                            dialog!!.dismiss()
                        }
                        alertDialog.show()
                    }else{
                        mContext.startActivity(
                            Intent(mContext, AddStoryActivity::class.java)
                                .putExtra(
                                    "userId",
                                    FirebaseAuth.getInstance().currentUser!!.uid
                                )
                        )
                    }
                }else {
                    if (countStory > 0){
                        textView.text = "My Story"
                        imageView.visibility  = View.GONE
                    }else{
                        textView.text = "Add Story"
                        imageView.visibility  = View.VISIBLE
                    }
                }
            }
        })
    }
}