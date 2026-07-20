package com.example.apiapp.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.data.connectivity.ConnectivityObserver
import com.example.apiapp.data.model.Character
import com.example.apiapp.data.network.toUserMessage
import com.example.apiapp.data.repository.CharacterRepository
import com.example.apiapp.data.sync.SyncScheduler
import com.example.apiapp.notifications.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class Filters(
    val query: String,
    val status: StatusFilter,
    val favoritesOnly: Boolean
)

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val repository: CharacterRepository,
    connectivityObserver: ConnectivityObserver,
    private val notificationHelper: NotificationHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _hasMore = MutableStateFlow(true)

    private var currentPage = 0
    private var previousConnected: Boolean? = null

    val isConnected: StateFlow<Boolean> = connectivityObserver.isConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** Unfiltered, always up-to-date list — used by the detail screen so it stays correct regardless of active search/filter. */
    val allCharacters = repository.characters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastSyncedAt: StateFlow<Long?> = repository.lastSyncedAt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalCount: StateFlow<Int?> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _episodeState = MutableStateFlow<EpisodeUiState>(EpisodeUiState.Idle)
    val episodeState: StateFlow<EpisodeUiState> = _episodeState.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow(StatusFilter.ALL)
    val favoritesOnly = MutableStateFlow(false)

    private val filters = combine(searchQuery, statusFilter, favoritesOnly) { query, status, favOnly ->
        Filters(query, status, favOnly)
    }

    val uiState: StateFlow<UiState> = combine(
        repository.characters,
        _isLoading,
        _errorMessage,
        _hasMore,
        filters
    ) { characters, isLoading, error, hasMore, filters ->
        val filtered = characters.filter { character ->
            (filters.query.isBlank() || character.name.contains(filters.query, ignoreCase = true)) &&
                (filters.status.apiValue == null || character.status.equals(filters.status.apiValue, ignoreCase = true)) &&
                (!filters.favoritesOnly || character.isFavorite)
        }
        when {
            characters.isEmpty() && error != null -> UiState.Error(error)
            characters.isEmpty() && isLoading -> UiState.Loading
            else -> UiState.Success(
                characters = filtered,
                hasMore = hasMore,
                isLoadingMore = isLoading && characters.isNotEmpty()
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    init {
        loadInitial()
        observeConnectivity()
    }

    fun loadInitial() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            currentPage = 1
            repository.refresh(currentPage)
                .onSuccess { hasMore -> _hasMore.value = hasMore }
                .onFailure { error ->
                    if (!repository.hasCachedData()) {
                        _errorMessage.value = error.toUserMessage()
                    }
                }
            _isLoading.value = false
        }
    }

    fun loadMoreCharacters() {
        if (_isLoading.value || !_hasMore.value) return
        viewModelScope.launch {
            _isLoading.value = true
            val nextPage = currentPage + 1
            repository.refresh(nextPage).onSuccess { hasMore ->
                currentPage = nextPage
                _hasMore.value = hasMore
            }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(id: Int) {
        viewModelScope.launch { repository.toggleFavorite(id) }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateStatusFilter(filter: StatusFilter) {
        statusFilter.value = filter
    }

    fun toggleFavoritesOnly() {
        favoritesOnly.value = !favoritesOnly.value
    }

    fun loadEpisodes(character: Character) {
        if (character.episodeIds.isEmpty()) {
            _episodeState.value = EpisodeUiState.Unavailable
            return
        }
        viewModelScope.launch {
            _episodeState.value = EpisodeUiState.Loading
            repository.getEpisodes(character.episodeIds)
                .onSuccess { episodes -> _episodeState.value = EpisodeUiState.Success(episodes) }
                .onFailure { error -> _episodeState.value = EpisodeUiState.Error(error.toUserMessage()) }
        }
    }

    /** Encola una sincronizacion en segundo plano via WorkManager (OneTimeWorkRequest). */
    fun syncInBackground() {
        SyncScheduler.enqueueOneTimeSync(context)
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            isConnected.collect { connected ->
                val previous = previousConnected
                if (previous != null && previous != connected) {
                    if (connected) {
                        val previousCount = repository.characters.first().size
                        repository.refreshCachedPages().onSuccess { newTotal ->
                            notificationHelper.notifySyncComplete(newItemsFound = newTotal > previousCount)
                        }
                    } else {
                        notificationHelper.notifyDisconnected()
                    }
                }
                previousConnected = connected
            }
        }
    }
}
