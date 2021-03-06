package com.repo01.repoapp.ui.search

import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.repo01.repoapp.R
import com.repo01.repoapp.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val adapter by lazy { SearchItemAdapter() }
    private val viewModel: SearchViewModel by viewModels()
    private val inputMethodManager by lazy {
        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        initViews()
        observeData()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.clSearch.setOnClickListener {
            binding.clSearch.requestFocus()
        }
        initSearchEditText()
        initRecyclerView()
        setLoadStateListener()
    }

    private fun setLoadStateListener() {
        adapter.addLoadStateListener { loadState ->
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
        }
    }

    private fun initSearchEditText() {
        with(binding.etSearch) {
            setOnFocusChangeListener { _, hasFocus ->
                setCompoundDrawablesWithIntrinsicBounds(
                    if (hasFocus.not()) R.drawable.ic_search else 0,
                    0,
                    if (hasFocus) R.drawable.ic_variant10 else 0,
                    0
                )
                if (hasFocus.not()) {
                    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
                }
            }
            setOnTouchListener { _, event ->
                performClick()
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (this.right - this.compoundDrawables[2].bounds.width() - this.compoundDrawablePadding)) {
                        text = null
                        return@setOnTouchListener true
                    }
                }
                false
            }
            requestFocus()
            doAfterTextChanged {
                if (it.toString().isEmpty()) {
                    viewModel.cancelSearchJob()
                    hideSearchResultRecyclerView()
                } else {
                    viewModel.searchRepos(it.toString())
                }
            }
        }
    }

    private fun initRecyclerView() {
        binding.rvSearchResultList.apply {
            adapter = this@SearchActivity.adapter.also {
                addItemDecoration(
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                )
            }
        }
    }

    private fun observeData() {
        viewModel.result.observe(this) {
            if (binding.etSearch.text.toString().isNotEmpty()) {
                adapter.submitData(lifecycle, it)
                showSearchResultRecyclerView()
            }
        }
    }

    private fun showSearchResultRecyclerView() = with(binding) {
        tvSearchDefaultTitle.isGone = true
        tvSearchDefaultContent.isGone = true
        rvSearchResultList.isVisible = true
    }

    private fun hideSearchResultRecyclerView() = with(binding) {
        tvSearchDefaultTitle.isVisible = true
        tvSearchDefaultContent.isVisible = true
        rvSearchResultList.isGone = true
    }
}
