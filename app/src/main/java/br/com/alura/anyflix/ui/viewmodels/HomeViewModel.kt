package br.com.alura.anyflix.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.anyflix.repositories.MovieRepository
import br.com.alura.anyflix.ui.uistates.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private var currentUiStateJob: Job? = null
    private val _uiState = MutableStateFlow<HomeUiState>(
        HomeUiState.Loading
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadUiState()
    }

    private fun loadUiState() {
        currentUiStateJob?.cancel()
        currentUiStateJob = viewModelScope.launch {
            repository.findSections().onStart {
                _uiState.update { HomeUiState.Loading }
            }.collectLatest { sections ->
                if (sections.isEmpty()) {
                    delay(3000)
                    _uiState.update {
                        HomeUiState.Empty
                    }
                } else {
                    val movie = sections
                        .entries.random()
                        .value.random()
                    _uiState.update {
                        HomeUiState.Success(
                            sections = sections,
                            mainBannerMovie = movie
                        )
                    }
                }
            }
        }
    }

    fun loadSections() {
        loadUiState()
    }


}