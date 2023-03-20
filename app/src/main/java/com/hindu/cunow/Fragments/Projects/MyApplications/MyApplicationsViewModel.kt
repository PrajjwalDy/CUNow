package com.hindu.cunow.Fragments.Projects.MyApplications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.cunow.Callback.IApplicationCallback
import com.hindu.cunow.Callback.IClubsCallback
import com.hindu.cunow.Model.ClubModel
import com.hindu.cunow.Model.MyAppModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplicationsViewModel : ViewModel(), IApplicationCallback {
    private var myAppLiveData: MutableLiveData<List<MyAppModel>>? = null
    private val appCallback: IApplicationCallback = this
    private var messageError: MutableLiveData<String>? = null

    val myAppModel:MutableLiveData<List<MyAppModel>>?
    get() {
        if (myAppLiveData == null){
            myAppLiveData = MutableLiveData()
            messageError = MutableLiveData()
            messageError = MutableLiveData()
            CoroutineScope(Dispatchers.IO).launch {
                loadData()
            }

        }
        return myAppLiveData
    }

    private fun loadData(){
        val appList = ArrayList<MyAppModel>()
        val data = FirebaseDatabase.getInstance().reference.child("ProjectApplications")
        data.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                appList.clear()
                for (snapshot in snapshot.children){
                    val data = snapshot.getValue((MyAppModel::class.java))
                    appList.add(data!!)
                }
                appCallback.onAbroadListLoadSuccess(appList)
            }

            override fun onCancelled(error: DatabaseError) {
                appCallback.onAbroadListLoadFailed(error.message)
            }

        })
    }

    override fun onAbroadListLoadFailed(str: String) {
        val mutableLiveData = messageError
        mutableLiveData!!.value = str
    }

    override fun onAbroadListLoadSuccess(list: List<MyAppModel>) {
        val mutableLiveData = myAppLiveData
        mutableLiveData!!.value = list
    }
}