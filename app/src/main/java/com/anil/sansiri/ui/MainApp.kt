package com.anil.sansiri.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.anil.sansiri.ui.admin.AdminViewModel
import com.anil.sansiri.ui.auth.AuthViewModel
import com.anil.sansiri.ui.auth.LoginScreen
import com.anil.sansiri.ui.home.HomeViewModel
import com.anil.sansiri.ui.home.MainDashboard
import com.anil.sansiri.ui.owner.HouseOwnerViewModel

@Composable
fun MainApp(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    houseOwnerViewModel: HouseOwnerViewModel,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentUser == null) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {}
            )
        } else {
            // MainDashboard with HomeScreen as FIRST Landing Screen for Everyone
            MainDashboard(
                viewModel = homeViewModel,
                houseOwnerViewModel = houseOwnerViewModel,
                onLogoutClicked = { authViewModel.logout() }
            )
        }
    }
}
