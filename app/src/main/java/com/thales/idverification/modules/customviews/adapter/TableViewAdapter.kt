package com.thales.idverification.modules.customviews.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.thales.idverification.R
import com.thales.idverification.modules.customviews.model.TableItemView


class TableViewAdapter(
    private val arrayList: List<TableItemView>,
    private val context: Context,
    private val tableData: List<HashMap<String, String?>>
) :
    RecyclerView.Adapter<TableViewAdapter.ViewHolder>() {

    private var tableItemOnClickListener: TableItemOnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.table_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = arrayList[position]

        holder.textView.apply {
            text = item.value

            if(item.isHeader) {
                setBackgroundColor(ContextCompat.getColor(context, R.color.table_header_color))
                typeface = Typeface.DEFAULT_BOLD
            } else {
                if(item.rowId % 2 == 0) {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.table_even_row_color))
                } else {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.table_odd_row_color))
                }
            }

            if (!item.isHeader)
                setOnClickListener {
                    tableItemOnClickListener?.onClickTableItem(
                        this,
                        item.rowId,
                        tableData[item.rowId]
                    )
                }
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView

        init {
            textView = itemView.findViewById(R.id.textView)
        }
    }

    fun setTableItemOnClickListener(onClickListener: TableItemOnClickListener) {
        tableItemOnClickListener = onClickListener
    }

    interface TableItemOnClickListener {
        fun onClickTableItem(textView: TextView, rowId: Int, tableRow: HashMap<String, String?>)
    }
}
