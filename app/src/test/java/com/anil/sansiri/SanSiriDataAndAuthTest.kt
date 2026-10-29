package com.anil.sansiri

import com.anil.sansiri.data.model.Announcement
import com.anil.sansiri.data.model.AppNotification
import com.anil.sansiri.data.model.ApprovalStatus
import com.anil.sansiri.data.model.FinancialTransaction
import com.anil.sansiri.data.model.HouseDetail
import com.anil.sansiri.data.model.MaintenancePayment
import com.anil.sansiri.data.model.NotificationType
import com.anil.sansiri.data.model.OccupancyType
import com.anil.sansiri.data.model.PaymentStatus
import com.anil.sansiri.data.model.PricingRules
import com.anil.sansiri.data.model.RequestComment
import com.anil.sansiri.data.model.ResidentRequest
import com.anil.sansiri.data.model.SocietyContact
import com.anil.sansiri.data.model.TransactionType
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.ui.auth.AuthViewModel
import com.anil.sansiri.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SanSiriDataAndAuthTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSanSiriRepository
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSanSiriRepository()
        authViewModel = AuthViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testQuickDemoLogin_HouseOwner() = runTest {
        authViewModel.quickDemoLogin(UserRole.HOUSE_OWNER)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.HOUSE_OWNER, state.currentUser?.role)
    }

    @Test
    fun testQuickDemoLogin_Admin() = runTest {
        authViewModel.quickDemoLogin(UserRole.ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.ADMIN, state.currentUser?.role)
    }

    @Test
    fun testQuickDemoLogin_SuperAdmin() = runTest {
        authViewModel.quickDemoLogin(UserRole.SUPER_ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.SUPER_ADMIN, state.currentUser?.role)
        assertEquals("+919964532102", state.currentUser?.phoneNumber)
        assertEquals("San Siri Super Admin", state.currentUser?.name)
    }

    @Test
    fun testSuperAdminLoginWithCredentials() = runTest {
        authViewModel.onPhoneNumberChanged("9964532102")
        authViewModel.onOtpCodeChanged("Anil@102")
        authViewModel.verifyOtp()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = authViewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals(UserRole.SUPER_ADMIN, state.currentUser?.role)
        assertEquals("+919964532102", state.currentUser?.phoneNumber)
    }

    @Test
    fun testAddAdminAndGetAdmins() = runTest {
        val result = fakeRepository.addAdmin("9876512340", "Rajesh Admin")
        assertTrue(result.isSuccess)

        val admins = fakeRepository.getAdmins().first()
        assertTrue(admins.any { it.phoneNumber.contains("9876512340") && it.role == UserRole.ADMIN })
    }

    @Test
    fun testHouseDetailCRUD() = runTest {
        val house = HouseDetail(
            id = "test_h1",
            ownerUid = "owner_123",
            ownerName = "John Doe",
            ownerPhone = "+919999988888",
            houseNumber = "C-301",
            crossStreet = "5th Cross",
            occupancyType = OccupancyType.SELF_OCCUPIED,
            approvalStatus = ApprovalStatus.PENDING
        )

        val saveResult = fakeRepository.saveHouseDetail(house)
        assertTrue(saveResult.isSuccess)

        val retrievedHouse = fakeRepository.getHouseDetailByOwner("owner_123").first()
        assertNotNull(retrievedHouse)
        assertEquals("C-301", retrievedHouse?.houseNumber)

        val updateResult = fakeRepository.updateHouseStatus("test_h1", ApprovalStatus.APPROVED)
        assertTrue(updateResult.isSuccess)

        val approvedHouses = fakeRepository.getApprovedHouses().first()
        assertTrue(approvedHouses.any { it.id == "test_h1" })
    }

    @Test
    fun testPricingRulesAndUpdate() = runTest {
        val initialRules = fakeRepository.getPricingRules().first()
        assertEquals(1500.0, initialRules.selfOccupiedMaintenanceRate, 0.01)
        assertEquals(2000.0, initialRules.rentedMaintenanceRate, 0.01)
        assertEquals(500.0, initialRules.emptySiteMaintenanceRate, 0.01)

        val newRules = PricingRules(
            selfOccupiedMaintenanceRate = 1800.0,
            rentedMaintenanceRate = 2200.0,
            emptySiteMaintenanceRate = 600.0
        )
        fakeRepository.updatePricingRules(newRules)

        val updated = fakeRepository.getPricingRules().first()
        assertEquals(1800.0, updated.selfOccupiedMaintenanceRate, 0.01)
        assertEquals(2200.0, updated.rentedMaintenanceRate, 0.01)
        assertEquals(600.0, updated.emptySiteMaintenanceRate, 0.01)

        assertEquals(1800.0, fakeRepository.calculateMaintenanceFee(OccupancyType.SELF_OCCUPIED, updated), 0.01)
        assertEquals(2200.0, fakeRepository.calculateMaintenanceFee(OccupancyType.RENTED, updated), 0.01)
        assertEquals(600.0, fakeRepository.calculateMaintenanceFee(OccupancyType.EMPTY_SITE, updated), 0.01)
    }

    @Test
    fun testMaintenancePaymentsAndApproval() = runTest {
        val payment = MaintenancePayment(
            id = "pay_test_1",
            houseId = "test_h1",
            ownerUid = "owner_123",
            ownerName = "John Doe",
            houseNumber = "C-301",
            monthYear = "2025-06",
            amount = 1800.0,
            paymentMethod = "UPI",
            transactionRef = "UPI000111222",
            status = PaymentStatus.PENDING
        )

        val submitResult = fakeRepository.submitPayment(payment)
        assertTrue(submitResult.isSuccess)

        val pendingList = fakeRepository.getPendingPayments().first()
        assertTrue(pendingList.any { it.id == "pay_test_1" })

        val approveResult = fakeRepository.approvePayment("pay_test_1")
        assertTrue(approveResult.isSuccess)

        val updatedOwnerPayments = fakeRepository.getPaymentsByOwner("owner_123").first()
        val approvedPayment = updatedOwnerPayments.find { it.id == "pay_test_1" }
        assertNotNull(approvedPayment)
        assertEquals(PaymentStatus.APPROVED, approvedPayment?.status)
    }

    @Test
    fun testLogout() = runTest {
        authViewModel.quickDemoLogin(UserRole.USER)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(authViewModel.uiState.value.currentUser)

        authViewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(authViewModel.uiState.value.currentUser)
    }

    @Test
    fun test4MonthsPaymentStatusAndMultiSelectPayment() = runTest {
        val statuses = fakeRepository.get4MonthsPaymentStatus().first()
        val currentMonth = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.US).format(java.util.Calendar.getInstance().time)
        assertTrue(statuses.isNotEmpty())
        assertTrue(statuses.any { it.monthYear == currentMonth })
        assertTrue(statuses.any { it.monthYear == currentMonth && !it.isPaid })

        val unpaidBefore = fakeRepository.getUnpaidMonthsForUser("owner_123").first()
        assertTrue(unpaidBefore.contains(currentMonth))

        val payResult = fakeRepository.submitMultiMonthPayment(
            userUid = "owner_123",
            houseId = "house_101",
            selectedMonths = listOf(currentMonth),
            transactionRef = "UPI9988776655",
            totalAmount = 1500.0
        )
        assertTrue(payResult.isSuccess)

        val unpaidAfter = fakeRepository.getUnpaidMonthsForUser("owner_123").first()
        assertTrue(!unpaidAfter.contains(currentMonth))
    }

    @Test
    fun testResidentRequestLifecycle() = runTest {
        val req = com.anil.sansiri.data.model.ResidentRequest(
            id = "req_test_10",
            userUid = "owner_123",
            userName = "Anil Kumar",
            flatNumber = "A-101",
            title = "Elevator issue",
            description = "Noise in elevator",
            status = com.anil.sansiri.data.model.RequestStatus.OPEN
        )

        fakeRepository.createResidentRequest(req)
        testDispatcher.scheduler.advanceUntilIdle()

        val list1 = fakeRepository.getResidentRequests().first()
        assertTrue(list1.any { it.id == "req_test_10" && it.status == com.anil.sansiri.data.model.RequestStatus.OPEN })

        fakeRepository.updateResidentRequestStatus("req_test_10", com.anil.sansiri.data.model.RequestStatus.RESOLVED)
        testDispatcher.scheduler.advanceUntilIdle()

        val list2 = fakeRepository.getResidentRequests().first()
        val updated = list2.find { it.id == "req_test_10" }
        assertNotNull(updated)
        assertEquals(com.anil.sansiri.data.model.RequestStatus.RESOLVED, updated?.status)
    }

    @Test
    fun testUpdateUserRoleAndBuildingDetails() = runTest {
        val updateResult = fakeRepository.updateUserRoleAndBuildingDetails(
            targetUid = "owner_123",
            name = "Anil Kumar Updated",
            houseNumber = "A-101",
            cross = "3rd Main",
            occupancyType = OccupancyType.RENTED,
            role = UserRole.ADMIN
        )
        assertTrue(updateResult.isSuccess)

        val users = fakeRepository.getAllUsers().first()
        val user = users.find { it.uid == "owner_123" }
        assertNotNull(user)
        assertEquals(UserRole.ADMIN, user?.role)
        assertEquals("Anil Kumar Updated", user?.name)

        val houses = fakeRepository.getAllHouses().first()
        val house = houses.find { it.ownerUid == "owner_123" }
        assertNotNull(house)
        assertEquals(OccupancyType.RENTED, house?.occupancyType)
    }

    @Test
    fun testUserAccountDeactivationAndPreventLogin() = runTest {
        // Initially active user owner_123 can log in
        val resultActive = fakeRepository.signInWithPhoneAndPassword("+919123456789", "123456")
        assertTrue(resultActive.isSuccess)
        assertNotNull(fakeRepository.getCurrentUser())

        // Deactivate account
        val deactivationResult = fakeRepository.setUserActiveStatus("owner_123", false)
        assertTrue(deactivationResult.isSuccess)

        // Verify user in repository is inactive
        val users = fakeRepository.getAllUsers().first()
        val deactivatedUser = users.find { it.uid == "owner_123" }
        assertNotNull(deactivatedUser)
        assertEquals(false, deactivatedUser?.isActive)

        // Current session should be revoked
        assertNull(fakeRepository.getCurrentUser())

        // Attempt login with deactivated account should fail
        val loginFailureResult = fakeRepository.signInWithPhoneAndPassword("+919123456789", "123456")
        assertTrue(loginFailureResult.isFailure)
        assertEquals(
            "Your account has been deactivated. Please contact San Siri Residency Admin.",
            loginFailureResult.exceptionOrNull()?.message
        )
    }

    @Test
    fun testFinancialTransactionsAndApprovedMaintenanceIncome() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository)
        fakeRepository.quickDemoLogin(UserRole.SUPER_ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Initial financial transactions should include custom transactions & approved maintenance income
        val initialTxns = fakeRepository.getFinancialTransactions().first()
        assertTrue(initialTxns.isNotEmpty())
        assertTrue(initialTxns.any { it.isIncome && it.category == "Maintenance Payment" })

        // 2. Add custom expense entry via ViewModel
        homeViewModel.addFinancialTransaction(
            type = TransactionType.EXPENSE,
            amount = 15000.0,
            category = "Water Tanker",
            description = "Emergency Water Tanker Supply",
            dateMillis = System.currentTimeMillis()
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedTxns = homeViewModel.uiState.value.financialTransactions
        assertTrue(updatedTxns.any { it.description == "Emergency Water Tanker Supply" && it.amount == 15000.0 && it.isExpense })
    }

    @Test
    fun testAssignAndGetCrossRepresentative() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial cross reps contains 1st Cross
        val initialReps = fakeRepository.getCrossRepresentatives().first()
        assertTrue(initialReps.containsKey("1st Cross"))

        // Assign cross representative for 2nd Cross
        homeViewModel.assignCrossRepresentative("2nd Cross", "owner_123")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedReps = homeViewModel.uiState.value.crossRepresentatives
        assertEquals("owner_123", updatedReps["2nd Cross"])
    }

    @Test
    fun testInteractiveRequestCommentsFlow() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository)
        fakeRepository.quickDemoLogin(UserRole.ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Post comment on request req_1
        homeViewModel.addRequestComment("req_1", "Plumber has completed the repairs.")
        testDispatcher.scheduler.advanceUntilIdle()

        // 2. Verify comment exists in state
        val comments = homeViewModel.uiState.value.requestComments
        val postedComment = comments.find { it.requestId == "req_1" && it.commentText == "Plumber has completed the repairs." }
        assertNotNull(postedComment)
        assertEquals("San Siri Admin", postedComment?.userName)
        assertEquals(UserRole.ADMIN, postedComment?.userRole)
    }

    @Test
    fun testDefaultSocietyContactsPrepopulated() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val contacts = homeViewModel.uiState.value.societyContacts
        assertEquals(4, contacts.size)
        assertTrue(contacts.any { it.category == "Electrical Issues" && it.contactPersonName == "Ramesh (Electrician)" && it.phoneNumber == "9876500001" })
        assertTrue(contacts.any { it.category == "Garbage Issues" && it.contactPersonName == "Suresh (Garbage Supervisor)" && it.phoneNumber == "9876500002" })
        assertTrue(contacts.any { it.category == "Plumbing Services" && it.contactPersonName == "Mahesh (Plumber)" && it.phoneNumber == "9876500003" })
        assertTrue(contacts.any { it.category == "Security Desk" && it.contactPersonName == "Main Gate Guard" && it.phoneNumber == "9876500004" })
    }

    @Test
    fun testAddAndDeleteSocietyContact() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository)
        authViewModel.quickDemoLogin(UserRole.ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Add contact
        homeViewModel.addSocietyContact("Elevator Service", "Kiran (Elevator Tech)", "9876599999")
        testDispatcher.scheduler.advanceUntilIdle()

        val contactsAfterAdd = homeViewModel.uiState.value.societyContacts
        val added = contactsAfterAdd.find { it.category == "Elevator Service" }
        assertNotNull(added)
        assertEquals("Kiran (Elevator Tech)", added?.contactPersonName)
        assertEquals("9876599999", added?.phoneNumber)

        // 2. Delete contact
        homeViewModel.deleteSocietyContact(added!!.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val contactsAfterDelete = homeViewModel.uiState.value.societyContacts
        assertTrue(contactsAfterDelete.none { it.id == added.id })
    }

    @Test
    fun testUpdateSocietyContact() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository)
        authViewModel.quickDemoLogin(UserRole.ADMIN)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialContacts = homeViewModel.uiState.value.societyContacts
        val contactToUpdate = initialContacts.first()

        val updatedContact = contactToUpdate.copy(
            category = "Updated Category",
            contactPersonName = "Updated Name",
            phoneNumber = "9999988888"
        )

        homeViewModel.updateSocietyContact(updatedContact)
        testDispatcher.scheduler.advanceUntilIdle()

        val contactsAfterUpdate = homeViewModel.uiState.value.societyContacts
        val found = contactsAfterUpdate.find { it.id == contactToUpdate.id }
        assertNotNull(found)
        assertEquals("Updated Category", found?.category)
        assertEquals("Updated Name", found?.contactPersonName)
        assertEquals("9999988888", found?.phoneNumber)
    }

    @Test
    fun testPublishAndObserveNotification() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        fakeRepository.publishNotification(
            title = "Maintenance Alert",
            message = "Water supply will be shut down from 2 PM to 4 PM.",
            type = NotificationType.GENERAL
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val notifs = homeViewModel.uiState.value.notifications
        val published = notifs.find { it.title == "Maintenance Alert" }
        assertNotNull(published)
        assertEquals("Water supply will be shut down from 2 PM to 4 PM.", published?.message)
        assertEquals(NotificationType.GENERAL, published?.type)
        assertEquals(false, published?.isRead)
    }

    @Test
    fun testAutomaticNotificationTriggers() = runTest {
        authViewModel.quickDemoLogin(UserRole.ADMIN)
        val homeViewModel = HomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Add Request -> Automatic Request Notification
        homeViewModel.createResidentRequest("Water Leakage in A-101", "Pipe leaking in bathroom", category = "REQUEST")
        testDispatcher.scheduler.advanceUntilIdle()

        val notifs1 = homeViewModel.uiState.value.notifications
        val reqNotif = notifs1.find { it.title == "New Request: Water Leakage in A-101" }
        assertNotNull(reqNotif)
        assertEquals(NotificationType.REQUEST, reqNotif?.type)

        // 2. Raise Concern -> Automatic Concern Notification
        homeViewModel.createResidentRequest("Broken Gym Equipment", "Treadmill belt snapped", category = "CONCERN")
        testDispatcher.scheduler.advanceUntilIdle()

        val notifs2 = homeViewModel.uiState.value.notifications
        val concernNotif = notifs2.find { it.title == "New Concern: Broken Gym Equipment" }
        assertNotNull(concernNotif)
        assertEquals(NotificationType.CONCERN, concernNotif?.type)

        // 3. Post Announcement -> Automatic Announcement Notification
        homeViewModel.createAnnouncement("Pest Control Drive", "Pest control scheduled on Saturday.")
        testDispatcher.scheduler.advanceUntilIdle()

        val notifs3 = homeViewModel.uiState.value.notifications
        val annNotif = notifs3.find { it.title == "New Announcement: Pest Control Drive" }
        assertNotNull(annNotif)
        assertEquals(NotificationType.ANNOUNCEMENT, annNotif?.type)

        // 4. Add Comment -> Automatic Comment Notification
        homeViewModel.addRequestComment("req_1", "Technician dispatched.")
        testDispatcher.scheduler.advanceUntilIdle()

        val notifs4 = homeViewModel.uiState.value.notifications
        val commentNotif = notifs4.find { it.type == NotificationType.COMMENT }
        assertNotNull(commentNotif)
        assertTrue(commentNotif!!.message.contains("Technician dispatched."))
    }

    @Test
    fun testMarkAndClearNotifications() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Mark single notification read
        val unreadNotif = homeViewModel.uiState.value.notifications.first { !it.isRead }
        homeViewModel.markNotificationAsRead(unreadNotif.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedNotif = homeViewModel.uiState.value.notifications.find { it.id == unreadNotif.id }
        assertEquals(true, updatedNotif?.isRead)

        // 2. Mark all as read
        homeViewModel.markAllNotificationsAsRead()
        testDispatcher.scheduler.advanceUntilIdle()

        val allNotifs = homeViewModel.uiState.value.notifications
        assertTrue(allNotifs.all { it.isRead })
        assertEquals(0, homeViewModel.uiState.value.unreadNotificationCount)

        // 3. Clear notifications
        homeViewModel.clearNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(homeViewModel.uiState.value.notifications.isEmpty())
    }
}
