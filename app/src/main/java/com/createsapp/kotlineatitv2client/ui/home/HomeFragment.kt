package com.createsapp.kotlineatitv2client.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asksira.loopingviewpager.LoopingViewPager
import com.createsapp.kotlineatitv2client.R
import com.createsapp.kotlineatitv2client.adapter.MyBestDealsAdapter
import com.createsapp.kotlineatitv2client.adapter.MyPopularCategoriesAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel


    var recyclerView: RecyclerView? = null
    var viewPager: LoopingViewPager? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        initView(root)

        //Bind Data
        homeViewModel.popularList.observe(this, Observer {
            val listData = it
            val adapter =  MyPopularCategoriesAdapter(context!!,listData)
            recyclerView!!.adapter = adapter
        })

        homeViewModel.bestDealList.observe(this, Observer {
            val adapter = MyBestDealsAdapter(context!!,it,false)
            viewPager!!.adapter = adapter
        })

        return root
    }

    private fun initView(root:View) {
        viewPager = root.findViewById(R.id.viewPager) as LoopingViewPager
        recyclerView = root.findViewById(R.id.recycler_popular) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun onResume() {
        super.onResume()
        viewPager!!.resumeAutoScroll()
    }

    override fun onPause() {
        super.onPause()
        viewPager!!.pauseAutoScroll()
    }

}