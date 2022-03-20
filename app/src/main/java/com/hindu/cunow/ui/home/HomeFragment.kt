package com.hindu.cunow.ui.home

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerLibraryInfo
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.material.progressindicator.LinearProgressIndicatorSpec
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.cunow.Activity.AddPostActivity
import com.hindu.cunow.Activity.VideoUploadActivity
import com.hindu.cunow.Adapter.PostAdapter
import com.hindu.cunow.Model.PostModel
import com.hindu.cunow.Model.UserModel
import com.hindu.cunow.R
import com.hindu.cunow.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.image_or_video_dialogbox.view.*
import kotlinx.android.synthetic.main.more_option_dialogbox.view.*

class HomeFragment : Fragment() {

    var recyclerView: RecyclerView? = null
    private var postAdapter: PostAdapter? = null

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.postModel!!.observe(viewLifecycleOwner, Observer {
            initView(root)
            postAdapter = context?.let { it1-> PostAdapter(it1,it) }
            recyclerView!!.adapter = postAdapter
            postAdapter!!.notifyDataSetChanged()

        })

        root.select_media_img.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.image_or_video_dialogbox, null)

            val dialogBuilder = AlertDialog.Builder(context)
                .setView(dialogView)

            val alertDialog = dialogBuilder.show()
            dialogView.selectImage.setOnClickListener {
                startActivity(Intent(context,AddPostActivity::class.java))
                alertDialog.dismiss()
            }
            dialogView.selectVideo.setOnClickListener {
                startActivity(Intent(context,VideoUploadActivity::class.java))
                alertDialog.dismiss()
            }


        }

        return root
    }

    private fun postText(view: View){
        Snackbar.make(view,"adding post....",Snackbar.LENGTH_SHORT).show()

        val ref = FirebaseDatabase.getInstance().reference.child("Post")
        val postId = ref.push().key

        val postMap = HashMap<String,Any>()
        postMap["postId"] = postId!!
        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
        postMap["caption"] = caption_only.text.toString()
        postMap["image"] = ""

        ref.child(postId).updateChildren(postMap)

        Snackbar.make(view,"post added successfully",Snackbar.LENGTH_SHORT).show()

        caption_only.text.clear()
    }
    private fun initView(root:View){
        recyclerView = root.findViewById(R.id.postRecyclerView) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.isNestedScrollingEnabled = false
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView!!.layoutManager = linearLayoutManager
        loadUserImage(root)

    }

    private fun loadUserImage(root: View){
        val dataRef = FirebaseDatabase
            .getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        dataRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listData = snapshot.getValue(UserModel::class.java)
                if (listData!!.profileImage == ""){
                    root.userProfileImg.visibility = View.GONE
                }else{
                    Glide.with(context!!).load(listData.profileImage).into(root.userProfileImg)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun checkFirstVisit(){

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}