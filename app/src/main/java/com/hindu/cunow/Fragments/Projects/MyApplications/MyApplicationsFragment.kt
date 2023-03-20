package com.hindu.cunow.Fragments.Projects.MyApplications

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hindu.cunow.Adapter.MyAppAdapter
import com.hindu.cunow.Adapter.ProjectAdapter
import com.hindu.cunow.R
import com.hindu.cunow.databinding.FragmentMyApplicationsBinding
import com.hindu.cunow.databinding.FragmentMyProjectsBinding

class MyApplicationsFragment : Fragment() {

    var recyclerView:RecyclerView? = null
    private var myAppAdapter: MyAppAdapter? = null
    private var _binding:FragmentMyApplicationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MyApplicationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MyApplicationsViewModel::class.java)
        _binding = FragmentMyApplicationsBinding.inflate(inflater,container,false)
        val root:View = binding.root

        viewModel.myAppModel!!.observe(viewLifecycleOwner, Observer {
            initView(root)
            myAppAdapter = context?.let { it1-> MyAppAdapter(it1,it) }
            recyclerView!!.adapter = myAppAdapter
            myAppAdapter!!.notifyDataSetChanged()
        })

        return root
    }

    private fun initView(root: View) {
        recyclerView = root.findViewById(R.id.myAPP_RV) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        val linearLayoutManger = LinearLayoutManager(context)
        linearLayoutManger.reverseLayout = true
        linearLayoutManger.stackFromEnd = true
        recyclerView!!.layoutManager = linearLayoutManger
    }

}