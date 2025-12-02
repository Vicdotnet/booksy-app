package com.booksy.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.booksy.data.local.AppDatabase
import com.booksy.data.local.UserDao
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
 * - validaciones de nombre password confirmpassword
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // mocks
    private lateinit var mockDatabase: AppDatabase
    private lateinit var mockUserDao: UserDao
    private lateinit var mockApi: BooksyApi

    // system under test
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockDatabase = mockk(relaxed = true)
        mockUserDao = mockk(relaxed = true)
        mockApi = mockk(relaxed = true)

        every { mockDatabase.userDao() } returns mockUserDao

        mockkObject(RetrofitClient)
        every { RetrofitClient.api } returns mockApi

        viewModel = RegisterViewModel(mockDatabase)
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
