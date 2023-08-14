package com.hindu.joltt.Fragments.Pages.MyPages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.joltt.Callback.IPageLoadCallback
import com.hindu.joltt.Model.PageModel

class MyPagesViewModel : ViewModel(), IPageLoadCallback {
    private var pageLiveData: MutableLiveData<List<PageModel>>? = null
    private val paeCallback: IPageLoadCallback = this
    private var messageError: MutableLiveData<String>? = null

    val myPageViewModel:MutableLiveData<List<PageModel>>?
        get() {
            if (pageLiveData == null){
                pageLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadPage()
            }
            val mutableLiveData = pageLiveData
            return mutableLiveData
        }

    private fun loadPage() {
        val pageList = ArrayList<PageModel>()
        val data = FirebaseDatabase.getInstance().reference.child("Pages")
        data.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children){
                    val dataModel = snapshot.getValue(PageModel::class.java)
                    if (dataModel!!.pageAdmin == FirebaseAuth.getInstance().currentUser!!.uid){
                        pageList.add(dataModel)
                    }
                }
                paeCallback.onPageLoadSuccess(pageList)
            }

            override fun onCancelled(error: DatabaseError) {
                paeCallback.onPageLoadFailed(error.message)
            }

        })
    }

    override fun onPageLoadFailed(str: String) {
        val mutableLiveData = messageError
        mutableLiveData!!.value = str
    }

    override fun onPageLoadSuccess(list: List<PageModel>) {
        val mutableLiveData = pageLiveData
        mutableLiveData!!.value = list
    }


}