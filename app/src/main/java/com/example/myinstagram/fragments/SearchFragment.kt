package com.example.myinstagram.fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myinstagram.R
import com.example.myinstagram.adapter.UserAdapter
import com.example.myinstagram.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.view.*

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {

    private var mRecyclerView: RecyclerView? = null
    private var mUserAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        mRecyclerView = view.findViewById(R.id.recyclerview_search)
        mRecyclerView?.setHasFixedSize(true)
        mRecyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()

        mUserAdapter = context?.let {
            UserAdapter(it, mUser as ArrayList, true)
        }

        mRecyclerView?.adapter = mUserAdapter

        view.search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (view.search_edit_text.text.toString() == "") {

                } else {
                    mRecyclerView?.visibility = View.VISIBLE
                    retrieveUser()
                    searchUser(s.toString().toLowerCase())
                }
            }

        })

        return view
    }

    private fun retrieveUser() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (view?.search_edit_text?.text.toString() == "") {

                    mUser?.clear()
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            mUser?.add(user)
                        }
                    }

                    mUserAdapter?.notifyDataSetChanged()
                }
            }

        })
    }

    private fun searchUser(input: String) {
        val query = FirebaseDatabase.getInstance().getReference().child("Users")
            .orderByChild("fullname")
            .startAt(input).endAt(input + "\uf8ff")


        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUser?.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        mUser?.add(user)
                    }
                }

                mUserAdapter?.notifyDataSetChanged()
            }

        })
    }


}
