package com.booksy.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.booksy.data.local.AppDatabase
import com.booksy.data.local.UserDao
import com.booksy.data.local.UserEntity
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockDatabase: AppDatabase
    private lateinit var mockUserDao: UserDao
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockDatabase = mockk(relaxed = true)
        mockUserDao = mockk(relaxed = true)

        every { mockDatabase.userDao() } returns mockUserDao
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `cargar usuario debe mostrar datos del usuario`() = runTest {
        // mock usuario
        val user = UserEntity(
            id = 1L,
            name = "Test User",
            email = "test@test.com",
            token = "test-token",
            profileImagePath = null
        )

        every { mockUserDao.getUser() } returns flowOf(user)

        viewModel = ProfileViewModel(mockDatabase)
        advanceUntilIdle()

        val currentUser = viewModel.currentUser.value
        assertNotNull(currentUser)
        assertEquals("Test User", currentUser?.name)
        assertEquals("test@test.com", currentUser?.email)
    }

    @Test
    fun `logout debe eliminar usuario de la base de datos`() = runTest {
        val user = UserEntity(
            id = 1L,
            name = "Test User",
            email = "test@test.com",
            token = "test-token",
            profileImagePath = null
        )

        every { mockUserDao.getUser() } returns flowOf(user)
        coEvery { mockUserDao.deleteUser() } just Runs

        viewModel = ProfileViewModel(mockDatabase)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        coVerify { mockUserDao.deleteUser() }
    }
}