package com.turkcell.lyraapp.ui.auth.register

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Register akışının durumlu (stateful) giriş noktası.
 *
 * [RegisterViewModel]'i Hilt'ten alır, durumu yaşam döngüsüne duyarlı şekilde toplar ve
 * tek seferlik [RegisterEffect]'leri tüketir. Navigasyon Effect'leri buradan, [NavHost]'tan
 * gelen lambda'lara köprülenir. UI ile iş mantığı arasındaki tek köprü burasıdır.
 */
@Composable
fun RegisterRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateToHome -> onNavigateToHome()
                RegisterEffect.NavigateToLogin -> onNavigateToLogin()
                RegisterEffect.NavigateBack -> onNavigateBack()
                is RegisterEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    RegisterScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Register ("Hesap oluştur") ekranı.
 *
 * Tamamen durumsuzdur (stateless): durumu [state] üzerinden alır, kullanıcı etkileşimlerini
 * [onIntent] ile yukarı yayımlar. İş mantığı veya state sahipliği bu katmanda bulunmaz.
 */
@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onIntent: (RegisterIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            BackButton(onClick = { onIntent(RegisterIntent.BackClicked) })
            Spacer(Modifier.height(16.dp))

            HeaderTexts()
            Spacer(Modifier.height(24.dp))

            NameFields(
                firstName = state.firstName,
                lastName = state.lastName,
                onFirstNameChange = { onIntent(RegisterIntent.FirstNameChanged(it)) },
                onLastNameChange = { onIntent(RegisterIntent.LastNameChanged(it)) },
            )
            Spacer(Modifier.height(14.dp))

            PhoneNumberField(
                value = state.phoneNumber,
                onValueChange = { onIntent(RegisterIntent.PhoneNumberChanged(it)) },
            )
            Spacer(Modifier.height(14.dp))

            PasswordField(
                value = state.password,
                isPasswordVisible = state.isPasswordVisible,
                onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                onToggleVisibility = { onIntent(RegisterIntent.TogglePasswordVisibility) },
            )
            Spacer(Modifier.height(10.dp))

            PasswordStrengthIndicator(strength = state.passwordStrength)
            Spacer(Modifier.height(6.dp))

            Text(
                text = "En az 8 karakter, bir rakam içermeli.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(18.dp))

            TermsCheckbox(
                checked = state.isTermsAccepted,
                onCheckedChange = { onIntent(RegisterIntent.TermsAcceptedChanged(it)) },
            )
            Spacer(Modifier.height(24.dp))

            RegisterButton(
                enabled = state.isRegisterEnabled,
                isLoading = state.isLoading,
                onClick = { onIntent(RegisterIntent.Submit) },
            )
            Spacer(Modifier.height(20.dp))

            LoginPrompt(
                onLoginClick = { onIntent(RegisterIntent.LoginClicked) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = LyraIcons.ArrowBack,
            contentDescription = "Geri",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HeaderTexts() {
    Text(
        text = "Hesap oluştur",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Birkaç adımda Lyra'ya katıl ve çalma listeni oluştur.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun NameFields(
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Ad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Soyad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
    }
}

@Composable
private fun PhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        label = { Text("Telefon numarası") },
        prefix = { Text("+90") },
        placeholder = { Text("5XX XXX XX XX") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Smartphone,
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun PasswordField(
    value: String,
    isPasswordVisible: Boolean,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text("Şifre") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation =
            if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Lock,
                contentDescription = null,
            )
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = LyraIcons.Visibility,
                    contentDescription = if (isPasswordVisible) "Şifreyi gizle" else "Şifreyi göster",
                )
            }
        },
    )
}

/** Şifre gücü için [RegisterUiState.PASSWORD_STRENGTH_MAX] segmentli yatay gösterge. */
@Composable
private fun PasswordStrengthIndicator(strength: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(RegisterUiState.PASSWORD_STRENGTH_MAX) { index ->
            val filled = index < strength
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (filled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            )
        }
    }
}

@Composable
private fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val emphasis = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
    val annotated = buildAnnotatedString {
        withStyle(emphasis) { append("Kullanım Koşulları") }
        append(" ve ")
        withStyle(emphasis) { append("Gizlilik Politikası") }
        append("'nı okudum, kabul ediyorum.")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun RegisterButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            Text(
                text = "Kayıt ol",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = LyraIcons.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun LoginPrompt(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Zaten hesabın var mı?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Giriş yap",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onLoginClick),
        )
    }
}

@Preview(name = "Register - Light", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        RegisterScreen(state = RegisterUiState(), onIntent = {})
    }
}

@Preview(name = "Register - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        RegisterScreen(
            state = RegisterUiState(
                firstName = "Halit",
                lastName = "Kalaycı",
                phoneNumber = "555 123 45 67",
                password = "lyra1234",
                isTermsAccepted = true,
                passwordStrength = 3,
                isRegisterEnabled = true,
            ),
            onIntent = {},
        )
    }
}

// Bireysel
// 13.06.2026 - 12:00
// Projeye MVI mimarisi kurulacak.
// Register-Login-Ana Sayfa
// Fake repository ile bu 3 sayfa çalışıyor (statesel) hale getirilecek.