package com.studyedge.phie.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.studyedge.phie.databinding.FragmentFavoritesBinding
import com.studyedge.phie.ui.adapter.CourseAdapter
import com.studyedge.phie.ui.viewmodel.FavoritesViewModel

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        setupBackNavigation()
    }

    private fun setupUI() {
        courseAdapter = CourseAdapter(
            onCourseClick = { course ->
                // Handle course click
            },
            onFavoriteClick = { course ->
                viewModel.removeFavorite(course)
                Snackbar.make(binding.root, "Removed from favorites", Snackbar.LENGTH_SHORT).show()
            },
            onStudyClick = { course ->
                navigateToBatchDetails(course.batchId)
            },
            isFavorite = { courseId -> 
                viewModel.isFavorite(courseId)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = courseAdapter
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
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

    private fun observeViewModel() {
        viewModel.favorites.observe(viewLifecycleOwner) { courses ->
            courseAdapter.submitList(courses)
            binding.emptyState.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
} 