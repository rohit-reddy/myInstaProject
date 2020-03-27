package com.example.myinstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myinstagram.adapter.UserAdapter
import com.example.myinstagram.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_show_users.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*

class ShowUsersActivity : AppCompatActivity() {

    var id: String = ""
    var title: String = ""
    var userAdapter: UserAdapter? = null
    var userList: List<User>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        intent.apply {
            id = getStringExtra("id")
            title = getStringExtra("title")
        }

        setSupportActionBar(show_users_toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        show_users_toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerview_show_users.setHasFixedSize(true)
        recyclerview_show_users.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerview_show_users.adapter = userAdapter

        idList = ArrayList()

        when(title){
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getViews() {
        val ref =
            FirebaseDatabase.getInstance().reference
                .child("Story").child(id)
                .child(intent.getStringExtra("storyid"))
                .child("views")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snapShot in dataSnapshot.children){
                        (idList as ArrayList<String>).add(snapShot.key!!)
                    }

                    fetchUsers()
                }
            }
        })
    }

    private fun getFollowers() {

        val followersRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(id)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snapShot in dataSnapshot.children){
                        (idList as ArrayList<String>).add(snapShot.key!!)
                    }

                    fetchUsers()
                }
            }
        })
    }

    private fun getFollowing() {
        val followingsRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(id)
                .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (snapShot in dataSnapshot.children){
                        (idList as ArrayList<String>).add(snapShot.key!!)
                    }

                    fetchUsers()
                }
            }
        })
    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(id)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){

                    (idList as ArrayList<String>).clear()
                    for (snapShot in p0.children){
                        (idList as ArrayList<String>).add(snapShot.key!!)
                    }

                    fetchUsers()
                }
            }
        })
    }

    private fun fetchUsers() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList).clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    for (id in idList!!){
                        if (user!!.getUid() == id){
                            (userList as ArrayList).add(user)
                        }
                    }
                }

                userAdapter?.notifyDataSetChanged()
            }

        })
    }
}
