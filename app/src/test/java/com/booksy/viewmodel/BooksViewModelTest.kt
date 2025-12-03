package com.booksy.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.booksy.data.models.Book
import com.booksy.data.remote.RetrofitClient
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BooksViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: BooksViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(RetrofitClient)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `buscar libros debe filtrar por titulo`() = runTest {
        // mock de libros
        val books = listOf(
            Book("1", "La casa de los espíritus", "Isabel Allende", 12990.0, "Ficción", "", null),
            Book("2", "Canto general", "Pablo Neruda", 14990.0, "Poesía", "", null)
        )

        coEvery { RetrofitClient.api.getAllBooks() } returns Response.success(books)

        viewModel = BooksViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("casa")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BooksUiState.Success)
        assertEquals(1, (state as BooksUiState.Success).books.size)
        assertEquals("La casa de los espíritus", state.books[0].title)
    }

    @Test
    fun `filtrar por categoria debe mostrar solo esa categoria`() = runTest {
        val books = listOf(
            Book("1", "Libro Ficción", "Autor 1", 10.0, "Ficción", "", null),
            Book("2", "Libro Poesía", "Autor 2", 20.0, "Poesía", "", null)
        )

        coEvery { RetrofitClient.api.getAllBooks() } returns Response.success(books)

        viewModel = BooksViewModel()
        advanceUntilIdle()

        viewModel.onCategoryChange("Ficción")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BooksUiState.Success)
        assertEquals(1, (state as BooksUiState.Success).books.size)
        assertEquals("Ficción", state.books[0].category)
    }
}