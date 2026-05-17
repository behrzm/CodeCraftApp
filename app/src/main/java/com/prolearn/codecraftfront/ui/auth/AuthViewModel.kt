package com.prolearn.codecraftfront.ui.auth

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val email: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isEmailLinkSent: Boolean = false,
    val emailForLink: String? = null,
    val pendingEmailVerification: Boolean = false,
    val phoneVerificationId: String? = null,
    val phoneNumber: String? = null,
    val isPhoneCodeSent: Boolean = false,
    val isPhoneNumberExists: Boolean = false,
    val phoneAuthInProgress: Boolean = false,
    val githubToken: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = auth.currentUser != null,
            email = auth.currentUser?.email,
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun refreshAuthState() {
        val user = auth.currentUser
        _uiState.update {
            it.copy(
                isAuthenticated = user != null,
                email = user?.email,
                pendingEmailVerification = user != null && user.email != null && !user.isEmailVerified
            )
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(appContext, gso).signOut()
        _uiState.update { AuthUiState() }
        onSignedOut()
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message, infoMessage = null) }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter email and password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            runCatching {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true, email = email.trim()) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unable to sign in.") }
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, onSuccess: () -> Unit) {
        when {
            email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please fill in all fields.") }
                return
            }
            password != confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            runCatching {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
            }.onSuccess {
                auth.currentUser?.sendEmailVerification()
                _uiState.update {
                    it.copy(isLoading = false, isAuthenticated = true, email = email.trim(),
                        infoMessage = "Account created! Verification email sent.")
                }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unable to register.") }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter email to reset password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            runCatching {
                auth.sendPasswordResetEmail(email.trim()).await()
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, infoMessage = "Password reset email sent.") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unable to send reset email.") }
            }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        if (idToken.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Google token is empty.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            runCatching {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Google sign-in failed.") }
            }
        }
    }

    fun sendEmailLink(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter email.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            runCatching {
                val actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setUrl("https://codequestdiplomaproject.web.app/email-signin")
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName("com.prolearn.codecraftfront", true, "1.0")
                    .build()
                auth.sendSignInLinkToEmail(email.trim(), actionCodeSettings).await()
                _uiState.update {
                    it.copy(isLoading = false, isEmailLinkSent = true, emailForLink = email.trim(),
                        infoMessage = "Sign-in link sent to $email")
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to send email link.") }
            }
        }
    }

    fun completeEmailLinkSignIn(link: String) {
        val email = _uiState.value.emailForLink
        if (email.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Email not found. Please enter email again.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                if (auth.isSignInWithEmailLink(link)) {
                    auth.signInWithEmailLink(email, link).await()
                    _uiState.update {
                        it.copy(isLoading = false, isAuthenticated = true, pendingEmailVerification = true,
                            emailForLink = null, isEmailLinkSent = false)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid sign-in link.") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to sign in with email link.") }
            }
        }
    }

    fun setPasswordAfterEmailLink(password: String, confirmPassword: String, onSuccess: () -> Unit) {
        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                user.updatePassword(password).await()
                user.reload().await()
                _uiState.update { it.copy(isLoading = false, pendingEmailVerification = false, infoMessage = "Password set successfully!") }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to set password.") }
            }
        }
    }

    fun checkIfPhoneExists(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, phoneNumber = phoneNumber) }
            try {
                val methods = auth.fetchSignInMethodsForEmail("${phoneNumber}@phone.craftfront.app").await()
                val exists = methods.signInMethods?.contains(PhoneAuthProvider.PROVIDER_ID) == true
                _uiState.update {
                    it.copy(isLoading = false, isPhoneNumberExists = exists,
                        errorMessage = if (!exists) "No account linked to this number. Register first." else null)
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _uiState.update { it.copy(isLoading = false, isPhoneNumberExists = false, errorMessage = "No account linked to this number.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Check failed.") }
            }
        }
    }

    fun sendPhoneVerificationCode(phoneNumber: String, activity: Activity, onSuccess: () -> Unit = {}) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                _uiState.update { it.copy(phoneAuthInProgress = false) }
                signInWithPhoneCredential(credential, onSuccess)   // ← для авто-подтверждения
            }
            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update { it.copy(isLoading = false, phoneAuthInProgress = false, errorMessage = e.message) }
            }
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _uiState.update { it.copy(isLoading = false, isPhoneCodeSent = true, phoneVerificationId = verificationId, phoneAuthInProgress = true) }
            }
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneCode(smsCode: String, onSuccess: () -> Unit) {
        val verificationId = _uiState.value.phoneVerificationId ?: run {
            _uiState.update { it.copy(errorMessage = "Verification not started.") }
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
        signInWithPhoneCredential(credential, onSuccess)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                auth.signInWithCredential(credential).await()
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true, isPhoneCodeSent = false,
                    phoneVerificationId = null, phoneAuthInProgress = false, isPhoneNumberExists = false) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Phone sign-in failed.") }
            }
        }
    }

    fun linkPhoneNumber(credential: PhoneAuthCredential) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                user.linkWithCredential(credential).await()
                _uiState.update { it.copy(isLoading = false, isPhoneCodeSent = false, phoneVerificationId = null, infoMessage = "Phone number linked!") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun sendPhoneLinkCode(phoneNumber: String, activity: Activity) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                linkPhoneNumber(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _uiState.update { it.copy(phoneVerificationId = verificationId, isPhoneCodeSent = true, isLoading = false) }
            }
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneLinkCode(smsCode: String) {
        val verificationId = _uiState.value.phoneVerificationId ?: run {
            _uiState.update { it.copy(errorMessage = "Missing verification ID") }
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
        linkPhoneNumber(credential)
    }

    fun completeGitHubSignIn(authorizationCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Для демонстрации: пропускаем обмен кода на токен.
                // В реальном проекте backend обменяет код через прокси.
                if (authorizationCode.isBlank()) {
                    throw Exception("Empty authorization code")
                }
                // Имитируем успешный вход через GitHub
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        email = "github_user@demo.com"  // фиктивная почта для UI
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "GitHub sign-in failed.") }
            }
        }
    }

    fun signInAnonymously(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                auth.signInAnonymously().await()
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Guest sign-in failed.") }
            }
        }
    }

    fun setEmailForLink(email: String) {
        _uiState.update { it.copy(emailForLink = email.trim()) }
    }
}