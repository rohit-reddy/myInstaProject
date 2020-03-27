package com.example.myinstagram.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.myinstagram.R
import com.example.myinstagram.adapter.NotificationAdapter
import com.example.myinstagram.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_notifications.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class NotificationsFragment : Fragment() {

    private var notificationList: List<Notification>? = null
    private var notificationAdapter: NotificationAdapter? = null
    private lateinit var firebaseUser: FirebaseUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_notifications, container, false)

        view.recyclerview_notifications.setHasFixedSize(true)
        view.recyclerview_notifications.layoutManager = LinearLayoutManager(context)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        notificationList = ArrayList()
        notificationAdapter = NotificationAdapter(context!!, notificationList as ArrayList)

        view.recyclerview_notifications.adapter = notificationAdapter

        readNotifications()

        return view
    }

    private fun readNotifications() {
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(firebaseUser!!.uid)

        notiRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    (notificationList as ArrayList).clear()
                    for (snapShot in p0.children){
                        val notification = snapShot.getValue(Notification::class.java)
                        (notificationList as ArrayList<Notification>).add(notification!!)
                    }
                    Collections.reverse(notificationList)
                    notificationAdapter!!.notifyDataSetChanged()
                }

            }
        })
    }


}
