package com.flokin.stocksafe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flokin.stocksafe.repository.AuthRepository
import com.flokin.stocksafe.util.Result
import com.flokin.stocksafe.util.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)

    // ================================================
    // STATE LOGIN
    // ================================================
    private val _loginState = MutableStateFlow<Result<String>?>(null)
    val loginState: StateFlow<Result<String>?> = _loginState.asStateFlow()

    // ================================================
    // STATE REGISTER
    // ================================================
    private val _registerState = MutableStateFlow<Result<String>?>(null)
    val registerState: StateFlow<Result<String>?> = _registerState.asStateFlow()

    /**
     * Mengecek apakah user sudah login dan token belum expired.
     */
    fun isLoggedIn(): Boolean {
        return SecureStorage.isLoggedIn(getApplication())
    }

    /**
     * Mendapatkan nama user yang sedang login.
     */
    fun getUsername(): String {
        return SecureStorage.getUsername(getApplication())
    }

    /**
     * Melakukan proses login.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Result.Loading
            val result = authRepository.login(email, password)
            _loginState.value = result
        }
    }

    /**
     * Melakukan proses logout.
     */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = null
            onComplete()
        }
    }

    /**
     * Reset state login.
     */
    fun resetLoginState() {
        _loginState.value = null
    }

    /**
     * Melakukan proses registrasi.
     */
    fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirm: String
    ) {
        viewModelScope.launch {
            _registerState.value = Result.Loading
            val result = authRepository.register(name, email, password, passwordConfirm)
            _registerState.value = result
        }
    }

    /**
     * Reset state register.
     */
    fun resetRegisterState() {
        _registerState.value = null
    }
}