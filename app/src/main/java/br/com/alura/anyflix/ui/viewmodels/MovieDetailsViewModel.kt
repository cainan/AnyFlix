package br.com.alura.anyflix.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.alura.anyflix.model.Movie
import br.com.alura.anyflix.navigation.movieIdArgument
import br.com.alura.anyflix.repositories.MovieRepository
import br.com.alura.anyflix.ui.uistates.MovieDetailsUiState
import br.com.alura.anyflix.ui.uistates.MovieDetailsUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
) : ViewModel() {
    private var currentUiStateJob: Job? = null

    private val _uiState = MutableStateFlow<MovieDetailsUiState>(
        MovieDetailsUiState.Loading
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadUiState()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadUiState() {
        currentUiStateJob?.cancel()
        currentUiStateJob = viewModelScope.launch {
            movieRepository.findMovieById(
                requireNotNull(
                    savedStateHandle[movieIdArgument]
                )
            ).onStart {
                _uiState.update { MovieDetailsUiState.Loading }
            }.flatMapLatest { movie ->
                movieRepository.suggestedMovies(movie.id)
                    .map { suggestedMovies ->
                        Success(
                            movie = movie,
                            suggestedMovies = suggestedMovies,
                        )
                    }
            }.collectLatest { uiState ->
                _uiState.emit(uiState)
            }
        }
    }

    suspend fun addToMyList(movie: Movie) {
        movieRepository.addToMyList(movie.id)
    }

    suspend fun removeFromMyList(movie: Movie) {
        movieRepository.removeFromMyList(movie.id)
    }

    fun loadMovie() {
        loadUiState()
    }

}