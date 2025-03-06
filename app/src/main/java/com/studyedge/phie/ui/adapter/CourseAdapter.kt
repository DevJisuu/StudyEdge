package com.studyedge.phie.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.studyedge.phie.R
import com.studyedge.phie.data.model.Course
import com.studyedge.phie.databinding.ItemCourseBinding

class CourseAdapter(
    private val onCourseClick: (Course) -> Unit,
    private val onFavoriteClick: (Course) -> Unit,
    private val onStudyClick: (Course) -> Unit,
    private val isFavorite: (Int) -> Boolean
) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = getItem(position)
        holder.bind(course)
        
        // Apply animation to new items
        if (position > itemCount - 5) {
            holder.itemView.startAnimation(
                android.view.animation.AnimationUtils.loadAnimation(
                    holder.itemView.context,
                    R.anim.item_animation_slide_up
                )
            )
        }
    }

    inner class CourseViewHolder(
        private val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCourseClick(getItem(adapterPosition))
                }
            }

            binding.favoriteButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val course = getItem(adapterPosition)
                    onFavoriteClick(course)
                    updateFavoriteState(course.id)
                }
            }
            
            binding.studyButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onStudyClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(course: Course) {
            binding.apply {
                courseTitle.text = course.name
                courseDescription.text = course.description
                languageTag.text = course.language ?: "N/A"
                courseExam.text = course.exam

                Glide.with(root.context)
                    .load(course.photo)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.bg_rounded_image)
                    .error(R.drawable.ic_pw_courses)
                    .into(courseThumbnail)

                updateFavoriteState(course.id)
                
                // Set batch ID as tag for the study button
                studyButton.tag = course.batchId
                
                // Apply pulse animation to the study button
                val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(
                    root.context,
                    R.anim.pulse_animation
                )
                studyButton.startAnimation(pulseAnimation)
            }
        }

        private fun updateFavoriteState(courseId: Int) {
            binding.favoriteButton.apply {
                isSelected = isFavorite(courseId)
            }
        }
    }

    class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem == newItem
        }
    }
} 