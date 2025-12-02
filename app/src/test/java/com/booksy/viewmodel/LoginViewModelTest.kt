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
 * pruebas unitarias para loginviewmodel
 *
 * cubre:
 * - validaciones de password
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // rule para ejecutar todo en el mismo thread
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // dispatcher de test
    private val testDispatcher = StandardTestDispatcher()

    // mocks
    private lateinit var mockDatabase: AppDatabase
    private lateinit var mockUserDao: UserDao
    private lateinit var mockApi: BooksyApi

    // system under test
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        // configurar dispatcher de test
        Dispatchers.setMain(testDispatcher)

        // crear mocks
        mockDatabase = mockk(relaxed = true)
        mockUserDao = mockk(relaxed = true)
        mockApi = mockk(relaxed = true)

        // configurar appdatabase mock
        every { mockDatabase.userDao() } returns mockUserDao

        // configurar retrofitclient mock
        mockkObject(RetrofitClient)
        every { RetrofitClient.api } returns mockApi

        // crear viewmodel
        viewModel = LoginViewModel(mockDatabase)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
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
