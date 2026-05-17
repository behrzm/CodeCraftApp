package com.prolearn.codecraftfront.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.prolearn.codecraftfront.R
import com.prolearn.codecraftfront.ui.auth.AuthUiState
import kotlinx.coroutines.delay
import net.openid.appauth.*

// -------------------- SplashScreen (без изменений) --------------------
@Composable
fun SplashScreen(onStart: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "robotPulse")
    val mascotScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mascotScale",
    )

    LaunchedEffect(Unit) {
        delay(1700)
        onStart()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        com.prolearn.codecraftfront.ui.theme.NeonPurple.copy(alpha = 0.32f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Android,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.scale(mascotScale),
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.splash_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// -------------------- LoginScreen (с GitHub OAuth через AppAuth) --------------------
@Composable
fun LoginScreen(
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onGoogleToken: (String) -> Unit,
    onGoogleError: (String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onOpenRegister: () -> Unit,
    onPhoneCheckExists: (String) -> Unit,
    onSendPhoneCode: (String, Activity) -> Unit,
    onVerifyPhoneCode: (String) -> Unit,
    onGithubLogin: (String) -> Unit,
    onGuestSignIn: () -> Unit
) {
    var showPhoneAuth by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    // Один экземпляр сервиса на всё время жизни LoginScreen
    val authService = remember {
        if (activity != null) AuthorizationService(activity) else null
    }

    // Закрываем сервис, когда уходим с экрана
    DisposableEffect(Unit) {
        onDispose {
            authService?.dispose()
        }
    }

    // Launcher для GitHub OAuth
    val githubLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val authorizationResponse = AuthorizationResponse.fromIntent(result.data!!)
            val code = authorizationResponse?.authorizationCode
            if (code != null) {
                onGithubLogin(code)
            } else {
                onGoogleError("GitHub authorization failed: no code")
            }
        }
        // Сервис не трогаем, он будет закрыт при уходе с экрана
    }

    if (showPhoneAuth) {
        PhoneAuthContent(
            state = state,
            onDismissMessage = onDismissMessage,
            onBack = { showPhoneAuth = false },
            onCheckPhoneExists = onPhoneCheckExists,
            onSendCode = { number, activity -> onSendPhoneCode(number, activity) },
            onVerifyCode = onVerifyPhoneCode
        )
    } else {
        AuthContent(
            state = state,
            title = stringResource(R.string.auth_login_title),
            subtitle = stringResource(R.string.auth_login_subtitle),
            primaryCta = stringResource(R.string.auth_login_cta),
            secondaryCta = stringResource(R.string.auth_open_register),
            showForgotPassword = true,
            showGoogleSignIn = true,
            showPhoneOption = true,
            onPrimaryClick = { email, password, _ -> onLogin(email, password) },
            onGoogleToken = onGoogleToken,
            onGoogleError = onGoogleError,
            onForgotPasswordClick = onForgotPassword,
            onDismissMessage = onDismissMessage,
            onSecondaryClick = onOpenRegister,
            showConfirmPassword = false,
            onPhoneClick = { showPhoneAuth = true },
            onGithubClick = {
                val service = authService
                if (activity == null || service == null) {
                    onGoogleError("Cannot launch GitHub sign-in")
                    return@AuthContent
                }
                val serviceConfig = AuthorizationServiceConfiguration(
                    Uri.parse("https://github.com/login/oauth/authorize"),
                    Uri.parse("https://github.com/login/oauth/access_token")
                )
                val clientId = "Ov23lizK79SNeH4J0ZF5"
                val redirectUri = Uri.parse("com.prolearn.codecraftfront://oauth2redirect")
                val authRequest = AuthorizationRequest.Builder(
                    serviceConfig,
                    clientId,
                    ResponseTypeValues.CODE,
                    redirectUri
                ).setScope("user:email").build()

                val intent = service.getAuthorizationRequestIntent(authRequest)
                githubLauncher.launch(intent)
            },
            onGuestClick = onGuestSignIn
        )
    }
}

// -------------------- RegisterScreen (с привязкой телефона после регистрации) --------------------
@Composable
fun RegisterScreen(
    state: AuthUiState,
    onRegister: (String, String, String) -> Unit,
    onDismissMessage: () -> Unit,
    onOpenLogin: () -> Unit,
    onSendEmailLink: (String) -> Unit,
    onCompleteEmailLink: (String) -> Unit,
    onSetPasswordAfterLink: (String, String) -> Unit,
    onLinkPhone: (String) -> Unit,         // запросить код для привязки телефона
    onVerifyPhoneLink: (String) -> Unit,   // подтвердить привязку
    onSkipPhoneLink: () -> Unit            // пропустить
) {
    var showEmailLinkFlow by remember { mutableStateOf(false) }
    var showPhoneLink by remember { mutableStateOf(false) }

    // После успешной регистрации показать предложение привязать телефон
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated && !showEmailLinkFlow && !showPhoneLink) {
            showPhoneLink = true
        }
    }

    if (showEmailLinkFlow) {
        EmailLinkContent(
            state = state,
            onDismissMessage = onDismissMessage,
            onBack = { showEmailLinkFlow = false },
            onSendLink = onSendEmailLink,
            onCompleteLink = onCompleteEmailLink,
            onSetPassword = onSetPasswordAfterLink
        )
    } else if (showPhoneLink) {
        PhoneLinkContent(
            state = state,
            onDismissMessage = onDismissMessage,
            onSendCode = onLinkPhone,
            onVerifyCode = onVerifyPhoneLink,
            onSkip = {
                onSkipPhoneLink()
                showPhoneLink = false
            }
        )
    } else {
        AuthContent(
            state = state,
            title = stringResource(R.string.auth_register_title),
            subtitle = stringResource(R.string.auth_register_subtitle),
            primaryCta = stringResource(R.string.auth_register_cta),
            secondaryCta = stringResource(R.string.auth_open_login),
            showForgotPassword = false,
            showGoogleSignIn = false,
            showPhoneOption = false,
            onPrimaryClick = onRegister,
            onGoogleToken = {},
            onGoogleError = {},
            onForgotPasswordClick = {},
            onDismissMessage = onDismissMessage,
            onSecondaryClick = onOpenLogin,
            showConfirmPassword = true,
            onPhoneClick = {},
            onGithubClick = {},
            showEmailLinkOption = true,
            onEmailLinkClick = { showEmailLinkFlow = true },
            onGuestClick = {}
        )
    }
}

