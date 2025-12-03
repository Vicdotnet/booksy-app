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
 * pruebas unitarias para registerviewmodel
 *
 * cubre:
 * - validaciones de nombre, email, password y confirmpassword
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // mocks
    private lateinit var mockApi: BooksyApi

    // system under test
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockApi = mockk(relaxed = true)

        mockkObject(RetrofitClient)
        every { RetrofitClient.api } returns mockApi

        viewModel = RegisterViewModel(context = null)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `nombre vacio debe mostrar error`() = runTest {
        viewModel.onNameChange("")
        viewModel.nameError.test {
            assertEquals("El nombre es requerido", awaitItem())
        }
    }

    @Test
    fun `password vacio debe mostrar error`() = runTest {
        viewModel.onPasswordChange("")
        viewModel.passwordError.test {
            assertEquals("La contraseña es requerida", awaitItem())
        }
    }

    @Test
    fun `confirmPassword diferente a password debe mostrar error`() = runTest {
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("password456")
        viewModel.confirmPasswordError.test {
            assertEquals("Las contraseñas no coinciden", awaitItem())
        }
    }
}
