package com.example.apiapp.ui

import com.example.apiapp.MainDispatcherRule
import com.example.apiapp.data.connectivity.ConnectivityObserver
import com.example.apiapp.data.model.Character
import com.example.apiapp.data.repository.CharacterRepository
import com.example.apiapp.notifications.NotificationHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<CharacterRepository>()
    private val connectivityObserver = mockk<ConnectivityObserver>()
    private val notificationHelper = mockk<NotificationHelper>(relaxUnitFun = true)
    private val connectionFlow = MutableStateFlow(true)

    private fun character(id: Int, name: String, status: String = "Alive", favorite: Boolean = false) = Character(
        id = id,
        name = name,
        status = status,
        species = "Human",
        type = "",
        gender = "Male",
        imageUrl = "https://example.com/$id.png",
        locationName = "Earth",
        originName = "Earth",
        episodeCount = 1,
        isFavorite = favorite,
        firstEpisodeId = 1,
        episodeIds = listOf(1)
    )

    @Before
    fun setUp() {
        every { connectivityObserver.isConnected } returns connectionFlow
        every { repository.lastSyncedAt } returns flowOf(null)
        every { repository.totalCount } returns flowOf(null)
        coEvery { repository.hasCachedData() } returns false
    }

    private fun createViewModel(): CharacterViewModel =
        CharacterViewModel(repository, connectivityObserver, notificationHelper, mockk(relaxed = true))

    @Test
    fun `loadInitial success shows characters from cache`() = runTest {
        val characters = listOf(character(1, "Rick"), character(2, "Morty"))
        every { repository.characters } returns flowOf(characters)
        coEvery { repository.refresh(1) } returns Result.success(true)

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).characters.size)
    }

    @Test
    fun `loadInitial failure without cache shows error`() = runTest {
        every { repository.characters } returns flowOf(emptyList())
        coEvery { repository.refresh(1) } returns Result.failure(IOException("sin red"))
        coEvery { repository.hasCachedData() } returns false

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Error)
    }

    @Test
    fun `loadInitial failure with cached data keeps showing cached characters`() = runTest {
        val characters = listOf(character(1, "Rick"))
        every { repository.characters } returns flowOf(characters)
        coEvery { repository.refresh(1) } returns Result.failure(IOException("sin red"))
        coEvery { repository.hasCachedData() } returns true

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is UiState.Success)
    }

    @Test
    fun `search query filters characters by name`() = runTest {
        val characters = listOf(character(1, "Rick Sanchez"), character(2, "Morty Smith"))
        every { repository.characters } returns flowOf(characters)
        coEvery { repository.refresh(1) } returns Result.success(false)

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateSearchQuery("morty")
        advanceUntilIdle()

        val state = viewModel.uiState.value as UiState.Success
        assertEquals(1, state.characters.size)
        assertEquals("Morty Smith", state.characters.first().name)
    }

    @Test
    fun `favoritesOnly filter shows only favorited characters`() = runTest {
        val characters = listOf(
            character(1, "Rick Sanchez", favorite = true),
            character(2, "Morty Smith", favorite = false)
        )
        every { repository.characters } returns flowOf(characters)
        coEvery { repository.refresh(1) } returns Result.success(false)

        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.toggleFavoritesOnly()
        advanceUntilIdle()

        val state = viewModel.uiState.value as UiState.Success
        assertEquals(1, state.characters.size)
        assertTrue(state.characters.first().isFavorite)
    }

    @Test
    fun `reconnecting after being offline triggers a resync and a notification`() = runTest {
        every { repository.characters } returns flowOf(emptyList())
        coEvery { repository.refresh(1) } returns Result.success(false)
        coEvery { repository.refreshCachedPages() } returns Result.success(0)

        createViewModel()
        advanceUntilIdle()

        connectionFlow.value = false
        advanceUntilIdle()
        coVerify { notificationHelper.notifyDisconnected() }

        connectionFlow.value = true
        advanceUntilIdle()

        coVerify { repository.refreshCachedPages() }
        coVerify { notificationHelper.notifySyncComplete(any()) }
    }
}
