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

data class SocialAuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userId: String? = null,
    val role: String? = null
)

sealed class SocialAuthEvent {
    data class GoogleSignIn(val idToken: String) : SocialAuthEvent()
    object ResetState : SocialAuthEvent()
}

class SocialAuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _socialAuthState = MutableStateFlow(SocialAuthState())
    val socialAuthState: StateFlow<SocialAuthState> = _socialAuthState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun handleEvent(event: SocialAuthEvent) {
        when (event) {
            is SocialAuthEvent.GoogleSignIn -> {
                signInWithGoogle(event.idToken)
            }
            is SocialAuthEvent.ResetState -> {
                _socialAuthState.value = SocialAuthState()
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _socialAuthState.value = _socialAuthState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )
            
            authRepository.signInWithGoogle(
                idToken = idToken,
                onSuccess = { role ->
                    _socialAuthState.value = _socialAuthState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        role = role
                    )
                    fetchProfile()
                },
                onError = { e ->
                    _socialAuthState.value = _socialAuthState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Đăng nhập Google thất bại"
                    )
                }
            )
        }
    }

    private fun fetchProfile() {
        userRepository.getCurrentUserProfile(
            onSuccess = { user ->
                _currentUser.value = user
            },
            onError = {
                // Ignore for now
            }
        )
    }
}

