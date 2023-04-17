package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.adapters.BoardItemAdapter
import com.example.trelloclone.databinding.ActivityMainBinding
import com.example.trelloclone.firebase.FirebaseStore
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        const val MY_PROFILE_REQUEST: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private var binding: ActivityMainBinding? = null
    private var mUserName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        binding?.navView?.setNavigationItemSelectedListener(this)
        FirebaseStore().signInUser(this, true)
        binding?.appBarComponent?.fabCreateBoard?.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
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


    fun updateNavigationUserDetail(user: User, readBoardList: Boolean) {
        mUserName = user.name
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

        if (readBoardList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseStore().getBoardList(this)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirebaseStore().getBoardList(this)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST) {
            FirebaseStore().signInUser(this)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                var intent = Intent(this, MyProfileActivity::class.java)
                startActivityForResult(intent, MY_PROFILE_REQUEST)
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

    fun populateBoardListToUI (boardList: ArrayList<Board>) {
        hideProgressDialog()
        var rvBoardsList: RecyclerView? = binding?.appBarComponent?.mainContent?.rvBoardsList
        if (boardList.size > 0) {
            rvBoardsList?.visibility = View.VISIBLE
            binding?.appBarComponent?.mainContent?.tvNoBoardsAvailable?.visibility = View.INVISIBLE

            rvBoardsList?.layoutManager = LinearLayoutManager(this)
            rvBoardsList?.setHasFixedSize(true)

            var adapter = BoardItemAdapter(this, boardList)
            rvBoardsList?.adapter = adapter

            adapter.setOnClickListener(object: BoardItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        } else {
            rvBoardsList?.visibility = View.INVISIBLE
            binding?.appBarComponent?.mainContent?.tvNoBoardsAvailable?.visibility = View.VISIBLE
        }
    }
}