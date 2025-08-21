package com.trungkien.fbtp_cn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trungkien.fbtp_cn.repository.AuthRepository
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
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

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
                },
                onError = { e ->
                    _authState.value = _authState.value.copy(isLoading = false, error = e.message ?: "Đăng nhập thất bại")
                }
            )
        }
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
}
