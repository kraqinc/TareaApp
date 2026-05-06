package com.tareaapp.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tareaapp.databinding.ItemGradeBinding
import com.tareaapp.databinding.ItemSectionHeaderBinding
import com.tareaapp.models.GradeSection

sealed class GradeItem {
    data class Header(val title: String) : GradeItem()
    data class GradeCard(val grado: Int)  : GradeItem()
}

class GradeListAdapter(
    private val onGradeClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<GradeItem>()

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_GRADE  = 1

        private val GRADE_COLORS = mapOf(
            1 to "#FF6B6B", 2 to "#FF8E53", 3 to "#FFC947",
            4 to "#47D16C", 5 to "#4ECDC4", 6 to "#45B7D1",
            7 to "#5C6BC0", 8 to "#AB47BC", 9 to "#EC407A"
        )
    }

    fun submitSections(sections: List<GradeSection>) {
        items.clear()
        for (section in sections) {
            items.add(GradeItem.Header(section.name))
            section.grades.forEach { items.add(GradeItem.GradeCard(it)) }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is GradeItem.Header    -> VIEW_TYPE_HEADER
        is GradeItem.GradeCard -> VIEW_TYPE_GRADE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                ItemSectionHeaderBinding.inflate(inflater, parent, false)
            )
            else -> GradeViewHolder(
                ItemGradeBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is GradeItem.Header    -> (holder as HeaderViewHolder).bind(item.title)
            is GradeItem.GradeCard -> (holder as GradeViewHolder).bind(item.grado)
        }
    }

    override fun getItemCount() = items.size

    inner class HeaderViewHolder(
        private val binding: ItemSectionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvSectionTitle.text = title
        }
    }

    inner class GradeViewHolder(
        private val binding: ItemGradeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(grado: Int) {
            binding.tvGrado.text   = "$grado°"
            binding.tvLabel.text   = "Grado $grado"
            val colorStr = GRADE_COLORS[grado] ?: "#5C6BC0"
            binding.cardGrade.setCardBackgroundColor(
                android.graphics.Color.parseColor(colorStr)
            )
            binding.root.setOnClickListener { onGradeClick(grado) }
        }
    }
}
