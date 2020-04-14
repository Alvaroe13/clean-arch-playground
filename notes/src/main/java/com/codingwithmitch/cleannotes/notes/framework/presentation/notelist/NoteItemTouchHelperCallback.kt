package com.codingwithmitch.cleannotes.notes.framework.presentation.notelist

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class NoteItemTouchHelperCallback
constructor(
    private val itemTouchHelperAdapter: ItemTouchHelperAdapter
): ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(
            ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.START or ItemTouchHelper.END
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        itemTouchHelperAdapter.onItemSwiped(viewHolder.adapterPosition)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

}


interface ItemTouchHelperAdapter{

    fun onItemSwiped(position: Int)
}














