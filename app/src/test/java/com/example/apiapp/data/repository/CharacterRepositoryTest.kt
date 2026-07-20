package com.example.apiapp.data.repository

import com.example.apiapp.data.local.CharacterDao
import com.example.apiapp.data.local.CharacterEntity
import com.example.apiapp.data.local.SyncMetaDao
import com.example.apiapp.data.network.RickAndMortyApiService
import com.example.apiapp.data.network.dto.CharacterDto
import com.example.apiapp.data.network.dto.CharacterResponse
import com.example.apiapp.data.network.dto.EpisodeDto
import com.example.apiapp.data.network.dto.LocationDto
import com.example.apiapp.data.network.dto.PageInfoDto
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.Runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CharacterRepositoryTest {

    private val api = mockk<RickAndMortyApiService>()
    private val dao = mockk<CharacterDao>()
    private val syncMetaDao = mockk<SyncMetaDao>()
    private val gson = Gson()
    private lateinit var repository: CharacterRepository

    private fun dto(id: Int) = CharacterDto(
        id = id,
        name = "Character $id",
        status = "Alive",
        species = "Human",
        type = "",
        gender = "Male",
        image = "https://example.com/$id.png",
        origin = LocationDto("Earth"),
        location = LocationDto("Earth"),
        episode = listOf("https://rickandmortyapi.com/api/episode/1")
    )

    @Before
    fun setUp() {
        every { dao.observeCharacters() } returns flowOf(emptyList())
        every { syncMetaDao.observeLastSyncedAt() } returns flowOf(null)
        every { syncMetaDao.observeTotalCount() } returns flowOf(null)
        coEvery { syncMetaDao.upsert(any()) } just Runs
        repository = CharacterRepository(api, dao, syncMetaDao, gson)
    }

    @Test
    fun `refresh success upserts mapped entities and marks synced`() = runTest {
        val response = CharacterResponse(
            info = PageInfoDto(count = 2, pages = 1, next = null, prev = null),
            results = listOf(dto(1), dto(2))
        )
        coEvery { api.getCharacters(1) } returns response
        coEvery { dao.getFavoriteIds() } returns emptyList()
        val slot = slot<List<CharacterEntity>>()
        coEvery { dao.upsertAll(capture(slot)) } just Runs

        val result = repository.refresh(1)

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull())
        assertEquals(2, slot.captured.size)
        assertEquals(1, slot.captured[0].id)
        coVerify { syncMetaDao.upsert(any()) }
    }

    @Test
    fun `refresh preserves favorite state for previously favorited characters`() = runTest {
        val response = CharacterResponse(
            info = PageInfoDto(count = 1, pages = 1, next = null, prev = null),
            results = listOf(dto(5))
        )
        coEvery { api.getCharacters(1) } returns response
        coEvery { dao.getFavoriteIds() } returns listOf(5)
        val slot = slot<List<CharacterEntity>>()
        coEvery { dao.upsertAll(capture(slot)) } just Runs

        repository.refresh(1)

        assertTrue(slot.captured.first { it.id == 5 }.isFavorite)
    }

    @Test
    fun `refresh returns failure when the network call throws`() = runTest {
        coEvery { api.getCharacters(1) } throws IOException("no network")

        val result = repository.refresh(1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `hasCachedData reflects dao count`() = runTest {
        coEvery { dao.count() } returns 0
        assertFalse(repository.hasCachedData())

        coEvery { dao.count() } returns 3
        assertTrue(repository.hasCachedData())
    }

    @Test
    fun `toggleFavorite delegates to dao`() = runTest {
        coEvery { dao.toggleFavorite(7) } just Runs

        repository.toggleFavorite(7)

        coVerify { dao.toggleFavorite(7) }
    }

    @Test
    fun `getEpisode returns success with the fetched episode`() = runTest {
        val episode = EpisodeDto(id = 1, name = "Pilot", airDate = "December 2, 2013", episode = "S01E01")
        coEvery { api.getEpisode(1) } returns episode

        val result = repository.getEpisode(1)

        assertTrue(result.isSuccess)
        assertEquals("Pilot", result.getOrNull()?.name)
    }

    @Test
    fun `getEpisode returns failure when the network call throws`() = runTest {
        coEvery { api.getEpisode(1) } throws IOException("no network")

        val result = repository.getEpisode(1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getEpisodes normalizes a single-object response into a one item list`() = runTest {
        val json = JsonParser.parseString(
            """{"id":1,"name":"Pilot","air_date":"December 2, 2013","episode":"S01E01"}"""
        )
        coEvery { api.getEpisodesRaw("1") } returns json

        val result = repository.getEpisodes(listOf(1))

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Pilot", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `getEpisodes normalizes an array response into a multi item list`() = runTest {
        val json = JsonParser.parseString(
            """[{"id":1,"name":"Pilot","air_date":"December 2, 2013","episode":"S01E01"},
                {"id":2,"name":"Lawnmower Dog","air_date":"December 9, 2013","episode":"S01E02"}]"""
        )
        coEvery { api.getEpisodesRaw("1,2") } returns json

        val result = repository.getEpisodes(listOf(1, 2))

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `getEpisodes returns an empty list without calling the network when there are no ids`() = runTest {
        val result = repository.getEpisodes(emptyList())

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }
}
