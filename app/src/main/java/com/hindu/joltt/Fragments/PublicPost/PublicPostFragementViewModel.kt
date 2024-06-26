package com.hindu.joltt.Fragments.PublicPost

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.joltt.Callback.IPostCallback
import com.hindu.joltt.Model.PostModel

class PublicPostFragementViewModel : ViewModel(), IPostCallback {
    private var postLiveData: MutableLiveData<List<PostModel>>? = null

    private val postLoadCallback: IPostCallback = this
    private var messageError: MutableLiveData<String>? = null

    val postModel: MutableLiveData<List<PostModel>>?
        get() {
            if (postLiveData == null){
                postLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadPost()
            }
            val mutableLiveData = postLiveData
            return mutableLiveData
        }
    private fun loadPost() {
        val postList=ArrayList<PostModel>()
        val dataReference = FirebaseDatabase.getInstance().reference.child("Post")
        dataReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children){
                    val postModel = snapshot.getValue<PostModel>(PostModel::class.java) as PostModel
                    if (postModel.public){
                        postList.add(postModel)
                    }
                    postList.reverse()
                }
                postLoadCallback.onPostPCallbackLoadSuccess(postList)
            }

            override fun onCancelled(error: DatabaseError) {
                postLoadCallback.onPostCallbackLoadFailed(error.message)
            }

        })
    }

    override fun onPostCallbackLoadFailed(str: String) {
        val mutableLiveData = messageError
        mutableLiveData!!.value = str
    }

    override fun onPostPCallbackLoadSuccess(list: List<PostModel>) {
        val mutableLiveData = postLiveData
        mutableLiveData!!.value = list
    }
}