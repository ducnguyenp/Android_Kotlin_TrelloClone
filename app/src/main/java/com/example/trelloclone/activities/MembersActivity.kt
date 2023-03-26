package com.example.trelloclone.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.MemberItemAdapter
import com.example.trelloclone.databinding.ActivityMembersBinding
import com.example.trelloclone.firebase.FirebaseStore
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants

class MembersActivity : BaseActivity() {
    var binding: ActivityMembersBinding? = null
    private lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangesMade: Boolean = false

    lateinit var mBoardDetail: Board
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setUpToolBar()
        getExtra()
    }

    private fun getExtra() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetail = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseStore().getAssignedMembersListDetails(this, mBoardDetail.assignedTo)
        }
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding?.toolbarMembersActivity)
        supportActionBar?.title = "Members"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_icon)
        binding?.toolbarMembersActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setupMemberList(list: ArrayList<User>) {
        mAssignedMembersList = list
        hideProgressDialog()
        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this)
        binding?.rvMembersList?.setHasFixedSize(true)
        var adapter = MemberItemAdapter(this, list)
        binding?.rvMembersList?.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_add_member) {
            dialogAddMember()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogAddMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_member)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {
            var email = dialog.findViewById<EditText>(R.id.et_email_search_member)?.text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                FirebaseStore().getMemberDetails(this@MembersActivity, email)
            } else {
                Toast.makeText(this@MembersActivity, "Please input email", Toast.LENGTH_LONG).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun memberDetails(user: User) {
        mBoardDetail.assignedTo.add(user.id)
        FirebaseStore().assignMemberToBoard(this@MembersActivity, mBoardDetail, user)
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        anyChangesMade = true
        mAssignedMembersList.add(user)
        setupMemberList(mAssignedMembersList)
    }

    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
}