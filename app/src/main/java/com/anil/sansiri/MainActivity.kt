package com.anil.sansiri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.anil.sansiri.data.repository.SanSiriRepositoryImpl
import com.anil.sansiri.ui.MainApp
import com.anil.sansiri.ui.admin.AdminViewModel
import com.anil.sansiri.ui.auth.AuthViewModel
import com.anil.sansiri.ui.owner.HouseOwnerViewModel
import com.anil.sansiri.ui.theme.SanSiriTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        SanSiriRepositoryImpl.getInstance(applicationContext)
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.Factory(repository)
    }

    private val houseOwnerViewModel: HouseOwnerViewModel by viewModels {
        HouseOwnerViewModel.Factory(repository)
    }

    private val adminViewModel: AdminViewModel by viewModels {
        AdminViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanSiriTheme {
                MainApp(
                    authViewModel = authViewModel,
                    houseOwnerViewModel = houseOwnerViewModel,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}
