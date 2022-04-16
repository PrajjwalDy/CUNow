package com.hindu.cunow.Adapter

import android.app.AlertDialog
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.cunow.Activity.CircleFlowActivity
import com.hindu.cunow.Activity.CommentActivity
import com.hindu.cunow.Model.CircleModel
import com.hindu.cunow.Model.JoinedCircleModel
import com.hindu.cunow.R
import kotlinx.android.synthetic.main.circle_more_option.view.*

class JoinedCircleAdapter(private val mContext:Context,
                          private val mCircle: List<JoinedCircleModel>
): RecyclerView.Adapter<JoinedCircleAdapter.ViewHolder>(){

    inner class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){

        val iconImage: ImageView = itemView.findViewById(R.id.circle_icon_joined) as ImageView
        val circleName: TextView = itemView.findViewById(R.id.circle_name_joined) as TextView
        val circleDescription:TextView = itemView.findViewById(R.id.circle_description_joined) as TextView
        val circleMoreOption:ImageView = itemView.findViewById(R.id.circle_joined_moreOption) as ImageView

    }

    private fun fetchJoined(circleIcon:ImageView,circleName:TextView,circleDescription:TextView,circleId:String){
        val circleData= FirebaseDatabase.getInstance().reference
            .child("Circle")
            .child(circleId)
        circleData.addListenerForSingleValueEvent(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val data = snapshot.getValue(CircleModel::class.java)
                    Glide.with(mContext).load(data!!.icon).into(circleIcon)
                    circleName.text = data.circleName
                    circleDescription.text = data.circle_description
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.joined_circles_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        fetchJoined(holder.iconImage,holder.circleName,holder.circleDescription,mCircle[position].JCId!!)
        holder.itemView.setOnClickListener {
            val commentIntent = Intent(mContext, CircleFlowActivity::class.java)
            commentIntent.putExtra("circleId",mCircle[position].JCId!!)
            mContext.startActivity(commentIntent)
        }

        holder.circleMoreOption.setOnClickListener {
            val dialogView = LayoutInflater.from(mContext).inflate(R.layout.circle_more_option, null)

            val dialogBuilder = AlertDialog.Builder(mContext)
                .setView(dialogView)
                .setTitle("Options")

            val alertDialog = dialogBuilder.show()

            dialogView.leaveCircle.setOnClickListener {
                leaveCircle(mCircle[position].JCId!!)
            }

        }
    }

    override fun getItemCount(): Int {
        return mCircle.size
    }

    private fun leaveCircle(circleId: String){
        FirebaseDatabase.getInstance().reference.child("Circle")
            .child(circleId).child("Members")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).removeValue()
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Joined_Circles")
            .child(circleId)
            .removeValue()

        Toast.makeText(mContext,"You left the circle successfully", Toast.LENGTH_SHORT).show()
    }
}