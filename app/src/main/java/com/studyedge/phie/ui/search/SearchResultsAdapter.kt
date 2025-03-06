package com.studyedge.phie.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.studyedge.phie.databinding.ItemSearchResultBinding

class SearchResultsAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private var courses = listOf<String>()

    fun updateCourses(newCourses: List<String>) {
        courses = newCourses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount() = courses.size

    inner class ViewHolder(
        private val binding: ItemSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(courses[adapterPosition])
                }
            }
        }

        fun bind(course: String) {
            binding.tvCourseName.text = course
            binding.tvCourseDescription.text = "Click to view course details"
        }
    }
} 