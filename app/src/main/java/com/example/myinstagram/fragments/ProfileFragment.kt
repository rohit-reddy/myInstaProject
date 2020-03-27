package com.example.myinstagram.fragments


import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myinstagram.AccountSettingsActivity
import com.example.myinstagram.R
import com.example.myinstagram.ShowUsersActivity
import com.example.myinstagram.adapter.MyUploadedPostAdapter
import com.example.myinstagram.model.Post
import com.example.myinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    private var postList: List<Post>? = null
    private var myUploadedPostAdapter: MyUploadedPostAdapter? = null
    private var mySavedPostAdapter: MyUploadedPostAdapter? = null
    private var postListSaved: List<Post>? = null
    private var mySavedImages: List<String>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        if (profileId == firebaseUser.uid) {
            view.edit_account_settings_btn.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        //recyclerview for uploaded images
        postList = ArrayList()
        val layoutManager = GridLayoutManager(context, 3)
        view.recyclerview_uploaded_pic.layoutManager = layoutManager
        myUploadedPostAdapter = context?.let {
            MyUploadedPostAdapter(it, postList as ArrayList)
        }
        view.recyclerview_uploaded_pic.adapter = myUploadedPostAdapter

        //recyclerview for saved images
        postListSaved = ArrayList()
        val layoutManager1 = GridLayoutManager(context, 3)
        view.recyclerview_saved_pic.layoutManager = layoutManager1
        mySavedPostAdapter = context?.let {
            MyUploadedPostAdapter(it, postListSaved as ArrayList)
        }
        view.recyclerview_saved_pic.adapter = mySavedPostAdapter


        view.images_grid_btn.setOnClickListener {
            view.recyclerview_saved_pic.visibility = View.GONE
            view.recyclerview_uploaded_pic.visibility = View.VISIBLE
        }

        view.images_save_btn.setOnClickListener {
            view.recyclerview_saved_pic.visibility = View.VISIBLE
            view.recyclerview_uploaded_pic.visibility = View.GONE
        }


        view.total_following.setOnClickListener {
            startActivity(
                Intent(context, ShowUsersActivity::class.java)
                    .putExtra("id", profileId)
                    .putExtra("title", "following")
            )
        }

        view.total_followers.setOnClickListener {
            startActivity(
                Intent(context, ShowUsersActivity::class.java)
                    .putExtra("id", profileId)
                    .putExtra("title", "followers")
            )
        }




        view.edit_account_settings_btn.setOnClickListener {
            val getEditText = view.edit_account_settings_btn.text.toString()

            when {
                getEditText == "Edit Profile" -> startActivity(
                    Intent(
                        context,
                        AccountSettingsActivity::class.java
                    )
                )

                getEditText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).setValue(true)
                    }


                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }
                    addNotifications()

                }

                getEditText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).removeValue()
                    }


                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }


                }
            }
        }



        getFollowers()
        getFollowings()
        userInfo()
        retrieveUploadedPosts()
        getTotalNumberOfPosts()
        mySavedImage()

        return view
    }

    private fun mySavedImage() {
        mySavedImages = ArrayList()

        val savedRef = FirebaseDatabase.getInstance().reference
            .child("Saves").child(firebaseUser!!.uid)

        savedRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (snapShot in p0.children) {
                        (mySavedImages as ArrayList<String>).add(snapShot.key!!)
                    }
                    readSavedImagesData()
                }
            }
        })
    }

    private fun readSavedImagesData() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postListSaved as ArrayList).clear()
                    for (snapShot in p0.children) {
                        val post = snapShot.getValue(Post::class.java)
                        for (id in mySavedImages!!) {
                            if (post!!.getPostid() == id) {
                                (postListSaved as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    mySavedPostAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    private fun retrieveUploadedPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference
            .child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (postList as ArrayList).clear()

                    for (snapShot in p0.children) {
                        val post = snapShot.getValue(Post::class.java)
                        if (post!!.getPublisher().equals(profileId)) {
                            (postList as java.util.ArrayList<Post>).add(post)
                        }
                    }
                    Collections.reverse(postList)
                    myUploadedPostAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")


        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(profileId).exists()) {
                    view?.edit_account_settings_btn?.text = "Following"
                } else {
                    view?.edit_account_settings_btn?.text = "Follow"
                }
            }
        })

    }

    private fun getFollowers() {
        val followersRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    view?.total_followers?.text = dataSnapshot.childrenCount.toString()
                }
            }
        })
    }


    private fun getFollowings() {
        val followingsRef = firebaseUser?.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
                .child("Following")
        }

        followingsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    view?.total_following?.text = dataSnapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(view?.profile_image_fragment)
                    view?.full_name_profile_frag?.text = user.getFullName()
                    view?.bio_porfile_frag?.text = user.getBio()
                    view?.profile_frag_username?.text = user.getUserName()

                }
            }
        })
    }


    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    private fun getTotalNumberOfPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    var postCounter = 0
                    for (snapShot in p0.children) {
                        val post = snapShot.getValue(Post::class.java)
                        if (profileId.equals(post!!.getPublisher())) {
                            postCounter++
                        }
                    }
                    view?.total_posts!!.text = "$postCounter"
                }
            }
        })
    }


    private fun addNotifications() {
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(profileId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)

    }

}
