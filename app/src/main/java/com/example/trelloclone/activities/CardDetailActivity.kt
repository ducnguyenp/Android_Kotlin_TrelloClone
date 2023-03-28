package com.example.trelloclone.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityCardDetailBinding
import com.example.trelloclone.dialog.LabelColorListDialog
import com.example.trelloclone.firebase.FirebaseStore
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.Task
import com.example.trelloclone.utils.Constants

class CardDetailActivity : BaseActivity() {
    var binding: ActivityCardDetailBinding? = null
    private var mBoardDetails: Board? = null
    private var mTaskListPosition: Int? = null
    private var mCardPosition: Int? = null
    private var mSelectedColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getIntentData()
        onSetupActionBar()
        onUpdateCardDetail()
        setUpUI()
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.CARD_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_POSITION, -1)
        }
        if (intent.hasExtra(Constants.TASK_LIST_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_POSITION, -1)
        }
        if (intent.hasExtra(Constants.TASK_LIST_POSITION)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        binding?.etNameCardDetails?.setText(mBoardDetails?.taskList!![mTaskListPosition!!]?.cardList!![mCardPosition!!].name)
        val color =
            mBoardDetails?.taskList!![mTaskListPosition!!]?.cardList!![mCardPosition!!].labelColor
        if (color.isNotEmpty()) {
            binding?.tvSelectLabelColor?.setBackgroundColor(
                Color.parseColor(color)
            )
            binding?.tvSelectLabelColor?.text = ""

        }
    }

    private fun onSetupActionBar() {
        setSupportActionBar(binding?.toolbarCardDetailsActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_icon)
        supportActionBar?.title =
            mBoardDetails?.taskList!![mTaskListPosition!!]?.cardList!![mCardPosition!!].name
        binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_delete_card) {
            deleteCard()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onUpdateCardDetail() {
        binding?.btnUpdateCardDetails?.setOnClickListener {
            updateCardDetails()
        }
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = mBoardDetails?.taskList!![mTaskListPosition!!].cardList
        cardsList.removeAt(mCardPosition!!)

        val taskList: ArrayList<Task> = mBoardDetails?.taskList!!
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition!!].cardList = cardsList
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseStore().addUpdateTaskList(this@CardDetailActivity, mBoardDetails!!)
    }

    private fun updateCardDetails() {
        val card = Card(
            binding?.etNameCardDetails?.text.toString(),
            mBoardDetails?.taskList!![mTaskListPosition!!].cardList!![mCardPosition!!].createdBy,
            mBoardDetails?.taskList!![mTaskListPosition!!].cardList!![mCardPosition!!].assignedTo,
            mSelectedColor,
        )

        mBoardDetails?.taskList!![mTaskListPosition!!].cardList!![mCardPosition!!] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        mBoardDetails?.taskList!!.removeAt(mBoardDetails?.taskList!!.size - 1)
        FirebaseStore().addUpdateTaskList(this@CardDetailActivity, mBoardDetails!!)
    }

    fun addUpdateTasklistSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun setUpUI() {
        binding?.tvSelectLabelColor?.setOnClickListener {
            labelColorsListDialog()
        }
    }

    private fun colorsList(): ArrayList<String> {

        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun labelColorsListDialog() {
        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object : LabelColorListDialog(this, colorsList, resources.getString(R.string.str_select_label_color)) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setColor() {
        binding?.tvSelectLabelColor?.text = ""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }
}