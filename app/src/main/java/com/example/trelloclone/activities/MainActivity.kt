package com.example.trelloclone.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityMainBinding
import com.example.trelloclone.firebase.FirebaseStore
import com.example.trelloclone.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        binding?.navView?.setNavigationItemSelectedListener(this)
        FirebaseStore().signInUser(this)
    }

    private fun setupActionBar() {
        var toolBar = binding?.appBarComponent?.toolbarMainActivity
        setSupportActionBar(toolBar)
        toolBar?.setNavigationIcon(R.drawable.ic_menu_drawer)
        toolBar?.setNavigationOnClickListener {
            toggleMenuDrawer()
        }
    }

    private fun toggleMenuDrawer() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }


    fun updateNavigationUserDetail(user: User) {
        val userNameImageView = binding?.navView?.getHeaderView(0)
            ?.findViewById<ImageView>(R.id.iv_user_image)
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_holder)
            .into(userNameImageView!!)

        val userNameTextView = binding?.navView?.getHeaderView(0)
            ?.findViewById<TextView>(R.id.tv_username)
        userNameTextView?.text = user.name
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                var intent = Intent(this, MyProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}