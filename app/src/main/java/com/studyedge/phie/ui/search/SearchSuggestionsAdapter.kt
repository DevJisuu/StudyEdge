package com.studyedge.phie.ui.search

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import android.view.View
import android.widget.FilterQueryProvider
import android.widget.ImageView
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.studyedge.phie.R

class SearchSuggestionsAdapter(context: Context) : SimpleCursorAdapter(
    context,
    R.layout.item_search_suggestion,
    null,
    arrayOf("suggestion"),
    intArrayOf(R.id.tvSuggestion),
    0
) {
    companion object {
        private const val SUGGESTIONS_LIMIT = 5
        private const val COLUMN_SUGGESTION = "suggestion"
    }

    init {
        filterQueryProvider = FilterQueryProvider { constraint ->
            val cursor = MatrixCursor(arrayOf(BaseColumns._ID, COLUMN_SUGGESTION))
            try {
                if (!constraint.isNullOrBlank()) {
                    val suggestions = getSuggestions(constraint.toString())
                    suggestions.forEachIndexed { index, suggestion ->
                        cursor.addRow(arrayOf(index, suggestion))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cursor
        }
        
        // Set view binder to customize the view
        viewBinder = SimpleCursorAdapter.ViewBinder { view, cursor, columnIndex ->
            if (view.id == R.id.tvSuggestion) {
                val suggestion = cursor.getString(columnIndex)
                view.findViewById<ImageView>(R.id.ivSearchIcon)?.visibility = View.VISIBLE
                return@ViewBinder false // Let the adapter handle the text setting
            }
            false
        }
    }

    private fun getSuggestions(query: String): List<String> {
        if (query.isBlank()) return emptyList()

        // Get content from current UI
        val currentContent = listOf(
            "Continue Watching",
            "Pick up where you left off",
            "PW Courses",
            "NT Courses",
            "Start your journey to success",
            "Dashboard",
            "PhysicsWallah",
            "Next Toppers",
            "Apni Kaksha",
            "Telegram",
            "Network Stream",
            "Support"
        )
        
        return currentContent
            .filter { it.contains(query, ignoreCase = true) }
            .take(SUGGESTIONS_LIMIT)
    }

    override fun convertToString(cursor: Cursor?): String {
        return if (cursor == null) {
            ""
        } else {
            try {
                val columnIndex = cursor.getColumnIndex(COLUMN_SUGGESTION)
                if (columnIndex != -1) {
                    cursor.getString(columnIndex)
                } else {
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
} 