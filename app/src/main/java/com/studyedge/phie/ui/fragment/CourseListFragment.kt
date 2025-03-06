package com.studyedge.phie.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.studyedge.phie.databinding.FragmentCourseListBinding
import com.studyedge.phie.ui.adapter.CourseAdapter
import com.studyedge.phie.ui.search.CourseSuggestionsAdapter
import com.studyedge.phie.ui.viewmodel.CourseViewModel
import com.studyedge.phie.ui.viewmodel.FavoritesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CourseListFragment : Fragment() {
    private var _binding: FragmentCourseListBinding? = null
    private val binding get() = _binding!!
    private val courseViewModel: CourseViewModel by viewModels()
    private val favoritesViewModel: FavoritesViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var suggestionsAdapter: CourseSuggestionsAdapter
    private var searchJob: Job? = null
    private var isLoading = false
    private var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSuggestionsAdapter()
        setupUI()
        setupBackNavigation()
        observeViewModel()
        courseViewModel.loadCourses()
    }

    private fun setupUI() {
        courseAdapter = CourseAdapter(
            onCourseClick = { course ->
                // Handle course click
            },
            onFavoriteClick = { course ->
                if (favoritesViewModel.isFavorite(course.id)) {
                    favoritesViewModel.removeFavorite(course)
                    Snackbar.make(binding.root, "Removed from favorites", Snackbar.LENGTH_SHORT).show()
                } else {
                    favoritesViewModel.addFavorite(course)
                    Snackbar.make(binding.root, "Added to favorites", Snackbar.LENGTH_SHORT).show()
                }
            },
            onStudyClick = { course ->
                navigateToBatchDetails(course.batchId)
            },
            isFavorite = { courseId ->
                favoritesViewModel.isFavorite(courseId)
            }
        )

        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.apply {
            this.layoutManager = layoutManager
            adapter = courseAdapter
            visibility = View.VISIBLE
            
            // Add layout animation for items
            layoutAnimation = android.view.animation.AnimationUtils.loadLayoutAnimation(
                context, 
                com.studyedge.phie.R.anim.layout_animation_from_bottom
            )
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    if (dy > 0) { // Scrolling down
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                        
                        // Trigger loading earlier - when user is 5 items away from the end
                        val isNearEnd = lastVisibleItemPosition >= totalItemCount - 5
                        
                        if (!isLoading && !isLastPage && isNearEnd && totalItemCount >= 3) {
                            android.util.Log.d("CourseListFragment", "User approaching end of list, loading more courses")
                            loadMoreCourses()
                        }
                    }
                }
            })
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupSearch()
    }

    private fun loadMoreCourses() {
        isLoading = true
        
        // Show loading animation
        binding.loadMoreProgress.visibility = View.VISIBLE
        val animation = android.view.animation.AnimationUtils.loadAnimation(
            context,
            com.studyedge.phie.R.anim.fade_in_up
        )
        binding.loadMoreProgress.startAnimation(animation)
        
        android.util.Log.d("CourseListFragment", "Loading more courses, page: ${courseViewModel.getCurrentPage() + 1}")
        courseViewModel.loadMoreCourses()
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (parentFragmentManager.backStackEntryCount > 0) {
                        parentFragmentManager.popBackStack()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun setupSuggestionsAdapter() {
        suggestionsAdapter = CourseSuggestionsAdapter(requireContext())
        (binding.searchView as AutoCompleteTextView).setAdapter(suggestionsAdapter)
        
        binding.searchView.setOnItemClickListener { _, _, position, _ ->
            val selectedCourse = suggestionsAdapter.getItem(position)
            selectedCourse?.let {
                // Handle course selection
                binding.searchView.setText(it.name)
                binding.searchView.clearFocus()
                
                // Filter to show only this course
                courseViewModel.searchCourses(it.name)
            }
        }
        
        binding.clearSearch.setOnClickListener {
            binding.searchView.text.clear()
            binding.clearSearch.visibility = View.GONE
            courseViewModel.loadCourses()
        }
    }

    private fun setupSearch() {
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearSearch.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    val query = s?.toString()
                    if (!query.isNullOrBlank() && query.length >= 2) {
                        courseViewModel.searchCourses(query)
                    } else if (query.isNullOrBlank()) {
                        courseViewModel.loadCourses()
                    }
                }
            }
        })
        
        binding.searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchView.text.toString()
                if (query.isNotBlank()) {
                    courseViewModel.searchCourses(query)
                    binding.searchView.clearFocus()
                    return@setOnEditorActionListener true
                }
            }
            false
        }
    }

    private fun navigateToBatchDetails(batchId: String) {
        // For now, just show a snackbar with the batch ID
        Snackbar.make(binding.root, "Opening batch: $batchId", Snackbar.LENGTH_SHORT).show()
        
        // TODO: Navigate to batch details page
        // val intent = Intent(requireContext(), BatchDetailsActivity::class.java)
        // intent.putExtra("BATCH_ID", batchId)
        // startActivity(intent)
    }

    private fun observeViewModel() {
        courseViewModel.courses.observe(viewLifecycleOwner) { courses ->
            // Create a new list to force update
            courseAdapter.submitList(ArrayList(courses))
            
            // Apply animation to recycler view
            if (courseAdapter.itemCount <= 10) {
                // Only apply full layout animation for initial load
                binding.recyclerView.scheduleLayoutAnimation()
            }
            
            suggestionsAdapter.updateSuggestions(courses)
            binding.shimmerLayout.visibility = View.GONE
            binding.recyclerView.visibility = if (courses.isEmpty()) View.GONE else View.VISIBLE
            
            // Hide loading with animation
            if (binding.loadMoreProgress.visibility == View.VISIBLE) {
                val animation = android.view.animation.AnimationUtils.loadAnimation(
                    context,
                    com.studyedge.phie.R.anim.fade_out_down
                )
                animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        binding.loadMoreProgress.visibility = View.GONE
                    }
                })
                binding.loadMoreProgress.startAnimation(animation)
            }
            
            isLoading = false
            
            android.util.Log.d("CourseListFragment", "Updated courses list, count: ${courses.size}")
        }

        courseViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading && courseAdapter.itemCount == 0) {
                binding.shimmerLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else if (loading) {
                binding.loadMoreProgress.visibility = View.VISIBLE
            } else {
                binding.shimmerLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.loadMoreProgress.visibility = View.GONE
                isLoading = false
            }
        }

        courseViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                binding.shimmerLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.loadMoreProgress.visibility = View.GONE
                isLoading = false
            }
        }
        
        courseViewModel.isLastPage.observe(viewLifecycleOwner) { lastPage ->
            isLastPage = lastPage
            if (lastPage) {
                binding.loadMoreProgress.visibility = View.GONE
                android.util.Log.d("CourseListFragment", "Reached last page, no more courses to load")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CourseListFragment()
    }
} 