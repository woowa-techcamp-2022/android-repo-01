package com.repo01.repoapp.ui.main.tab.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.repo01.repoapp.R
import com.repo01.repoapp.databinding.FragmentNotificationsBinding
import com.repo01.repoapp.ui.common.UiState
import com.repo01.repoapp.ui.main.tab.notifications.adapter.NotificationsItemAdapter
import com.repo01.repoapp.util.PrintLog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment(), ItemTouchHelperListener {

    private lateinit var binding: FragmentNotificationsBinding
    private val notificationsViewModel: NotificationsViewModel by viewModels()
    private val notificationsAdpater by lazy { NotificationsItemAdapter() }

    private var isFirstTimeCall = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeData()
    }

    private fun initView() {
        setRecyclerView()
        getNotificationsData()
    }

    private fun setRecyclerView() {
        val swipeHelper = NotificationsSwipeHelper(
            this,
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)!!
        )
        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(binding.rvNotificationsList)

        binding.rvNotificationsList.apply {
            adapter = notificationsAdpater
            layoutManager = LinearLayoutManager(requireContext())

            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            PrintLog.printLog("SCROLL_STATE_IDLE")
                            if (isFirstTimeCall) isFirstTimeCall = false
                        }
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            PrintLog.printLog("SCROLL_STATE_DRAGGING")
                            isFirstTimeCall = true
                        }
                        RecyclerView.SCROLL_STATE_SETTLING -> PrintLog.printLog("SCROLL_STATE_SETTLING")
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = binding.rvNotificationsList.layoutManager
                    PrintLog.printLog("?????? ????????? ????????? : ${(layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()}")

                    if (!binding.rvNotificationsList.canScrollVertically(1)) {
                        PrintLog.printLog("????????? ????????? ??????")

                        val lastVisibleItemPosition =
                            (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                        if (lastVisibleItemPosition == notificationsAdpater.itemCount - 1) {
                            if (notificationsViewModel.addtionalNotificationState != UiState.Loading &&
                                notificationsViewModel.notificationState.value != UiState.Loading &&
                                isFirstTimeCall
                            ) {
                                notificationsViewModel.getNotifications(false)
                                PrintLog.printLog("${notificationsViewModel.currentPage} page ??????!")
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getNotificationsData() {
        notificationsViewModel.getNotifications(false)
    }

    private fun observeData() {
        observeNotificationsData()
        observeProgressBarVisible()
    }

    private fun observeNotificationsData() {
        notificationsViewModel.notificationList.observe(viewLifecycleOwner) {
            notificationsAdpater.submitList(it.toList())
        }

        notificationsViewModel.notificationState.observe(viewLifecycleOwner) {
            when (it) {
                is UiState.Loading -> {
                    // progressbar ????????????
                    notificationsViewModel.setProgressBarVisibility(true)
                }
                is UiState.Success -> {
                    notificationsViewModel.setProgressBarVisibility(false)
                    notificationsViewModel.getAdditionalNotificationsInfo(it.data)
                }
                is UiState.Error -> {
                    notificationsViewModel.setProgressBarVisibility(false)
                    // Error ????????????
                }
                is UiState.Empty -> {
                    notificationsViewModel.setProgressBarVisibility(false)
                }
            }
        }
    }

    private fun observeProgressBarVisible() {
        notificationsViewModel.progressBarVisible.observe(viewLifecycleOwner) {
            binding.pbLoading.isVisible = it
        }
    }

    override fun itemSwipe(position: Int) {
        // TODO - position??? id ????????? ?????? ?????? ??????
        val threadId = notificationsAdpater.currentList[position].id

        val itemPage = position / 10 + 1
        PrintLog.printLog("item position : $position")
        PrintLog.printLog("item page : $itemPage")

        notificationsViewModel.currentPage = itemPage
        notificationsViewModel.deleteItem(position)
        notificationsViewModel.deleteDataForUpdate()

        notificationsViewModel.readNotification(threadId.toLong())
    }
}