package com.arcxp.thearcxp.ui.composables

import ErrorDialog
import android.content.res.Configuration
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.LocalAccountViewModel
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.ui.components.CustomButton
import com.arcxp.thearcxp.ui.components.EmailInput
import com.arcxp.thearcxp.ui.components.LoadingSpinner
import com.arcxp.thearcxp.ui.components.PasswordInput
import com.arcxp.thearcxp.ui.components.SignUpButtonIcon
import com.arcxp.thearcxp.ui.components.TitleText
import com.arcxp.thearcxp.ui.theme.AppTheme
import com.arcxp.thearcxp.ui.theme.facebookButton
import com.arcxp.thearcxp.ui.theme.googleButton
import com.arcxp.thearcxp.ui.theme.googleSignInButtonTextColor
import com.arcxp.thearcxp.ui.theme.white

@Composable
fun LoginScreen(
    openCreateAccount: () -> Unit = {},
    openForgotPassword: () -> Unit = {},
    navigation: () -> Unit = {},
    accountViewModel: AccountViewModel = LocalAccountViewModel.current
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    var enableAuthentication by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val loginState by accountViewModel.loginState.collectAsState(initial = AccountViewModel.LoginState.IDLE)

    var showLoadingIndicator = remember { mutableStateOf(false) }

    when (loginState) {
        is AccountViewModel.LoginState.Success -> {
            showLoadingIndicator.value = false
            navigation()
        }

        is AccountViewModel.LoginState.LOADING -> {
            showLoadingIndicator.value = true
        }

        is AccountViewModel.LoginState.Error -> {
            showLoadingIndicator.value = false
            val error = (loginState as AccountViewModel.LoginState.Error).arcXPException

            val openDialog = remember { mutableStateOf(true) }

            ErrorDialog(
                showDialog = openDialog.value,
                errorMessage = error.message!!,
                onDismiss = {
                    openDialog.value = false
                }
            )
        }

        else -> {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_xlarge)),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
        ) {
            TitleText(textId = R.string.sign_in)
            val passwordFocusRequester = FocusRequester()
            EmailInput(
                modifier = Modifier.fillMaxWidth(),
                email = email,
                onEmailChanged = {
                    email = it
                    enableAuthentication = validateInput(email = email, password = password)
                },
                onNextClicked = { passwordFocusRequester.requestFocus() }
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            PasswordInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                password = password,
                labelId = R.string.password,
                onPasswordChanged = {
                    password = it
                    enableAuthentication = validateInput(email = email, password = password)
                },
                onDoneClicked = {}
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            CustomButton(
                buttonTextId = R.string.sign_in,
                isEnabled = enableAuthentication,
                onNextClick = {
                    if (validateInput(email, password)) {
                        accountViewModel.setLoginInProgress()
                        accountViewModel.login(email, password)
                    }

                } //onAuthenticate
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        accountViewModel.rememberUser(it)
                        isChecked = it
                    }
                )
                Text(
                    text = stringResource(id = R.string.remember_me),
                    fontSize = integerResource(id = R.integer.text_size_small).sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.no_account),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = integerResource(id = R.integer.text_size_small).sp
                )
                TextButton(onClick = openCreateAccount) {
                    Text(
                        text = stringResource(id = R.string.register),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = integerResource(id = R.integer.text_size_small).sp
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = openForgotPassword) {
                    Text(
                        text = stringResource(id = R.string.forgot_password),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = integerResource(id = R.integer.text_size_small).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (stringResource(id = R.string.google_key).isNotBlank()) {
                SignUpButtonIcon(
                    buttonTextId = R.string.fb_signin,
                    drawableResourceId = R.drawable.ic_baseline_fb,
                    buttonColor = facebookButton,
                    textColor = white,
                    onNextClick = {
                        // TODO sign in with Facebook
                        // TODO articleViewModel.restoreContentEvent()
                    }
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            }
            if (stringResource(id = R.string.google_key).isNotBlank()) {
                SignUpButtonIcon(
                    buttonTextId = R.string.google_signin,
                    drawableResourceId = R.drawable.googleg_standard_color_18,
                    buttonColor = googleButton,
                    textColor = googleSignInButtonTextColor,
                    onNextClick = {
                        accountViewModel.googleLogin()
                    }
                )
            }
        }
    }

    if (showLoadingIndicator.value) {
        LoadingSpinner()
    }
}

fun validateInput(email: String, password: String): Boolean {
    if (email.isNotBlank() && password.isNotBlank()) {
        return true
    }
    return false
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight",
    showBackground = true
)

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark",
    showBackground = true
)

@Composable
fun LoginScreenPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreen()
        }
    }
}