package com.example.trelloclone.adapters

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.Task
import java.util.Collections

class TaskListItemAdapter(private val context: Context, private var list: ArrayList<Task>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPositionDraggerFrom = -1
    private var mPositionDraggerTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        view.layoutParams = layoutParams
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            if (position == list.size - 1) {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility =
                    View.VISIBLE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.GONE
            } else {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility =
                    View.VISIBLE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text = model.title

            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility =
                    View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility =
                    View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility =
                    View.GONE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).setOnClickListener {
                val listName =
                    holder.itemView.findViewById<EditText>(R.id.et_task_list_name).text.toString()
                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }
            holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name).setOnClickListener {
                holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name)
                    .setText(model.title)
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility =
                    View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility =
                    View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view)
                .setOnClickListener {
                    holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility =
                        View.VISIBLE
                    holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility =
                        View.GONE
                }

            holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name)
                .setOnClickListener {
                    val listName =
                        holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).text.toString()
                    if (listName.isNotEmpty()) {
                        if (context is TaskListActivity) {
                            context.updateTaskList(position, listName, model)
                        }
                    } else {
                        Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility =
                        View.VISIBLE
                    holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility =
                        View.GONE
                }

            holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list).setOnClickListener {
                if (context is TaskListActivity) {
                    context.deleteTaskList(position)
                }
            }
            onAddCard(holder, position)
            onShowCardList(holder, model.cardList, position)
        }
    }

    private fun onShowCardList(
        holder: RecyclerView.ViewHolder,
        card: ArrayList<Card>,
        taskPosition: Int
    ) {
        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).layoutManager =
            LinearLayoutManager(context)
        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).setHasFixedSize(true)

        var adapter = CardListItemsAdapter(context, card)
        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter = adapter
        adapter.setOnClickListener(object : CardListItemsAdapter.OnClickListener {
            override fun onClick(cardPosition: Int) {
                if (context is TaskListActivity) {
                    context.cardDetail(taskPosition, cardPosition)
                }
            }
        })

        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).addItemDecoration(dividerItemDecoration)

        val helper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, dragger: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val draggedPosition = dragger.adapterPosition
                val targetPosition = target.adapterPosition

                if (mPositionDraggerFrom == -1) {
                    mPositionDraggerFrom = draggedPosition
                }
                mPositionDraggerTo = targetPosition
                Collections.swap(list[taskPosition].cardList, draggedPosition, targetPosition)
                adapter.notifyItemMoved(draggedPosition, targetPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (mPositionDraggerFrom != -1 && mPositionDraggerTo != -1 && mPositionDraggerFrom != mPositionDraggerTo) {
                    (context as TaskListActivity)!!.updateCardsInTaskList(taskPosition, list[taskPosition].cardList)
                }
                mPositionDraggerTo = -1
                mPositionDraggerFrom = -1
            }
        })
        helper.attachToRecyclerView(holder.itemView.findViewById(R.id.rv_card_list))
    }

    private fun onAddCard(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tv_add_card).setOnClickListener {

            holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.GONE
            holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.VISIBLE

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility =
                    View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.GONE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {
                val cardName =
                    holder.itemView.findViewById<TextView>(R.id.et_card_name).text.toString()
                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position, cardName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Card Detail.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    // Todo: get the density of the screen and turn it into Integer: To see how big the pixel density of the screen is
    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}