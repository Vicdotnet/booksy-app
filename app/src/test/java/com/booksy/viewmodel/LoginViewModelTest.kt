package com.booksy.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.booksy.data.remote.BooksyApi
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

/**
 * pruebas unitarias para loginviewmodel
 *
 * cubre:
 * - validaciones de email y password
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // rule para ejecutar todo en el mismo thread
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // dispatcher de test
    private val testDispatcher = StandardTestDispatcher()

    // mocks
    private lateinit var mockApi: BooksyApi

    // system under test
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        // configurar dispatcher de test
        Dispatchers.setMain(testDispatcher)

        // crear mocks
        mockApi = mockk(relaxed = true)

        // configurar retrofitclient mock
        mockkObject(RetrofitClient)
        every { RetrofitClient.api } returns mockApi

        // crear viewmodel (sin context para tests)
        viewModel = LoginViewModel(context = null)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `email vacio debe mostrar error`() = runTest {
        viewModel.onEmailChange("")
        viewModel.emailError.test {
            val error = awaitItem()
            assertEquals("El email es requerido", error)
        }
    }

    @Test
    fun `password vacio debe mostrar error`() = runTest {
        viewModel.onPasswordChange("")
        viewModel.passwordError.test {
            val error = awaitItem()
            assertEquals("La contraseña es requerida", error)
        }
    }

    @Test
    fun `password con 8 o mas caracteres no debe mostrar error`() = runTest {
        viewModel.onPasswordChange("password123")
        viewModel.passwordError.test {
            val error = awaitItem()
            assertEquals(null, error)
        }
    }
}
