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
            Book(1, "Cien Años de Soledad", "Gabriel Garcia Marquez", 12990.0, "ficcion", ""),
            Book(2, "El Principito", "Antoine de Saint-Exupery", 8990.0, "infantil", "")
        )

        coEvery { RetrofitClient.api.getAllBooks() } returns Response.success(books)

        viewModel = BooksViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChange("cien")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BooksUiState.Success)
        assertEquals(1, (state as BooksUiState.Success).books.size)
        assertEquals("Cien Años de Soledad", state.books[0].title)
    }

    @Test
    fun `filtrar por categoria debe mostrar solo esa categoria`() = runTest {
        val books = listOf(
            Book(1, "Libro 1", "Autor 1", 10.0, "ficcion", ""),
            Book(2, "Libro 2", "Autor 2", 20.0, "infantil", "")
        )

        coEvery { RetrofitClient.api.getAllBooks() } returns Response.success(books)

        viewModel = BooksViewModel()
        advanceUntilIdle()

        viewModel.onCategoryChange("ficcion")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BooksUiState.Success)
        assertEquals(1, (state as BooksUiState.Success).books.size)
        assertEquals("ficcion", state.books[0].category)
    }

    @Test
    fun `error de conexion debe mostrar estado de error`() = runTest {
        coEvery { RetrofitClient.api.getAllBooks() } throws Exception("error de red")

        viewModel = BooksViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is BooksUiState.Error)
        assertEquals("error de conexion", (state as BooksUiState.Error).message)
    }
}