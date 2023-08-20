package com.hindu.joltt.Fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.cunow.R
import com.hindu.joltt.Activity.EditProfileActivity
import com.hindu.joltt.Activity.SettingActivity
import com.hindu.joltt.Adapter.MyPostAdapter
import com.hindu.joltt.Model.PostModel
import com.hindu.joltt.Model.UserModel
import kotlinx.android.synthetic.main.profile_fragment.editProfile_fab
import kotlinx.android.synthetic.main.profile_fragment.github_profile
import kotlinx.android.synthetic.main.profile_fragment.gitlink_profile
import kotlinx.android.synthetic.main.profile_fragment.instagramLink_profile
import kotlinx.android.synthetic.main.profile_fragment.instagram_profile
import kotlinx.android.synthetic.main.profile_fragment.linkedin_profile
import kotlinx.android.synthetic.main.profile_fragment.linkednLink_profile
import kotlinx.android.synthetic.main.profile_fragment.myPostLayout
import kotlinx.android.synthetic.main.profile_fragment.noPostLayout
import kotlinx.android.synthetic.main.profile_fragment.portfolioLink_profile
import kotlinx.android.synthetic.main.profile_fragment.portfolio_profile
import kotlinx.android.synthetic.main.profile_fragment.postCount_profile
import kotlinx.android.synthetic.main.profile_fragment.profileImage
import kotlinx.android.synthetic.main.profile_fragment.profileMenu
import kotlinx.android.synthetic.main.profile_fragment.settingFab
import kotlinx.android.synthetic.main.profile_fragment.threadsLink_profile
import kotlinx.android.synthetic.main.profile_fragment.threads_profile
import kotlinx.android.synthetic.main.profile_fragment.twitterLink_profile
import kotlinx.android.synthetic.main.profile_fragment.twitter_profile
import kotlinx.android.synthetic.main.profile_fragment.userBio
import kotlinx.android.synthetic.main.profile_fragment.userFullName
import kotlinx.android.synthetic.main.profile_fragment.view.editProfile_fab
import kotlinx.android.synthetic.main.profile_fragment.view.github_profile
import kotlinx.android.synthetic.main.profile_fragment.view.instagram_profile
import kotlinx.android.synthetic.main.profile_fragment.view.linkedin_profile
import kotlinx.android.synthetic.main.profile_fragment.view.ll_followers
import kotlinx.android.synthetic.main.profile_fragment.view.ll_following
import kotlinx.android.synthetic.main.profile_fragment.view.portfolio_profile
import kotlinx.android.synthetic.main.profile_fragment.view.profileImage
import kotlinx.android.synthetic.main.profile_fragment.view.profileMenu
import kotlinx.android.synthetic.main.profile_fragment.view.settingFab
import kotlinx.android.synthetic.main.profile_fragment.view.threads_profile
import kotlinx.android.synthetic.main.profile_fragment.view.totalFollowers_user
import kotlinx.android.synthetic.main.profile_fragment.view.totalFollowing_user
import kotlinx.android.synthetic.main.profile_fragment.view.twitter_profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var postAdapter: MyPostAdapter? = null
    private var mPost:MutableList<PostModel>? = null
    private var clicked= false

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var profileId:String

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.to_bottom_anim) }
    private val toInvisibility: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.to_invisibility) }
    private val toVisibility: Animation by lazy { AnimationUtils.loadAnimation(context,R.anim.to_visibility) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root:View = inflater.inflate(R.layout.profile_fragment, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null){
            this.profileId = pref.getString("uid","none")!!
        }

        CoroutineScope(Dispatchers.IO).launch {
            launch { userInfo() }
            launch { getFollowers(root) }
            launch { getFollowings(root) }
        }

        root.github_profile.setOnClickListener {
            val url = gitlink_profile.text.toString()
            openInBrowser(url)
        }
        root.linkedin_profile.setOnClickListener {
            val url = linkednLink_profile.text.toString()
            openInBrowser(url)
        }
        root.portfolio_profile.setOnClickListener {
            val url = portfolioLink_profile.text.toString()
            openInBrowser(url)
        }
        root.instagram_profile.setOnClickListener {
            val url = instagramLink_profile.text.toString()
            openInBrowser(url)
        }
        root.threads_profile.setOnClickListener {
            val url = threadsLink_profile.text.toString()
            openInBrowser(url)
        }
        root.twitter_profile.setOnClickListener {
            val url = twitterLink_profile.text.toString()
            openInBrowser(url)
        }



        root.ll_followers.setOnClickListener {
            val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
            pref!!.putString("uid",FirebaseAuth.getInstance().currentUser!!.uid)
            pref.putString("title","Followers")
            pref.apply()
            Navigation.findNavController(root).navigate(R.id.action_navigation_profile_to_showUserFragment)
        }

        root.ll_following.setOnClickListener {
            val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
            pref!!.putString("uid",FirebaseAuth.getInstance().currentUser!!.uid)
            pref.putString("title","Following")
            pref.apply()
            Navigation.findNavController(root).navigate(R.id.action_navigation_profile_to_showUserFragment)
        }

        recyclerView = root.findViewById(R.id.myPostRV)
        recyclerView!!.setHasFixedSize(true)
        val layoutManager:LinearLayoutManager = GridLayoutManager(context,2)
        recyclerView!!.layoutManager = layoutManager

        mPost = ArrayList()
        postAdapter = context?.let { MyPostAdapter(it,mPost as ArrayList<PostModel>,"MyProfile") }
        recyclerView?.adapter = postAdapter
        postAdapter!!.notifyDataSetChanged()

        CoroutineScope(Dispatchers.IO).launch {
            launch { retrievePost() }
        }

        root.profileMenu.setOnClickListener {
            addButtonClicked()
        }

        root.settingFab.setOnClickListener {
            val intent = Intent(context, SettingActivity::class.java)
            startActivity(intent)
        }

        root.editProfile_fab.setOnClickListener {
            val intent = Intent(context, EditProfileActivity::class.java)
            startActivity(intent)
        }

        root.profileImage.setOnClickListener {
            Navigation.findNavController(root).navigate(R.id.action_navigation_profile_to_myDetailsFragment)
        }

        return  root
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val userData = snapshot.getValue(UserModel::class.java)
                    context?.let { Glide.with(it).load(userData!!.profileImage).into(profileImage) }
                    userFullName.text = userData!!.fullName
                    userBio.text = userData.bio
                    gitlink_profile.text = userData.githubLink
                    linkednLink_profile.text = userData.linkedin
                    portfolioLink_profile.text = userData.portfolio
                    instagramLink_profile.text = userData.instagram
                    twitterLink_profile.text = userData.twitter
                    threadsLink_profile.text = userData.threads


                    if (userData.githubLink.isNullOrEmpty()){
                        github_profile.setImageResource(R.drawable.git_blur)
                        github_profile.isClickable = false
                    }
                    if (userData.portfolio.isNullOrEmpty()){
                        portfolio_profile.setImageResource(R.drawable.portfolio_blur)
                        portfolio_profile.isClickable = false
                    }
                    if (userData.instagram.isNullOrEmpty()){
                        instagram_profile.setImageResource(R.drawable.insta_blur)
                        instagram_profile.isClickable = false
                    }
                    if (userData.twitter.isNullOrEmpty()){
                        twitter_profile.setImageResource(R.drawable.twitter_blur)
                        twitter_profile.isClickable = false
                    }
                    if (userData.linkedin.isNullOrEmpty()){
                        linkedin_profile.setImageResource(R.drawable.linkedin_blur)
                        linkedin_profile.isClickable = false
                    }
                    if (userData.threads.isNullOrEmpty()){
                        threads_profile.setImageResource(R.drawable.threads_blur)
                        threads_profile.isClickable = false
                    }

                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("some error occurred")
            }
        })
    }
    private fun getFollowers(root: View){
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Followers")

        followingRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val count = snapshot.childrenCount.toInt()
                    root.totalFollowers_user.text = count.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun getFollowings(root: View){
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")
        followingRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val count = snapshot.childrenCount.toInt()-1
                    root.totalFollowing_user.text = count.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun retrievePost(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Post")
        postRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mPost!!.clear()
                for(snapshot in snapshot.children){
                    val post = snapshot.getValue(PostModel::class.java)
                    if (post != null){
                        if(post.publisher == FirebaseAuth.getInstance().currentUser!!.uid){
                            mPost!!.add(post)
                        }
                    }
                    postAdapter!!.notifyDataSetChanged()
                    postCount_profile.text = mPost?.size.toString()
                    if (mPost.isNullOrEmpty()){
                        noPostLayout?.visibility = View.VISIBLE
                        myPostLayout?.visibility = View.GONE
                    }else{
                        noPostLayout?.visibility = View.GONE
                        myPostLayout?.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun addButtonClicked(){
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }
    private fun setVisibility(clicked:Boolean) {
        if (!clicked){
            editProfile_fab.visibility = View.VISIBLE
            settingFab.visibility = View.VISIBLE
        }else {
            editProfile_fab.visibility = View.GONE
            settingFab.visibility = View.GONE
        }
    }
    private fun setAnimation(clicked:Boolean) {
        if (!clicked){
            editProfile_fab.startAnimation(fromBottom)
            settingFab.startAnimation(fromBottom)
            profileMenu.startAnimation(rotateOpen)
            profileMenu.setImageResource(R.drawable.clos_angle)
        }else{
            editProfile_fab.startAnimation(toBottom)
            settingFab.startAnimation(toBottom)
            profileMenu.startAnimation(rotateClose)
            profileMenu.setImageResource(R.drawable.menu)
        }
    }


    private fun openInBrowser(url:String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    //User Post init function

}