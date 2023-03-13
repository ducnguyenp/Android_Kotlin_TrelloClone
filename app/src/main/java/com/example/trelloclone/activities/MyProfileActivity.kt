package com.example.trelloclone.activities

import android.os.Bundle
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityMyProfileBinding

class MyProfileActivity : BaseActivity() {
    var binding: ActivityMyProfileBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpNavigationBar()
    }

    private fun setUpNavigationBar() {
        setSupportActionBar(binding?.toolbarMyProfileActivity)
        binding?.toolbarMyProfileActivity?.setTitle(R.string.my_profile)
        binding?.toolbarMyProfileActivity?.setNavigationIcon(R.drawable.ic_back_icon)
        binding?.toolbarMyProfileActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

}