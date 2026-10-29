package com.anil.sansiri.ui.auth

import com.anil.sansiri.FakeSanSiriRepository
import com.anil.sansiri.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSanSiriRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSanSiriRepository()
        viewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSuperAdminLoginWithCredentialsAnil102() = runTest {
        viewModel.onPhoneNumberChanged("9964532102")
        viewModel.onOtpCodeChanged("Anil@102")
        viewModel.verifyOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.SUPER_ADMIN, state.currentUser?.role)
        assertEquals("+919964532102", state.currentUser?.phoneNumber)
    }

    @Test
    fun testSuperAdminLoginWithCredentials123456() = runTest {
        viewModel.onPhoneNumberChanged("9964532102")
        viewModel.onOtpCodeChanged("123456")
        viewModel.verifyOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.SUPER_ADMIN, state.currentUser?.role)
    }

    @Test
    fun testPhoneNumberFilteringDigitsAnd10DigitLimit() = runTest {
        viewModel.onPhoneNumberChanged("+91-987-654-3210-999")
        assertEquals("9198765432", viewModel.uiState.value.phoneNumber)

        viewModel.onPhoneNumberChanged("9876543210")
        assertEquals("9876543210", viewModel.uiState.value.phoneNumber)
    }

    @Test
    fun testLoginFailsIfPhoneNot10Digits() = runTest {
        viewModel.onPhoneNumberChanged("987654321") // 9 digits
        viewModel.onPasswordChanged("Anil@102")
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertEquals("Please enter a valid 10-digit phone number", state.errorMessage)
        assertNull(state.currentUser)
    }

    @Test
    fun testQuickDemoLoginSuperAdmin() = runTest {
        viewModel.quickDemoLogin(UserRole.SUPER_ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.SUPER_ADMIN, state.currentUser?.role)
        assertEquals("+919964532102", state.currentUser?.phoneNumber)
        assertEquals("San Siri Super Admin", state.currentUser?.name)
    }

    @Test
    fun testQuickDemoLoginAdminAndHouseOwner() = runTest {
        viewModel.quickDemoLogin(UserRole.ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(UserRole.ADMIN, viewModel.uiState.value.currentUser?.role)

        viewModel.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(UserRole.HOUSE_OWNER, viewModel.uiState.value.currentUser?.role)
    }

    @Test
    fun testOtpValidationFailsForShortCode() = runTest {
        viewModel.onPhoneNumberChanged("9876543210")
        viewModel.onOtpCodeChanged("12")
        viewModel.verifyOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertNull(state.currentUser)
    }

    @Test
    fun testLogoutClearsCurrentUser() = runTest {
        viewModel.quickDemoLogin(UserRole.SUPER_ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.currentUser)

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.currentUser)
    }
}
