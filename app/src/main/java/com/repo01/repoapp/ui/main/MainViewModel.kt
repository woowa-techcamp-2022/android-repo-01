package com.repo01.repoapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repo01.repoapp.data.model.ProfileModel
import com.repo01.repoapp.data.repository.ProfileRepository
import com.repo01.repoapp.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableLiveData<UiState<ProfileModel>>()
    val uiState: LiveData<UiState<ProfileModel>> = _uiState

    private val _searchClickEvent = MutableLiveData<Unit>()
    val searchClickEvent : LiveData<Unit> = _searchClickEvent

    private val _profileClickEvent = MutableLiveData<Unit>()
    val profileClickEvent : LiveData<Unit> = _profileClickEvent

    init {
        getUserInformation()
    }

    private fun getUserInformation() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            _uiState.value = repository.getUserInformation()
        }
    }

    fun onSearchIconClicked() {
        _searchClickEvent.postValue(Unit)
    }

    fun onProfileIconClicked() {
        _profileClickEvent.postValue(Unit)
    }
}