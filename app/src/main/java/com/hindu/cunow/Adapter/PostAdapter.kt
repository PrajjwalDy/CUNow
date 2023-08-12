package com.hindu.cunow.Adapter

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.core.os.postDelayed
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.util.Util
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MediaFormatUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.cunow.Activity.CommentActivity
import com.hindu.cunow.Activity.HelpActivity
import com.hindu.cunow.Activity.ReportPostActivity
import com.hindu.cunow.Fragments.Pages.PageDetailsActivity
import com.hindu.cunow.Model.NotificationModel
import com.hindu.cunow.Model.PageModel
import com.hindu.cunow.Model.PostModel
import com.hindu.cunow.Model.UserModel
import com.hindu.cunow.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.delete_confirm_layout.cancel_delete
import kotlinx.android.synthetic.main.delete_confirm_layout.confirmDelete
import kotlinx.android.synthetic.main.more_option_dialogbox.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class PostAdapter (private val mContext: Context,
                   private val mPost:List<PostModel>,
                   ):RecyclerView.Adapter<PostAdapter.ViewHolder>()
{

    private var posts: List<PostModel> = emptyList()

    fun setPosts(filteredPosts: List<PostModel>) {
        posts = filteredPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //ANIMATION DECLARATION
        val zoom = AnimationUtils.loadAnimation(mContext, R.anim.zoom)
        val popUp = AnimationUtils.loadAnimation(mContext, R.anim.pop_up_animation)
        val fromRight = AnimationUtils.loadAnimation(mContext, R.anim.from_right)

        //DIALOG VIEW DECLARATION


        val post = mPost[position]
        CoroutineScope(Dispatchers.IO).launch {
            launch { islike(post.postId!!, holder.like) }
            launch { totalLikes(post.postId!!,holder.totalLikes) }
            launch { totalComments(holder.totalComments,post.postId!!) }
        }

        holder.bind(mPost[position],mContext,holder.image,holder.playerView)

        if (mPost[position].page){
            CoroutineScope(Dispatchers.IO).launch {
                pageInfo(holder.publisherImage,holder.publisherName,post.publisher!!)
            }

        }else{
            CoroutineScope(Dispatchers.IO).launch {
                publisher(holder.publisherImage,holder.publisherName,post.publisher!!,holder.verification)
            }
        }

        holder.publisherName.setOnClickListener {
            if (mPost[position].page){
                val intent = Intent(mContext, PageDetailsActivity::class.java)
                intent.putExtra("pageId",mPost[position].publisher)
                intent.putExtra("pageAdmin",mPost[position].pageAdmin)
                mContext.startActivity(intent)
            }else{
                val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("uid",post.publisher)
                pref.apply()

                Navigation.findNavController(holder.itemView).navigate(R.id.action_navigation_home_to_userProfile)
            }
        }

        //LIKE BUTTON
        holder.like.setOnClickListener {
            like(holder.like,
                post.postId!!,zoom,
                post.publisher!!,
                holder.animation,
                holder.caption,
                holder.totalLikes,
            )
        }

        //COMMENT BUTTON
        holder.comment.setOnClickListener {
            if (post.page){
                val commentIntent = Intent(mContext,CommentActivity::class.java)
                commentIntent.putExtra("postId",post.postId)
                commentIntent.putExtra("publisher",post.publisher)
                commentIntent.putExtra("pageName",post.pageName)
                commentIntent.putExtra("pageAdmin",post.pageAdmin)
                commentIntent.putExtra("page",post.page)
                commentIntent.putExtra("pageId",post.publisher)
                mContext.startActivity(commentIntent)
            }else{
                val commentIntent = Intent(mContext,CommentActivity::class.java)
                commentIntent.putExtra("postId",post.postId)
                commentIntent.putExtra("publisher",post.publisher)
                mContext.startActivity(commentIntent)
            }

        }

        //COMMENT BUTTON

        holder.moreOption.setOnClickListener {
            val dialogView = LayoutInflater.from(mContext).inflate(R.layout.more_option_dialogbox, null)

            dialogView.startAnimation(popUp)

            val dialogBuilder = AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setTitle("Options")


            val alertDialog = dialogBuilder.show()

            dialogView.savePost.setOnClickListener {
                FirebaseDatabase.getInstance().reference
                    .child("Users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("SavedPosts")
                    .child(post.postId!!)
                    .setValue(true)
                alertDialog.dismiss()
            }

            if (post.publisher != FirebaseAuth.getInstance().currentUser!!.uid){
                dialogView.deletePost.visibility = View.GONE
            }

            dialogView.deletePost.setOnClickListener {
                val confirmView = LayoutInflater.from(mContext).inflate(R.layout.delete_confirm_layout, null)
                confirmView.startAnimation(popUp)
                val buildDialog = AlertDialog.Builder(mContext)
                    .setView(confirmView)
                    .setTitle("Are you sure?")

                val confDialog = buildDialog.show()

                confDialog.confirmDelete.setOnClickListener {
                    FirebaseDatabase.getInstance().reference.child("Post")
                        .child(post.postId!!)
                        .removeValue()
                    removeNotification(post.postId)
                    Snackbar.make(holder.itemView,"Post removed success",Snackbar.LENGTH_SHORT).show()
                    confDialog.dismiss()
                    alertDialog.dismiss()
                }

                confDialog.cancel_delete.setOnClickListener {
                    confDialog.dismiss()
                    alertDialog.dismiss()
                }

            }

            dialogView.reportPost.setOnClickListener {
                val commentIntent = Intent(mContext,ReportPostActivity::class.java)
                commentIntent.putExtra("postId",post.postId)
                mContext.startActivity(commentIntent)
                alertDialog.dismiss()
            }

            alertDialog.setCanceledOnTouchOutside(true)

        }

        holder.postCardView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.card_view_anim))
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    //INNER CLASS
    inner class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.postImage) as ImageView
        val caption: TextView = itemView.findViewById(R.id.caption) as TextView
        val publisherImage:CircleImageView = itemView.findViewById(R.id.publisher_profile_image) as CircleImageView
        val publisherName: TextView = itemView.findViewById(R.id.publisher_Name) as TextView
        val verification:CircleImageView = itemView.findViewById(R.id.verification_image) as CircleImageView
        val like: ImageView = itemView.findViewById(R.id.like) as ImageView
        val playerView: PlayerView = itemView.findViewById(R.id.videoPlayer) as PlayerView
        val comment:ImageView = itemView.findViewById(R.id.comment) as ImageView
        val animation:LottieAnimationView = itemView.findViewById(R.id.animation) as LottieAnimationView
        val moreOption:ImageView = itemView.findViewById(R.id.moreOptionPost) as ImageView
        val totalLikes: TextView = itemView.findViewById(R.id.totalLikes) as TextView
        val totalComments:TextView = itemView.findViewById(R.id.totalComments)
        val postCardView:CardView = itemView.findViewById(R.id.postCV)
        val mediaCV: CardView = itemView.findViewById(R.id.media_cv) as CardView
        val duration:TextView = itemView.findViewById(R.id.videoDuration) as TextView
        val rl_duration:RelativeLayout = itemView.findViewById(R.id.videoDuration_RL) as RelativeLayout


        fun bind(list:PostModel,context: Context,imageView: ImageView,playerView: PlayerView){

            val progressDialog = Dialog(context)
            progressDialog.setContentView(R.layout.profile_dropdown_menu)
            progressDialog.show()
            if (list.caption == ""){
                caption.visibility = View.GONE
            }else{
                caption.visibility = View.VISIBLE
                caption.text = list.caption
            }

            if (list.iImage){
                imageView.visibility = View.VISIBLE
                playerView.visibility = View.GONE
                mediaCV.visibility = View.VISIBLE
                Glide.with(context).load(list.image).into(image)
            }else if(list.video){
                imageView.visibility = View.GONE
                playerView.visibility = View.VISIBLE
                rl_duration.visibility = View.VISIBLE
                playVideo(playerView,list.videoId!!,duration)
                mediaCV.visibility = View.VISIBLE
            }else{
                playerView.visibility = View.GONE
                imageView.visibility = View.GONE
                mediaCV.visibility = View.GONE
            }
            progressDialog.dismiss()

        }
    }

    //PUBLISHER
    private suspend fun publisher(profileImage:CircleImageView, name:TextView,publisherId:String,verifImage:CircleImageView){

        val userDataRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)

        userDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val data = snapshot.getValue(UserModel::class.java)
                    Glide.with(mContext).load(data!!.profileImage).into(profileImage)
                    name.text = data.fullName
                    if (data.verification){
                        verifImage.visibility =View.VISIBLE
                    }else{
                        verifImage.visibility =View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        userDataRef.keepSynced(true)
    }

    //Like Mechanism
    private fun like(likeButton: ImageView,
                     postId: String,
                     zoom:Animation,
                     publisherId: String,
                     likeAnimationView: LottieAnimationView,
                     caption: TextView,
                     likeTextView: TextView){
        likeButton.startAnimation(zoom)
        if (likeButton.tag == "Like"){
            FirebaseDatabase.getInstance().reference
                .child("Likes")
                .child(postId)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(true)

            val ref = FirebaseDatabase.getInstance().reference.child("Post").child(postId)
            ref.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val data = snapshot.getValue(PostModel::class.java)
                        if (data!!.page){
                            addPageNotification(data.pageAdmin!!,data.postId!!,data.pageName!!,data.publisher!!)
                        }else{
                            addNotification(publisherId,postId,caption)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            ref.keepSynced(true)

        }else{
            FirebaseDatabase.getInstance().reference
                .child("Likes")
                .child(postId)
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .removeValue()
            likeButton.tag = "Like"
        }
        CoroutineScope(Dispatchers.IO).launch {
            islike(postId,likeButton)
            totalLikes(postId,likeTextView)
        }
    }

    //IS LIKE FUNCTION
    private suspend fun islike(postId:String, likeButton:ImageView){
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val likeRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postId)
        likeRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser!!.uid).exists()){
                    likeButton.setImageResource(R.drawable.thunder_filled)
                    likeButton.tag = "Liked"
                }else{
                    likeButton.tag = "Like"
                    if (likeButton.tag == "Like"){
                        likeButton.setImageResource(R.drawable.thunder_blank)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        likeRef.keepSynced(true)

    }

    //PLAY VIDEO
    private fun playVideo(videoView:PlayerView,videoUrl: String,duration:TextView){
        initPlayer(videoView,videoUrl)


        val player = videoView.player
        player!!.addListener(object : Player.Listener {
            private val handler = Handler()
            private val updateProgressAction = object : Runnable {
                override fun run() {
                    updateProgress(videoView,duration)
                    handler.postDelayed(this, 1000) // Update every 1 second
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when(playbackState){
                    Player.STATE_READY->{
                        handler.postDelayed(updateProgressAction,1000)
                    }else->{
                        handler.removeCallbacks(updateProgressAction)
                    }
                }
            }
        })
    }

    //INIT PLAYER
    private fun initPlayer(videoView:PlayerView,videoUrl: String) {
        lateinit var simpleExoPlayer:SimpleExoPlayer
        lateinit var mediaSource: MediaSource


        simpleExoPlayer = SimpleExoPlayer.Builder(mContext).build()

        videoView.player = simpleExoPlayer
        val urlType = URLType.MP4
        urlType.url = videoUrl

        simpleExoPlayer.seekTo(0)
        when (urlType){
            URLType.MP4 ->{
                val datasourceFactory:DataSource.Factory = DefaultDataSourceFactory(
                    mContext,
                    com.google.android.exoplayer2.util.Util.getUserAgent(mContext,"")
                )
                mediaSource = ProgressiveMediaSource.Factory(datasourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }else->{}
        }

        simpleExoPlayer.setMediaSource(mediaSource)
        simpleExoPlayer.prepare()
        val playerListener = object :Player.Listener{

            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
                videoView.useController = true
                (videoView.player as SimpleExoPlayer).playWhenReady = false
                videoView.requestFocus()
                videoView.setShowNextButton(false)
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                //Toast.makeText(mContext,"Some Error Occurred",Toast.LENGTH_SHORT).show()
            }

        }
        simpleExoPlayer.addListener(playerListener)

    }

    //URL OF THE VIDEO
    enum class URLType(var url:String){
        MP4(""), HLS("")
    }

    //TOTAL LIKES
    private suspend fun totalLikes(postId: String, totalLikes:TextView){
        val databaseRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)
        databaseRef.addValueEventListener(object : ValueEventListener{
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    totalLikes.text = snapshot.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        databaseRef.keepSynced(true)
    }

    //ADD NOTIFICATIONS
    private fun addNotification(publisherId: String,postId: String,caption:TextView){
        if (publisherId != FirebaseAuth.getInstance().currentUser!!.uid){
            val dataRef = FirebaseDatabase.getInstance()
                .reference.child("Notification")
                .child("AllNotification")
                .child(publisherId)

            val notificationId = dataRef.push().key!!

            val dataMap = HashMap<String,Any>()
            dataMap["notificationId"] = notificationId
            dataMap["notificationText"] = "Liked your post "+caption.text.toString()
            dataMap["postID"] = postId
            dataMap["postN"] = true
            dataMap["notifierId"] = FirebaseAuth.getInstance().currentUser!!.uid

            dataRef.child(notificationId).setValue(dataMap)

            FirebaseDatabase.getInstance().reference
                .child("Notification")
                .child("UnReadNotification")
                .child(publisherId).child(notificationId).setValue(true)


        }
    }

    //TOTAL COMMENTS
    private suspend fun totalComments(totalComments:TextView, postId: String){
        val postData = FirebaseDatabase.getInstance().reference.child("Comments")
            .child(postId)

        postData.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    totalComments.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        postData.keepSynced(true)

    }

    //ADD PAGE NOTIFICATION
    private fun addPageNotification(pageAdmin: String,postId: String,pageName:String,pageId:String){
        if (pageAdmin != FirebaseAuth.getInstance().currentUser!!.uid){
            val dataRef = FirebaseDatabase.getInstance()
                .reference.child("Notification")
                .child("AllNotification")
                .child(pageAdmin)

            val notificationId = dataRef.push().key!!

            val dataMap = HashMap<String,Any>()
            dataMap["notificationId"] = notificationId
            dataMap["notificationText"] = "You have new notifications for page:"+pageName
            dataMap["postID"] = postId
            dataMap["postN"] = false
            dataMap["pageN"] = true
            dataMap["pageId"] = pageId
            dataMap["notifierId"] = FirebaseAuth.getInstance().currentUser!!.uid

            dataRef.child(notificationId).setValue(dataMap)
            addPageN(pageAdmin,postId,pageName,pageId)

            FirebaseDatabase.getInstance().reference
                .child("Notification")
                .child("UnReadNotification")
                .child(pageAdmin).child(notificationId).setValue(true)
        }
    }

    //ADD PAGE DETAILS
    private fun addPageN(pageAdmin: String,postId: String,pageName:String,pageId:String){
        val dataNRef = FirebaseDatabase.getInstance()
            .reference.child("PageNotification")
            .child("AllNotifications")
            .child(pageAdmin)
            .child(pageId)

        val nId = dataNRef.push().key!!

        val dataNMap = HashMap<String,Any>()
        dataNMap["nId"] = nId
        dataNMap["nText"] = "new like on post: "
        dataNMap["postImage"] = postId
        dataNMap["iPost"] = true
        dataNMap["pageId"] = pageId
        dataNMap["notifier"] = FirebaseAuth.getInstance().currentUser!!.uid

        dataNRef.child(nId).updateChildren(dataNMap)
    }

    //FETCH PAGE DETAILS
    private suspend fun pageInfo(profileImage:CircleImageView, name:TextView,publisherId:String) {
        val userDataRef = FirebaseDatabase.getInstance().reference.child("Pages").child(publisherId)

        userDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val data = snapshot.getValue(PageModel::class.java)
                    Glide.with(mContext).load(data!!.pageIcon).into(profileImage)
                    name.text = data.pageName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //OBJECT FOR DURATION OF THE VIDEO
    object FormatUtils {
        fun formatDuration(duration: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(duration)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    //UPDATE THE PROGRESS OF THE VIDEO WHILE PLAYING
    private fun updateProgress(videoView: PlayerView,duration: TextView) {
        val player = videoView.player
        val currentPosition = player!!.currentPosition
        val formattedPosition = FormatUtils.formatDuration(currentPosition)
        duration.text = formattedPosition
    }

    //REMOVE NOTIFICATION
    private fun removeNotification(postId: String){
        val dbRef = FirebaseDatabase.getInstance().reference.child("Notification")
            .child("AllNotification")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        dbRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children){
                    val nData = snapshot.getValue(NotificationModel::class.java)
                    if (nData!!.postID == postId){
                        snapshot.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}