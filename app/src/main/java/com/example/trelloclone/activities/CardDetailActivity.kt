package com.example.trelloclone.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.CardMemberListItemsAdapter
import com.example.trelloclone.databinding.ActivityCardDetailBinding
import com.example.trelloclone.dialog.LabelColorListDialog
import com.example.trelloclone.dialog.MembersListDialog
import com.example.trelloclone.firebase.FirebaseStore
import com.example.trelloclone.models.*
import com.example.trelloclone.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailActivity : BaseActivity() {
    var binding: ActivityCardDetailBinding? = null
    private var mBoardDetails: Board? = null
    private var mTaskListPosition: Int? = null
    private var mCardPosition: Int? = null
    private var mSelectedColor: String = ""
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mDueDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getIntentData()
        onSetupActionBar()
        onUpdateCardDetail()
        setUpUI()
        setupSelectedMembersList()
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.CARD_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_POSITION, -1)
        }
        if (intent.hasExtra(Constants.TASK_LIST_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.BOARD_MEMBER_LIST)) {
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBER_LIST)!!
        }
        binding?.etNameCardDetails?.setText(mBoardDetails?.taskList!![mTaskListPosition!!]?.cardList!![mCardPosition!!].name)
        val color =
            mBoardDetails?.taskList!![mTaskListPosition!!]?.cardList!![mCardPosition!!].labelColor
        if (color.isNotEmpty()) {
            binding?.tvSelectLabelColor?.setBackgroundColor(
                Color.parseColor(color)
            )
            binding?.tvSelectLabelColor?.text = ""
            mSelectedColor = color

        }

        var dueDate = mBoardDetails?.taskList!![mTaskListPosition!!]?.cardList!![mCardPosition!!].dueDate
        if (dueDate > 0) {
            val simmpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simmpleDateFormat.format(Date(dueDate))
            binding?.tvSelectDueDate?.text = selectedDate
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
            mDueDate,
        )

        mBoardDetails?.taskList!![mTaskListPosition!!].cardList!![mCardPosition!!] = card
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
        binding?.tvSelectMembers?.setOnClickListener {
            membersListDialog()
        }
        binding?.tvSelectDueDate?.setOnClickListener{
            showDataPicker()
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

        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color)
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun membersListDialog() {
        val cardAssignedMembersList =
            mBoardDetails?.taskList!![mTaskListPosition!!]?.cardList!![mCardPosition!!].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in mMembersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this@CardDetailActivity,
            mMembersDetailList,
            resources.getString(R.string.select_members)
        ) {
            override fun onItemSelected(user: User, action: String) {
                Log.i("Action", action)
                if (action == Constants.SELECT) {
                    if (!mBoardDetails?.taskList!![mTaskListPosition!!].cardList[mCardPosition!!].assignedTo.contains(
                            user.id
                        )
                    ) {
                        mBoardDetails?.taskList!![mTaskListPosition!!].cardList[mCardPosition!!].assignedTo.add(
                            user.id
                        )
                    }
                } else {
                    mBoardDetails?.taskList!![mTaskListPosition!!].cardList[mCardPosition!!].assignedTo.remove(
                        user.id
                    )
                    for (i in mMembersDetailList.indices) {
                        if (mMembersDetailList[i].id == user.id) {
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun setColor() {
        binding?.tvSelectLabelColor?.text = ""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList =
            mBoardDetails?.taskList!![mTaskListPosition!!].cardList[mCardPosition!!].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )

                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("", ""))

            binding?.tvSelectMembers?.visibility = View.GONE
            binding?.rvSelectedMembersList?.visibility = View.VISIBLE

            binding?.rvSelectedMembersList?.layoutManager =
                GridLayoutManager(this@CardDetailActivity, 6)
            val adapter = CardMemberListItemsAdapter(this@CardDetailActivity, selectedMembersList, true)
            binding?.rvSelectedMembersList?.adapter = adapter
            adapter.setOnClickListener(object :
                CardMemberListItemsAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }
            })
        } else {
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.visibility = View.GONE
        }
    }


    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this,{ _, year, monthOfYear, dayOfMonth ->
                // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                val sMonthOfYear =if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                binding?.tvSelectDueDate!!.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mDueDate = theDate!!.time
            }, year, month, day
        )
        dpd.show()
    }
}