package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.TaskListItemAdapter
import com.example.trelloclone.databinding.ActivityTaskListBinding
import com.example.trelloclone.firebase.FirebaseStore
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.Task
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants

class TaskListActivity : BaseActivity() {
    private var binding: ActivityTaskListBinding? = null
    private lateinit var mBoardDetail: Board
    lateinit var mAssignedMemberDetaiList: ArrayList<User>

    var documentId: String = ""

    companion object {
        const val MEMBER_REQUEST_CODE = 13
        const val CARD_DETAIL_REQUEST_CODE = 14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getIntentFromActivity()
        Log.i("Life circle", "OnCreate")
    }

    private fun getIntentFromActivity() {
        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            documentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseStore().getBoardDetail(this, documentId)
    }

    private fun onSetupToolBar() {
        setSupportActionBar(binding?.toolbarTaskList)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = mBoardDetail.name
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_icon)
        binding?.toolbarTaskList?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun getBoardDetailSuccess(board: Board) {
        mBoardDetail = board
        hideProgressDialog()
        onSetupToolBar()

        showSuccessSnackBar(resources.getString(R.string.please_wait))
        FirebaseStore().getAssignedMembersListDetails(this, mBoardDetail.assignedTo)
    }

    fun addUpdateTasklistSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString((R.string.please_wait)))
        FirebaseStore().getBoardDetail(this, mBoardDetail.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirebaseStore().getCurrentUserId())
        mBoardDetail.taskList.add(0, task)
        mBoardDetail.taskList.removeAt(mBoardDetail.taskList.size - 1)

        showProgressDialog(resources.getString((R.string.please_wait)))
        FirebaseStore().addUpdateTaskList(this, mBoardDetail)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        mBoardDetail.taskList[position] = task
        mBoardDetail.taskList.removeAt(mBoardDetail.taskList.size - 1)
        showProgressDialog(resources.getString((R.string.please_wait)))
        FirebaseStore().addUpdateTaskList(this, mBoardDetail)
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {
        mBoardDetail.taskList.removeAt(mBoardDetail.taskList.size - 1)
        mBoardDetail.taskList[taskListPosition].cardList = cards
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseStore().addUpdateTaskList(this@TaskListActivity, mBoardDetail)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetail.taskList.removeAt(position)
        mBoardDetail.taskList.removeAt(mBoardDetail.taskList.size - 1)
        showProgressDialog(resources.getString((R.string.please_wait)))
        FirebaseStore().addUpdateTaskList(this, mBoardDetail)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetail.taskList.removeAt(mBoardDetail.taskList.size - 1)
        var cardAssignedUser = ArrayList<String>()
        cardAssignedUser.add(FirebaseStore().getCurrentUserId())
        var card = Card(cardName, FirebaseStore().getCurrentUserId(), cardAssignedUser)

        var cardList = mBoardDetail.taskList[position].cardList
        cardList.add(card)

        var task = Task(
            mBoardDetail.taskList[position].title,
            mBoardDetail.taskList[position].createdBy,
            cardList
        )
        mBoardDetail.taskList[position] = task
        showProgressDialog(resources.getString((R.string.please_wait)))
        FirebaseStore().addUpdateTaskList(this, mBoardDetail)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_member) {
            var intent = Intent(this@TaskListActivity, MembersActivity::class.java)
            intent.putExtra(Constants.BOARD_DETAIL, mBoardDetail)
            startActivityForResult(intent, MEMBER_REQUEST_CODE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CARD_DETAIL_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseStore().getBoardDetail(this, documentId)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBER_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseStore().getBoardDetail(this, documentId)
        }
    }

    fun boardMemberDetailList(list: ArrayList<User>) {
        mAssignedMemberDetaiList = list
        hideProgressDialog()


        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetail.taskList.add(addTaskList)

        binding?.rvTaskList?.layoutManager =
            LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)

        val adapter = TaskListItemAdapter(this@TaskListActivity, mBoardDetail.taskList)
        binding?.rvTaskList?.adapter = adapter

    }

    fun cardDetail(taskListPosition: Int, cardPosition: Int) {
        var intent = Intent(this, CardDetailActivity::class.java)
        intent.putExtra(Constants.CARD_POSITION, cardPosition)
        intent.putExtra(Constants.TASK_LIST_POSITION, taskListPosition)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetail)
        intent.putExtra(Constants.BOARD_MEMBER_LIST, mAssignedMemberDetaiList)
        startActivityForResult(intent, CARD_DETAIL_REQUEST_CODE)
    }
}