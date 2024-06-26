package com.hindu.joltt.Activity

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.hindu.cunow.R
import com.hindu.joltt.Adapter.ChatAdapter
import com.hindu.joltt.Model.ChatModel
import com.hindu.joltt.Model.UserModel
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView


class ChatActivity : AppCompatActivity() {


    private var profileId = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageChatImageRef: StorageReference? =null
    private var media = ""

    //objects
    private lateinit var selectMedia:ImageView
    private lateinit var send:ImageView
    private lateinit var chatMessage:EditText
    private lateinit var moreOption:ImageView
    private lateinit var viewMore:CardView
    private lateinit var back:ImageView
    private lateinit var userName:TextView
    private lateinit var userProfile:CircleImageView
    private lateinit var chatImagePreview:ImageView




    private var chatList:MutableList<ChatModel>? = null
    private var chatAdapter:ChatAdapter? = null

    var check = false

    //UPDATING THE TIME STAMP
    private val handler = Handler()
    private val updateDelayMillis = 1000*60

    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom3
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim3
        )
    }

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        storageChatImageRef = FirebaseStorage.getInstance().reference.child("Chat Flow")

        val intent = intent
        profileId = intent.getStringExtra("uid").toString()

        val recyclerView:RecyclerView = findViewById(R.id.chatRV)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        linearLayoutManager.stackFromEnd = true

        chatList = ArrayList()
        chatAdapter = ChatAdapter(this, chatList as ArrayList<ChatModel>)
        recyclerView.adapter = chatAdapter

        startPeriodicTimestampUpdate()

        //object Decleration

        selectMedia = findViewById(R.id.selectMedia_chat)
        send = findViewById(R.id.send_chat)
        back = findViewById(R.id.chat1Back)
        viewMore = findViewById(R.id.viewMore_chat)
        moreOption = findViewById(R.id.moreOption_chat)
        chatImagePreview = findViewById(R.id.chatImage_preview)
        userName = findViewById(R.id.userName_chat)
        userProfile = findViewById(R.id.chatUser_Image)
        chatMessage = findViewById(R.id.chat_message)






        selectMedia.setOnClickListener {
            cropImage()
            media = "yes"
        }

        send.setOnClickListener { view->
            if (imageUri != null){
                uploadImage()
            }else{
                sendMessage(view,recyclerView)
            }
        }
        moreOption.setOnClickListener {
            if (!check){
                moreOption.startAnimation(rotateOpen)
                viewMore.visibility = View.VISIBLE
                viewMore.startAnimation(toBottom)
                check = true
            }else{
                moreOption.startAnimation(rotateClose)
                viewMore.visibility = View.GONE
                viewMore.startAnimation(fromBottom)
                check = false
            }


        }

        loadUserData()
        retrieveChat(recyclerView)

        back.setOnClickListener {
            finish()
        }

    }

    private fun startPeriodicTimestampUpdate() {
        handler.postDelayed(timestampUpdaterRunnable, updateDelayMillis.toLong())
    }

    private val timestampUpdaterRunnable = object :Runnable{
        override fun run() {
            chatAdapter!!.notifyDataSetChanged()
            handler.postDelayed(this, updateDelayMillis.toLong())
        }
    }


    private fun cropImage(){
        CropImage.activity()
            .start(this)
    }

    //SEND MESSAGE
    private fun sendMessage(view:View,recyclerView: RecyclerView){
        if (chatMessage.text.isEmpty()) {
            Snackbar.make(view, "please write something..", Snackbar.LENGTH_SHORT).show()
        } else {
            FirebaseDatabase.getInstance().reference.child("ChatList")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(profileId).setValue(true)

            FirebaseDatabase.getInstance().reference.child("ChatList")
                .child(profileId)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(true)

            val dataRef = FirebaseDatabase.getInstance().reference
                .child("ChatData")

            val commentId = dataRef.push().key

            val dataMap = HashMap<String, Any>()
            dataMap["messageId"] = commentId!!
            dataMap["chatText"] = chatMessage.text.toString()
            dataMap["sender"] = FirebaseAuth.getInstance().currentUser!!.uid
            dataMap["receiver"] = profileId
            dataMap["containImage"] = false
            dataMap["timeStamp"] = System.currentTimeMillis().toString()

            dataRef.child(commentId).setValue(dataMap)
            chatMessage.text.clear()
            FirebaseDatabase.getInstance().reference.child("ChatMessageCount")
                .child(profileId)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(commentId).setValue(true)

            scrollToBottom(recyclerView)
        }
    }

    //UPLOAD IMAGE
    private fun uploadImage(){
        val fileReference = storageChatImageRef!!
            .child(System.currentTimeMillis().toString()+".jpg")

        FirebaseDatabase.getInstance().reference.child("ChatList")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(profileId)
            .setValue(true)

        FirebaseDatabase.getInstance().reference.child("ChatList")
            .child(profileId)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(true)

        val ref = FirebaseDatabase.getInstance()
            .reference
            .child("ChatData")

        val chatId = ref.push().key

        val dataMap = HashMap<String, Any>()
        dataMap["messageId"] = chatId!!
        dataMap["chatText"] = chatMessage.text.toString()
        dataMap["sender"] = FirebaseAuth.getInstance().currentUser!!.uid
        dataMap["containImage"] = true
        dataMap["receiver"] = profileId
        dataMap["chatImage"] = ""
        dataMap["timeStamp"] = System.currentTimeMillis().toString()

        ref.child(chatId).updateChildren(dataMap)

        val uploadTask:StorageTask<*>
        uploadTask = fileReference.putFile(imageUri!!)

        uploadTask.continueWithTask<Uri?>(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task ->
            if (task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation fileReference.downloadUrl
        }).addOnCompleteListener(OnCompleteListener<Uri>{task ->
            if (task.isSuccessful){
                val downloadUrl = task.result
                myUrl = downloadUrl.toString()


                val dataMap = HashMap<String, Any>()
                dataMap["messageId"] = chatId
                dataMap["sender"] = FirebaseAuth.getInstance().currentUser!!.uid
                dataMap["receiver"] = profileId
                dataMap["containImage"] = true
                dataMap["chatImage"] = myUrl
                dataMap["timeStamp"] = System.currentTimeMillis().toString()

                ref.child(chatId).updateChildren(dataMap)
                Toast.makeText(this,"Image Sent", Toast.LENGTH_SHORT).show()

                FirebaseDatabase.getInstance().reference.child("ChatMessageCount")
                    .child(profileId)
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(chatId).setValue(true)
                media = ""
                imageUri = null

            }else{
                Toast.makeText(this,"Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //RETRIEVE CHAT
    private fun retrieveChat(recyclerView: RecyclerView){
        val chatData = FirebaseDatabase.getInstance().reference.child("ChatData")

        chatData.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    chatList!!.clear()
                    for (snapshot in snapshot.children){
                        val chat = snapshot.getValue(ChatModel::class.java)

                        if (chat!!.receiver.equals(FirebaseAuth.getInstance().currentUser!!.uid) && chat.sender.equals(profileId)
                            ||chat.receiver.equals(profileId) && chat.sender.equals(FirebaseAuth.getInstance().currentUser!!.uid) ){
                            chatList!!.add(chat!!)
                            scrollToBottom(recyclerView)
                        }

                    }
                    chatAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //LOAD USER DATA
    private fun loadUserData(){
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(profileId)

        usersRef.addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){
                        val user = snapshot.getValue(UserModel::class.java)
                        Glide.with(this@ChatActivity).load(user!!.profileImage).into(userProfile)
                        userName.text = user.fullName
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )
    }

    //ON ACTIVITY RESULT
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            chatImagePreview?.visibility = View.VISIBLE
            chatImagePreview.setImageURI(imageUri)

            chatMessage.setHint("Click on send button to send the image")
        }
    }

    //SCROLL TO BOTTOM
    private fun scrollToBottom(recyclerView: RecyclerView){

        /*chatAdapter!!.notifyItemInserted(chatAdapter!!.itemCount-1)
        recyclerView.smoothScrollToPosition(chatAdapter!!.itemCount-1)*/
        recyclerView.post { recyclerView.scrollToPosition(chatAdapter!!.itemCount-1) }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the periodic timestamp update when the activity is destroyed
        handler.removeCallbacks(timestampUpdaterRunnable)

        finish()

    }


}