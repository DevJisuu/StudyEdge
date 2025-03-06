package com.studyedge.phie.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.studyedge.phie.R
import com.studyedge.phie.data.model.Course

class CourseSuggestionsAdapter(
    context: Context
) : ArrayAdapter<Course>(context, R.layout.item_search_suggestion), Filterable {
    
    private var courses = listOf<Course>()
    private var filteredCourses = listOf<Course>()
    
    fun updateSuggestions(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }
    
    override fun getCount(): Int = filteredCourses.size
    
    override fun getItem(position: Int): Course = filteredCourses[position]
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_search_suggestion, parent, false)
        
        val course = getItem(position)
        course?.let {
            // Set the course name
            view.findViewById<TextView>(R.id.tvSuggestion).text = it.name
            
            // Set the search icon tint to match the theme
            val searchIcon = view.findViewById<ImageView>(R.id.ivSearchIcon)
            searchIcon.setColorFilter(
                ContextCompat.getColor(context, R.color.gray),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
        
        return view
    }
    
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val results = FilterResults()
                
                filteredCourses = if (query.isEmpty()) {
                    emptyList()
                } else {
                    courses.filter { 
                        it.name.lowercase().contains(query) || 
                        it.description.lowercase().contains(query) 
                    }.take(5) // Limit to 5 suggestions
                }
                
                results.values = filteredCourses
                results.count = filteredCourses.size
                return results
            }
            
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
} 