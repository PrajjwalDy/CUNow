package com.hindu.joltt.Fragments.Pages.MyPages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hindu.cunow.R
import com.hindu.cunow.databinding.MyPagesFragmentBinding
import com.hindu.joltt.Adapter.PageAdapter
import com.hindu.joltt.Model.PageModel
import kotlinx.android.synthetic.main.my_pages_fragment.myPages_RV
import kotlinx.android.synthetic.main.my_pages_fragment.no_page_txt

class    MyPagesFragment : Fragment() {

    var recyclerView:RecyclerView? = null
    private var pageAdapter: PageAdapter? = null

    private lateinit var viewModel: MyPagesViewModel
    private var _binding:MyPagesFragmentBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =ViewModelProvider(this).get(MyPagesViewModel::class.java)
        _binding = MyPagesFragmentBinding.inflate(inflater,container,false)
        val root:View = binding!!.root
        check()
        viewModel.myPageViewModel!!.observe(viewLifecycleOwner, Observer {
            initView(root)
            pageAdapter = context?.let { it1-> PageAdapter(it1,it) }
            recyclerView!!.adapter = pageAdapter
            pageAdapter!!.notifyDataSetChanged()
        })

        return root
    }

    private fun initView(root: View) {
        recyclerView = root.findViewById(R.id.myPages_RV) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        val linearLayoutManager:LinearLayoutManager= GridLayoutManager(context,2)
        recyclerView!!.layoutManager = linearLayoutManager
    }

    private fun check(){
        val data = FirebaseDatabase
            .getInstance()
            .reference
            .child("Pages")
        data.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children){
                    val dataModel = snapshot.getValue(PageModel::class.java)
                    if (dataModel!!.pageAdmin == FirebaseAuth.getInstance().currentUser!!.uid){
                        no_page_txt.visibility = View.GONE
                        myPages_RV.visibility = View.VISIBLE
                    }else{
                        no_page_txt.visibility = View.VISIBLE
                        myPages_RV.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}