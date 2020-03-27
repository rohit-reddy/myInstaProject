package com.example.myinstagram.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myinstagram.MainActivity
import com.example.myinstagram.R
import com.example.myinstagram.fragments.ProfileFragment
import com.example.myinstagram.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(private var mContext : Context,
    private var mUser : List<User>,
                  private var isFragment: Boolean = false) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userNameTextview.setText(user.getUserName())
        holder.fullNameTextview.setText(user.getFullName())
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userImageSearch)

        checkFollowingStatus(user.getUid(), holder.userFollowSearch)

        holder.itemView.setOnClickListener {
           if (isFragment){
               val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
               pref.putString("profileId", user.getUid())
               pref.apply()

               (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                   .replace(R.id.fragment_container, ProfileFragment()).commit()
           }else{
               mContext.startActivity(
                   Intent(mContext, MainActivity::class.java)
                       .putExtra("publisherId", user.getUid())
               )
           }
        }

        holder.userFollowSearch.setOnClickListener {
            if(holder.userFollowSearch.text.toString() == "Follow"){
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener {task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
                addNotifications(user.getUid())
            }else{
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener {task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }

    }


    class ViewHolder(@NonNull itemView : View) : RecyclerView.ViewHolder(itemView){
        var userNameTextview : TextView = itemView.findViewById(R.id.user_name_search)
        var fullNameTextview : TextView = itemView.findViewById(R.id.user_fullname_search)
        var userImageSearch : ImageView = itemView.findViewById(R.id.user_profile_image_search)
        var userFollowSearch : Button = itemView.findViewById(R.id.follow_btn_search)

    }

    private fun checkFollowingStatus(uid: String, userFollowSearch: Button) {
        val followingRef = firebaseUser?.uid.let {
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object  : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(uid).exists()){
                    userFollowSearch.text = "Following"
                }else{
                    userFollowSearch.text = "Follow"
                }
            }
        })


    }

    private fun addNotifications(userId: String){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(userId)

        val notiMap = HashMap<String, Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)

    }

}