// -------------------- PhoneLinkContent (привязка телефона после регистрации) --------------------
@Composable
private fun PhoneLinkContent(
    state: AuthUiState,
    onDismissMessage: () -> Unit,
    onSendCode: (String) -> Unit,
    onVerifyCode: (String) -> Unit,
    onSkip: () -> Unit
) {
    val activity = LocalContext.current as Activity
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Link Phone Number",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone number (e.g., +1...)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (!state.isPhoneCodeSent) {
            Button(
                onClick = { onSendCode(phoneNumber) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Send Code")
            }
        } else {
            OutlinedTextField(
                value = smsCode,
                onValueChange = { smsCode = it },
                label = { Text("SMS Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onVerifyCode(smsCode) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Verify")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip")
        }
        AnimatedVisibility(visible = state.errorMessage != null) {
            MessageChip(
                iconRes = Icons.Rounded.ErrorOutline,
                message = state.errorMessage.orEmpty(),
                tint = MaterialTheme.colorScheme.error,
                onDismiss = onDismissMessage
            )
        }
    }
}

// -------------------- AuthContent (универсальный) --------------------
@Composable
private fun AuthContent(
    state: AuthUiState,
    title: String,
    subtitle: String,
    primaryCta: String,
    secondaryCta: String,
    showForgotPassword: Boolean,
    showGoogleSignIn: Boolean,
    showPhoneOption: Boolean,
    onPrimaryClick: (String, String, String) -> Unit,
    onGoogleToken: (String) -> Unit,
    onGoogleError: (String) -> Unit,
    onForgotPasswordClick: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onSecondaryClick: () -> Unit,
    showConfirmPassword: Boolean,
    onPhoneClick: () -> Unit,
    onGithubClick: () -> Unit,
    showEmailLinkOption: Boolean = false,
    onEmailLinkClick: () -> Unit = {},
    onGuestClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    @Suppress("DEPRECATION")
    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                onGoogleError("Missing Google ID token. Check WEB client id.")
            } else {
                onGoogleToken(idToken)
            }
        } catch (e: ApiException) {
            onGoogleError(e.localizedMessage ?: "Google sign-in failed.")
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        com.prolearn.codecraftfront.ui.theme.NeonPurple.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(22.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = { Icon(Icons.Rounded.Mail, contentDescription = null) },
                    label = { Text(stringResource(R.string.auth_email)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    leadingIcon = { Icon(Icons.Rounded.Password, contentDescription = null) },
                    label = { Text(stringResource(R.string.auth_password)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                )
                if (showConfirmPassword) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        leadingIcon = { Icon(Icons.Rounded.Password, contentDescription = null) },
                        label = { Text(stringResource(R.string.auth_confirm_password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }

                AnimatedVisibility(visible = state.errorMessage != null) {
                    MessageChip(
                        iconRes = Icons.Rounded.ErrorOutline,
                        message = state.errorMessage.orEmpty(),
                        tint = MaterialTheme.colorScheme.error,
                        onDismiss = onDismissMessage,
                    )
                }

                AnimatedVisibility(visible = state.infoMessage != null) {
                    MessageChip(
                        iconRes = Icons.Rounded.CheckCircle,
                        message = state.infoMessage.orEmpty(),
                        tint = MaterialTheme.colorScheme.primary,
                        onDismiss = onDismissMessage,
                    )
                }

                if (showForgotPassword) {
                    Text(
                        text = stringResource(R.string.auth_forgot_password),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp)
                            .clickable { onForgotPasswordClick(email) },
                    )
                }

                Button(
                    onClick = {
                        onPrimaryClick(email, password, confirmPassword)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !state.isLoading,
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .height(18.dp)
                                .padding(end = 8.dp),
                        )
                    }
                    Text(primaryCta, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(secondaryCta)
                }

                // Кнопка "Войти по телефону"
                if (showPhoneOption) {
                    OutlinedButton(
                        onClick = onPhoneClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.auth_phone_sign_in))
                    }
                }

                // Кнопка "Зарегистрироваться через Email Link"
                if (showEmailLinkOption) {
                    OutlinedButton(
                        onClick = onEmailLinkClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Rounded.Mail, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.auth_email_link_sign_up))
                    }
                }

                // Google Sign-In
                if (showGoogleSignIn) {
                    Text(
                        text = stringResource(R.string.auth_google),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                            .clickable {
                                if (activity == null) {
                                    onGoogleError("Unable to open Google sign-in from this context.")
                                } else {
                                    val webClientId = resolveWebClientId(context)
                                    if (webClientId.isNullOrBlank()) {
                                        onGoogleError("Missing default_web_client_id. Add google-services.json and sync project.")
                                        return@clickable
                                    }
                                    @Suppress("DEPRECATION")
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .requestIdToken(webClientId)
                                        .build()
                                    val client = GoogleSignIn.getClient(activity, gso)
                                    googleSignInLauncher.launch(client.signInIntent)
                                }
                            },
                    )
                }

                // GitHub Sign-In
                Text(
                    text = stringResource(R.string.auth_github),
                    color = if (state.isLoading) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp)
                        .clickable(enabled = !state.isLoading) { onGithubClick() }
                )

                // Гость
                OutlinedButton(
                    onClick = onGuestClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.auth_guest_sign_in))
                }
            }
        }
    }
}

