package com.hindu.joltt.Fragments.Pages

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hindu.cunow.R
import com.hindu.joltt.Activity.CreatePageActivity
import com.hindu.joltt.Adapter.PageTabAdapter
import kotlinx.android.synthetic.main.activity_pages_tab.CP_TV
import kotlinx.android.synthetic.main.activity_pages_tab.CP_img
import kotlinx.android.synthetic.main.activity_pages_tab.pageTxt
import kotlinx.android.synthetic.main.activity_pages_tab.pagesBack

class PagesTabActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pages_tab)

        val tabLayout = findViewById<TabLayout>(R.id.pages_tabs_layout)
        val viewPager2 = findViewById<ViewPager2>(R.id.pages_viewPager)

        val adapter = PageTabAdapter(supportFragmentManager,lifecycle)
        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout,viewPager2){tab,position->

            when(position){
                0->{
                    tab.text = "Explore Pages"
                }
                1->{
                    tab.text = "My Pages"
                }
            }

            pagesBack.setOnClickListener {
                finish()
            }
            pageTxt.setOnClickListener {
                finish()
            }

        }.attach()

        CP_img.setOnClickListener {
            val intent = Intent(this, CreatePageActivity::class.java)
            startActivity(intent)
        }

        CP_TV.setOnClickListener {
            val intent = Intent(this, CreatePageActivity::class.java)
            startActivity(intent)

            finish()

        }


    }
}