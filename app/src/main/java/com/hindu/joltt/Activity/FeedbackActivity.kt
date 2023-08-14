package com.hindu.joltt.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hindu.cunow.R
import kotlinx.android.synthetic.main.activity_feedback.done
import kotlinx.android.synthetic.main.activity_feedback.fName
import kotlinx.android.synthetic.main.activity_feedback.feedText
import kotlinx.android.synthetic.main.activity_feedback.feedbackBack
import kotlinx.android.synthetic.main.activity_feedback.feedbackTxt
import kotlinx.android.synthetic.main.activity_feedback.ll_feedLayout
import kotlinx.android.synthetic.main.activity_feedback.sendFeedback

class FeedbackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        feedbackBack.setOnClickListener {
            finish()
        }
        feedbackTxt.setOnClickListener {
            finish()
        }

        sendFeedback.setOnClickListener {
            requestHelp()
        }
    }
    private fun requestHelp(){
        if(fName.text.isEmpty()||feedText.text.isEmpty()){
            Toast.makeText(this,"Credentials are Required", Toast.LENGTH_LONG).show()
        }else{
            val ref = FirebaseDatabase.getInstance().reference.child("Feedback")
            val helpId = ref.push().key

            val postMap = HashMap<String,Any>()
            postMap["feedbackId"] = helpId!!
            postMap["fName"] = fName.text.toString()
            postMap["feedText"] = feedText.text.toString()
            postMap["fUID"] = FirebaseAuth.getInstance().currentUser!!.uid

            ref.child(helpId).updateChildren(postMap)

            fName.text.clear()
            feedText.text.clear()
            Toast.makeText(this,"Feedback sent success", Toast.LENGTH_LONG).show()
            ll_feedLayout.visibility  = View.GONE
            done.visibility = View.VISIBLE

            finish()

        }

    }
}