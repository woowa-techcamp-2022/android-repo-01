package com.repo01.repoapp.ui.main.tab.issue

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.repo01.repoapp.R
import com.repo01.repoapp.databinding.FragmentIssueBinding
import com.repo01.repoapp.ui.main.tab.issue.adapter.IssueItemAdapter
import com.repo01.repoapp.util.PrintLog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IssueFragment : Fragment() {

    private lateinit var binding: FragmentIssueBinding
    private val issueViewModel: IssueViewModel by viewModels()
    private val issueAdapter by lazy { IssueItemAdapter() }
    private var filterBarActivate = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_issue, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeData()
    }

    private fun initView(){
        setFilterBar()
        setRecyclerView()
    }

    private fun setRecyclerView(){
        binding.rvIssueList.apply {
            adapter = issueAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData(){
        observeOptionData()
        observeIssueData()
    }

    private fun observeIssueData(){
        issueViewModel.issueList.observe(viewLifecycleOwner){
            issueAdapter.submitList(it.toList())
            binding.rvIssueList.smoothScrollToPosition(0)
        }
    }

    private fun observeOptionData(){
        issueViewModel.optionIndex.observe(viewLifecycleOwner){
            when(it){
                0 -> binding.tvOption.text = getString(R.string.issue_menu_open)
                1 -> binding.tvOption.text = getString(R.string.issue_menu_closed)
                2 -> binding.tvOption.text = getString(R.string.issue_menu_all)
            }
        }
    }

    private fun setFilterBar() {
        binding.clFilterBar.setOnClickListener {
            filterBarActivate = !filterBarActivate
            if (filterBarActivate) {
                binding.clFilterBar.setBackgroundResource(R.drawable.bg_issue_filter_bar_pressed)
                binding.ivOption.setImageResource(R.drawable.ic_variant16_up)
                showMenu(it, R.menu.menu_issue_filter)
            } else {
                binding.clFilterBar.setBackgroundResource(R.drawable.bg_issue_filter_bar_default)
                binding.ivOption.setImageResource(R.drawable.ic_variant16)
            }

        }
    }
    // selector 를 통해 구현 시도 -> 실패
    // 백그라운드 리소스 교체로 임시 해결

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.option_open -> {
                    PrintLog.printLog("Open")
                    issueViewModel.updateOptionIndex(0)
                    issueViewModel.getIssues("open")
                }
                R.id.option_closed -> {
                    PrintLog.printLog("Closed")
                    issueViewModel.updateOptionIndex(1)
                    issueViewModel.getIssues("closed")
                }
                R.id.option_all -> {
                    PrintLog.printLog("All")
                    issueViewModel.updateOptionIndex(2)
                }
            }
            true
        }

        popup.setOnDismissListener {
            // Respond to popup being dismissed.
            PrintLog.printLog("popup dismiss")
            binding.clFilterBar.setBackgroundResource(R.drawable.bg_issue_filter_bar_default)
            binding.ivOption.setImageResource(R.drawable.ic_variant16)
            filterBarActivate = false
        }

        when (issueViewModel.optionIndex.value) {
            0 -> {
                updateOptionSelected(popup.menu.getItem(0), getString(R.string.issue_menu_open))
                updateOptionUnselected(popup.menu.getItem(1), getString(R.string.issue_menu_closed))
                updateOptionUnselected(popup.menu.getItem(2), getString(R.string.issue_menu_all))
            }
            1 -> {
                updateOptionUnselected(popup.menu.getItem(0), getString(R.string.issue_menu_open))
                updateOptionSelected(popup.menu.getItem(1), getString(R.string.issue_menu_closed))
                updateOptionUnselected(popup.menu.getItem(2), getString(R.string.issue_menu_all))
            }
            2 -> {
                updateOptionUnselected(popup.menu.getItem(0), getString(R.string.issue_menu_open))
                updateOptionUnselected(popup.menu.getItem(1), getString(R.string.issue_menu_closed))
                updateOptionSelected(popup.menu.getItem(2), getString(R.string.issue_menu_all))
            }
            else -> {
                updateOptionUnselected(popup.menu.getItem(0), getString(R.string.issue_menu_open))
                updateOptionUnselected(popup.menu.getItem(1), getString(R.string.issue_menu_closed))
                updateOptionUnselected(popup.menu.getItem(2), getString(R.string.issue_menu_all))
            }
        }

        // Show the popup menu.
        popup.gravity = Gravity.END
        popup.show()
    }
    // < 보완 사항 >
    // 팝업메뉴 & 필터바 사이 여백 문제
    // 팝업 메뉴 전체 크기 조절 가능한지
    // 선택/미선택 메뉴의 텍스트 색상 설정 로직 짜기

    private fun updateOptionSelected(menuItem: MenuItem, itemTitle: String) {
        menuItem.title = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(
                "<font color='#FFFFFF'>${itemTitle}</font>",
                Html.FROM_HTML_MODE_LEGACY
            )
        else
            Html.fromHtml("<font color='#FFFFFF'>${itemTitle}</font>")
    }

    private fun updateOptionUnselected(menuItem: MenuItem, itemTitle: String) {
        menuItem.title = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(
                "<font color='#74869B'>${itemTitle}</font>",
                Html.FROM_HTML_MODE_LEGACY
            )
        else
            Html.fromHtml("<font color='#74869B'>${itemTitle}</font>")
    }
}