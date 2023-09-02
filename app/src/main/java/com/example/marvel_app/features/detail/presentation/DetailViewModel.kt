package com.example.marvel_app.features.detail.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.features.details.domain.Comic
import com.example.core.features.details.usecase.GetComicsUseCase
import com.example.core.utils.ResultStatus
import com.example.marvel_app.R
import com.example.marvel_app.features.detail.response.DetailChildViewEntity
import com.example.marvel_app.features.detail.response.DetailParentViewEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getComicsUseCase: GetComicsUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> get() = _uiState

    fun getComics(characterId: Int) = viewModelScope.launch {
        getComicsUseCase(GetComicsUseCase.GetComicsParams(characterId))
            .watchStatus()
    }

    private fun Flow<ResultStatus<List<Comic>>>.watchStatus() = viewModelScope.launch {
        collect { status ->
            _uiState.value = when (status) {
                ResultStatus.Loading -> UiState.Loading
                is ResultStatus.Success -> {
                    val detailChildList = status.data.map {
                        DetailChildViewEntity(it.id, it.imageUrl)
                    }

                    val detailParentList = listOf(
                        DetailParentViewEntity(
                            R.string.details_comics_category,
                            detailChildList
                        )
                    )
                    UiState.Success(detailParentList)
                }
                is ResultStatus.Error -> UiState.Error
            }
        }
    }

    sealed class UiState {
        object Loading: UiState()
        data class Success(val detailParentList: List<DetailParentViewEntity>): UiState()
        object Error: UiState()
    }
}