// -------------------- Phone Auth Content (вход по телефону) --------------------
@Composable
private fun PhoneAuthContent(
    state: AuthUiState,
    onDismissMessage: () -> Unit,
    onBack: () -> Unit,
    onCheckPhoneExists: (String) -> Unit,  // оставлен для совместимости, но не используется
    onSendCode: (String, Activity) -> Unit,
    onVerifyCode: (String) -> Unit,
) {
    val activity = LocalContext.current as Activity
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        com.prolearn.codecraftfront.ui.theme.NeonPurple.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Sign in with Phone",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(22.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                    label = { Text("+1 234 567 890") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )

                if (!state.isPhoneCodeSent) {
                    Button(
                        onClick = {
                            // Сразу отправляем код, без проверок
                            if (phoneNumber.isNotBlank()) {
                                onSendCode(phoneNumber, activity)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !state.isLoading && phoneNumber.isNotBlank(),
                    ) {
                        Text(if (state.isLoading) "Sending..." else "Send Code")
                    }
                } else {
                    OutlinedTextField(
                        value = smsCode,
                        onValueChange = { smsCode = it },
                        label = { Text("SMS Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onVerifyCode(smsCode) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !state.isLoading && smsCode.isNotBlank(),
                    ) {
                        Text("Verify")
                    }
                }

                AnimatedVisibility(visible = state.errorMessage != null) {
                    MessageChip(
                        iconRes = Icons.Rounded.ErrorOutline,
                        message = state.errorMessage.orEmpty(),
                        tint = MaterialTheme.colorScheme.error,
                        onDismiss = onDismissMessage,
                    )
                }
                AnimatedVisibility(visible = state.infoMessage != null) {
                    MessageChip(
                        iconRes = Icons.Rounded.CheckCircle,
                        message = state.infoMessage.orEmpty(),
                        tint = MaterialTheme.colorScheme.primary,
                        onDismiss = onDismissMessage,
                    )
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Back to Login")
                }
            }
        }
    }
}

// -------------------- Email Link Flow --------------------
@Composable
private fun EmailLinkContent(
    state: AuthUiState,
    onDismissMessage: () -> Unit,
    onBack: () -> Unit,
    onSendLink: (String) -> Unit,
    onCompleteLink: (String) -> Unit,
    onSetPassword: (String, String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var deepLink by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        com.prolearn.codecraftfront.ui.theme.NeonPurple.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Passwordless Email Sign-In",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(modifier = Modifier.height(22.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (!state.isEmailLinkSent) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        leadingIcon = { Icon(Icons.Rounded.Mail, contentDescription = null) },
                        label = { Text(stringResource(R.string.auth_email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onSendLink(email) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !state.isLoading,
                    ) {
                        Text("Send Sign-In Link")
                    }
                } else if (!state.pendingEmailVerification) {
                    OutlinedTextField(
                        value = deepLink,
                        onValueChange = { deepLink = it },
                        label = { Text("Paste link from email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onCompleteLink(deepLink) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !state.isLoading,
                    ) {
                        Text("Complete Sign-In")
                    }
                } else {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    Button(
                        onClick = { onSetPassword(password, confirmPassword) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !state.isLoading,
                    ) {
                        Text("Set Password & Finish")
                    }
                }

                AnimatedVisibility(visible = state.errorMessage != null) {
                    MessageChip(
                        iconRes = Icons.Rounded.ErrorOutline,
                        message = state.errorMessage.orEmpty(),
                        tint = MaterialTheme.colorScheme.error,
                        onDismiss = onDismissMessage,
                    )
                }
                AnimatedVisibility(visible = state.infoMessage != null) {
                    MessageChip(
                        iconRes = Icons.Rounded.CheckCircle,
                        message = state.infoMessage.orEmpty(),
                        tint = MaterialTheme.colorScheme.primary,
                        onDismiss = onDismissMessage,
                    )
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Back")
                }
            }
        }
    }
}

// -------------------- Вспомогательные --------------------
private fun resolveWebClientId(context: android.content.Context): String? {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    return if (resId != 0) context.getString(resId) else null
}

@Composable
private fun MessageChip(
    iconRes: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    tint: androidx.compose.ui.graphics.Color,
    onDismiss: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = tint.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clickable { onDismiss() },
    ) {
        Icon(
            imageVector = iconRes,
            contentDescription = null,
            tint = tint,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}