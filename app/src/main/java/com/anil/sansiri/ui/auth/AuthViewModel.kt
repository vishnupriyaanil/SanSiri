package com.anil.sansiri.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.data.repository.SanSiriRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phoneNumber: String = "+91 98765 43210",
    val otpCode: String = "",
    val selectedRole: UserRole = UserRole.HOUSE_OWNER,
    val verificationId: String? = null,
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUser: User? = null
)

class AuthViewModel(private val repository: SanSiriRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun onPhoneNumberChanged(number: String) {
        _uiState.update { it.copy(phoneNumber = number, errorMessage = null) }
    }

    fun onOtpCodeChanged(code: String) {
        _uiState.update { it.copy(otpCode = code, errorMessage = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun sendOtp(activity: Activity) {
        val phone = _uiState.value.phoneNumber.trim()
        if (phone.length < 10) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid phone number") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto verification
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Phone verification failed"
                    )
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpSent = true,
                        verificationId = verificationId
                    )
                }
            }
        }

        repository.signInWithPhone(phone, activity, callbacks)
    }

    fun verifyOtp() {
        val phone = _uiState.value.phoneNumber.trim()
        val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
        val code = _uiState.value.otpCode.trim()

        if (code.length < 5) {
            _uiState.update { it.copy(errorMessage = "Please enter valid OTP code") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // Automatically authenticate as SUPER_ADMIN when phone 9964532102 (or +919964532102) enters code 12345 or 123456
        if (cleanPhone.endsWith("9964532102") && (code == "12345" || code == "123456")) {
            viewModelScope.launch {
                val result = repository.quickDemoLogin(
                    role = UserRole.SUPER_ADMIN,
                    phoneNumber = "+919964532102",
                    name = "San Siri Super Admin"
                )
                result.onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, currentUser = user) }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Super Admin authentication failed"
                        )
                    }
                }
            }
            return
        }

        val verificationId = _uiState.value.verificationId ?: "mock_verification_id_123456"

        viewModelScope.launch {
            val result = repository.verifyOtp(verificationId, code)
            result.onSuccess { user ->
                if (user.role != UserRole.SUPER_ADMIN) {
                    val role = _uiState.value.selectedRole
                    repository.switchUserRole(role)
                }
                val currentUser = repository.getCurrentUser() ?: user
                _uiState.update { it.copy(isLoading = false, currentUser = currentUser) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Invalid OTP code"
                    )
                }
            }
        }
    }

    fun quickDemoLogin(role: UserRole = _uiState.value.selectedRole) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val phone = when {
                role == UserRole.SUPER_ADMIN -> "+919964532102"
                _uiState.value.phoneNumber.length >= 10 -> _uiState.value.phoneNumber
                else -> "+919876543210"
            }
            val name = when (role) {
                UserRole.SUPER_ADMIN -> "San Siri Super Admin"
                UserRole.ADMIN -> "San Siri Admin"
                UserRole.HOUSE_OWNER -> "Anil Kumar"
            }
            val result = repository.quickDemoLogin(role, phone, name)
            result.onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, currentUser = user) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Demo login failed"
                    )
                }
            }
        }
    }

    fun switchRole(newRole: UserRole) {
        viewModelScope.launch {
            repository.switchUserRole(newRole)
            _uiState.update { it.copy(selectedRole = newRole) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                AuthUiState(selectedRole = it.selectedRole)
            }
        }
    }

    fun resetState() {
        _uiState.update { it.copy(isOtpSent = false, otpCode = "", errorMessage = null) }
    }

    class Factory(private val repository: SanSiriRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
