package com.hindu.joltt.Fragments.PublicPost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hindu.cunow.R
import com.hindu.cunow.databinding.PublicPostFragementFragmentBinding
import com.hindu.joltt.Adapter.PublicPostAdapter
import kotlinx.android.synthetic.main.public_post_fragement_fragment.view.explorerTxt
import kotlinx.android.synthetic.main.public_post_fragement_fragment.view.postBack

class PublicPostFragement : Fragment() {

    var recyclerViewGrid: RecyclerView? = null
    private var publicPostAdapter: PublicPostAdapter? = null

    private lateinit var viewModel: PublicPostFragementViewModel
    private var _binding:PublicPostFragementFragmentBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProvider(this).get(PublicPostFragementViewModel::class.java)

        _binding = PublicPostFragementFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel.postModel!!.observe(viewLifecycleOwner, Observer {
            initView2(root)
            publicPostAdapter = context?.let { it1-> PublicPostAdapter(it1,it) }
            recyclerViewGrid!!.adapter = publicPostAdapter
            publicPostAdapter!!.notifyDataSetChanged()
        })
        root.postBack.setOnClickListener {
            Navigation.findNavController(root)
                .navigate(R.id.action_publicPostFragement_to_navigation_dashboard)
        }
        root.explorerTxt.setOnClickListener {
            Navigation.findNavController(root)
                .navigate(R.id.action_publicPostFragement_to_navigation_dashboard)
        }

        return root
    }

    private fun initView2(root: View){
        recyclerViewGrid = root.findViewById(R.id.explorePost_rv) as RecyclerView
        recyclerViewGrid!!.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewGrid!!.layoutManager = linearLayoutManager
    }

}