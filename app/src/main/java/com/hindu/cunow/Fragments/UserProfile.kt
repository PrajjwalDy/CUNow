package com.hindu.cunow.Fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.cunow.Model.UserModel
import com.hindu.cunow.R
import kotlinx.android.synthetic.main.fragment_user_profiel.*
import kotlinx.android.synthetic.main.fragment_user_profiel.view.*
import kotlinx.android.synthetic.main.profile_fragment.*
import kotlinx.android.synthetic.main.profile_fragment.verification_profilePage
import kotlinx.android.synthetic.main.profile_fragment.view.*

class UserProfile : Fragment() {
    private lateinit var profileId:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root:View = inflater.inflate(R.layout.fragment_user_profiel, container, false)

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null){
            this.profileId = pref.getString("uid","none")!!
        }


        root.open_options_user.setOnClickListener {
            root.userProfile_LL.visibility = View.VISIBLE
            root.open_options_user.visibility = View.GONE
            root.close_options_user.visibility = View.VISIBLE
        }

        root.close_options_user.setOnClickListener {
            root.userProfile_LL.visibility = View.GONE
            root.open_options_user.visibility = View.VISIBLE
            root.close_options_user.visibility = View.GONE
        }

        userInfo()

        return root
    }


    private fun userInfo(){
        val progressDialog = context?.let { Dialog(it) }
        progressDialog!!.setContentView(R.layout.profile_dropdown_menu)
        progressDialog.show()
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val userData = snapshot.getValue(UserModel::class.java)
                    Glide.with(context!!).load(userData!!.profileImage).into(userProfileImage)
                    userName_profile.text = userData!!.fullName
                    user_bio_profile.text = userData.bio
                    if (userData.verification){
                        verification_UserPage.visibility = View.VISIBLE
                    }
                }
                progressDialog.dismiss()
            }
            override fun onCancelled(error: DatabaseError) {
                println("some error occurred")
            }
        })
    }


}