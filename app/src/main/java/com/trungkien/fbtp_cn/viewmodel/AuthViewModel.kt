package com.trungkien.fbtp_cn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trungkien.fbtp_cn.repository.AuthRepository
import com.trungkien.fbtp_cn.repository.UserRepository
import com.trungkien.fbtp_cn.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userId: String? = null,
    val role: String? = null,
    val op: String? = null // "REGISTER" | "LOGIN"
)

sealed class AuthEvent {
    data class Register(
        val username: String,
        val password: String,
        val email: String,
        val phone: String,
        val role: String
    ) : AuthEvent()
    data class Login(
        val email: String,
        val password: String
    ) : AuthEvent()
    data class ForgotPassword(
        val email: String
    ) : AuthEvent()
    
    object ResetState : AuthEvent()
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun handleEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.Register -> {
                _authState.value = _authState.value.copy(op = "REGISTER")
                registerUser(event.username, event.password, event.email, event.phone, event.role)
            }
            is AuthEvent.Login -> {
                _authState.value = _authState.value.copy(op = "LOGIN")
                login(event.email, event.password)
            }
            is AuthEvent.ForgotPassword -> {
                _authState.value = _authState.value.copy(op = "FORGOT")
                resetPassword(event.email)
            }
            is AuthEvent.ResetState -> {
                _authState.value = AuthState()
            }
        }
    }

    private fun registerUser(
        username: String,
        password: String,
        email: String,
        phone: String,
        role: String
    ) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null,
                    isSuccess = false
                )

                authRepository.registerUser(
                    username = username,
                    password = password,
                    email = email,
                    phone = phone,
                    role = role,
                    onSuccess = { userId ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            userId = userId
                        )
                    },
                    onError = { error ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Đăng ký thất bại"
                        )
                    }
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Có lỗi xảy ra"
                )
            }
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, isSuccess = false)
            authRepository.login(
                email = email,
                password = password,
                onSuccess = { role ->
                    _authState.value = _authState.value.copy(isLoading = false, isSuccess = true, role = role)
                    fetchProfile()
                },
                onError = { e ->
                    _authState.value = _authState.value.copy(isLoading = false, error = e.message ?: "Đăng nhập thất bại")
                }
            )
        }
    }

    fun fetchProfile() {
        println("🔄 DEBUG: AuthViewModel.fetchProfile() called")
        userRepository.getCurrentUserProfile(
            onSuccess = { user -> 
                println("🔄 DEBUG: AuthViewModel.fetchProfile() success - user: $user")
                println("🔄 DEBUG: AuthViewModel.fetchProfile() success - userId: ${user.userId}")
                println("🔄 DEBUG: AuthViewModel.fetchProfile() success - avatarUrl: ${user.avatarUrl?.take(50)}...")
                _currentUser.value = user 
                println("🔄 DEBUG: AuthViewModel.fetchProfile() - _currentUser.value updated")
                println("🔄 DEBUG: AuthViewModel.fetchProfile() - _currentUser.value: ${_currentUser.value?.name}")
            },
            onError = { error -> 
                println("❌ ERROR: AuthViewModel.fetchProfile() failed: ${error.message}")
            }
        )
    }

    fun updateProfile(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        address: String? = null,
        avatarUrl: String? = null,
        onDone: (Boolean, String?) -> Unit
    ) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        // Optimistic UI update for avatar to make UI reflect immediately
        val previousUser = _currentUser.value
        // Optimistic UI update for name so ProfileHeader updates immediately
        if (!name.isNullOrBlank() && previousUser != null) {
            _currentUser.value = previousUser.copy(name = name)
        }
        val normalizedAvatar: String? = avatarUrl?.let { raw ->
            when {
                raw.isBlank() -> ""
                raw.startsWith("http", ignoreCase = true) -> raw
                raw.startsWith("data:image", ignoreCase = true) -> raw
                else -> "data:image/jpeg;base64,$raw"
            }
        }
        if (normalizedAvatar != null) {
            _currentUser.value = previousUser?.copy(avatarUrl = normalizedAvatar)
        }
        userRepository.updateCurrentUserProfile(
            name = name,
            email = email,
            phone = phone,
            address = address,
            avatarUrl = avatarUrl,
            onSuccess = { user ->
                // Server truth wins; replace optimistic user with server user
                _currentUser.value = user
                _authState.value = _authState.value.copy(isLoading = false, isSuccess = true)
                onDone(true, null)
            },
            onError = { e ->
                // Revert optimistic avatar if failed
                if (previousUser != null) {
                    _currentUser.value = previousUser
                }
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
                onDone(false, e.message)
            }
        )
    }

    // Force apply avatar optimistically, used to ensure all screens update together when closing dialogs
    fun applyOptimisticAvatar(avatarUrl: String?) {
        val normalized = avatarUrl?.let { raw ->
            when {
                raw.isBlank() -> ""
                raw.startsWith("http", ignoreCase = true) -> raw
                raw.startsWith("data:image", ignoreCase = true) -> raw
                else -> "data:image/jpeg;base64,$raw"
            }
        }
        _currentUser.value = _currentUser.value?.copy(avatarUrl = normalized.orEmpty())
    }

    private fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, isSuccess = false)
            authRepository.sendPasswordReset(
                email = email,
                onSuccess = {
                    _authState.value = _authState.value.copy(isLoading = false, isSuccess = true)
                },
                onError = { e ->
                    _authState.value = _authState.value.copy(isLoading = false, error = e.message ?: "Gửi email khôi phục thất bại")
                }
            )
        }
    }

    fun deleteAccount(onDone: (Boolean, String?) -> Unit) {
        _authState.value = _authState.value.copy(isLoading = true, error = null, isSuccess = false)
        authRepository.deleteAccount(
            onSuccess = {
                _currentUser.value = null
                _authState.value = AuthState(isSuccess = true)
                onDone(true, null)
            },
            onError = { e ->
                _authState.value = _authState.value.copy(isLoading = false, error = e.message ?: "Xóa tài khoản thất bại")
                onDone(false, e.message)
            }
        )
    }
}
