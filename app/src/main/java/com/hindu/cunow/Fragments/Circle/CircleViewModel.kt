package com.hindu.cunow.Fragments.Circle

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.hindu.cunow.Adapter.CircleAdapter
import com.hindu.cunow.Callback.ICircleDisplayCallback
import com.hindu.cunow.Model.CircleModel
import com.hindu.cunow.databinding.CircleFragmentBinding
import java.util.*
import kotlin.collections.ArrayList

class CircleViewModel : ViewModel(), ICircleDisplayCallback {


    private var circleDisplayLiveData:MutableLiveData<List<CircleModel>>? = null

    private val circleDisplayCallback: ICircleDisplayCallback = this
    private var messageError:MutableLiveData<String>? = null

    val circleViewModel:MutableLiveData<List<CircleModel>>?
    get() {
        if (circleDisplayLiveData == null){
            circleDisplayLiveData = MutableLiveData()
            messageError = MutableLiveData()
            loadCircles()
        }
        val mutableLiveData = circleDisplayLiveData
        return mutableLiveData
    }

    private fun loadCircles() {
        val circleList = ArrayList<CircleModel>()
        val database = FirebaseDatabase.getInstance().reference.child("Circle")
        database.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snapshot in snapshot.children){
                    val dataModel = snapshot.getValue(CircleModel::class.java)
                    circleList.add(dataModel!!)
                }
                circleList.reverse()
                circleDisplayCallback.onCircleDisplayLoadSuccess(circleList)
            }

            override fun onCancelled(error: DatabaseError) {
                circleDisplayCallback.onCircleDisplayLoadFailed(error.message)
            }

        })
    }

    override fun onCircleDisplayLoadFailed(str: String) {
        val mutableLiveData = messageError
        mutableLiveData!!.value = str
    }

    override fun onCircleDisplayLoadSuccess(list: List<CircleModel>) {
        val mutableLiveData = circleDisplayLiveData
        mutableLiveData!!.value = list
    }


}