package com.hindu.joltt.Fragments.Courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hindu.cunow.R
import com.hindu.cunow.databinding.FragmentCoursesBinding
import com.hindu.joltt.Adapter.CourseAdapter
import kotlinx.android.synthetic.main.fragment_courses.view.courseBack
import kotlinx.android.synthetic.main.fragment_courses.view.courseTxt

class CoursesFragment : Fragment() {
    var recyclerView: RecyclerView? = null
    private var courseAdapter: CourseAdapter? = null
    private lateinit var viewModel: CoursesViewModel
    private var _binding:FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(CoursesViewModel::class.java)
        _binding = FragmentCoursesBinding.inflate(inflater,container,false)
        val root:View = binding.root

        viewModel.courseModel!!.observe(viewLifecycleOwner, Observer {
            initView(root)
            courseAdapter = context?.let { it1-> CourseAdapter(it1,it) }
            recyclerView!!.adapter = courseAdapter
            courseAdapter!!.notifyDataSetChanged()
        })

        root.courseBack.setOnClickListener {
            Navigation.findNavController(root)
                .navigate(R.id.action_coursesFragment_to_navigation_dashboard)
        }
        root.courseTxt.setOnClickListener {
            Navigation.findNavController(root)
                .navigate(R.id.action_coursesFragment_to_navigation_dashboard)
        }

        return root
    }

    private fun initView(root: View) {
        recyclerView = root.findViewById(R.id.coursesRV) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        val linearLayoutManger = LinearLayoutManager(context)
        linearLayoutManger.reverseLayout = true
        linearLayoutManger.stackFromEnd = true
        recyclerView!!.layoutManager = linearLayoutManger
    }

}