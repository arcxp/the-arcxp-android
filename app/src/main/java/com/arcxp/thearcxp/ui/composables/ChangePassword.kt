package com.arcxp.thearcxp.ui.composables

import ErrorDialog
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.arcxp.thearcxp.LocalAccountViewModel
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.ui.components.CustomButton
import com.arcxp.thearcxp.ui.components.LoadingSpinner
import com.arcxp.thearcxp.ui.components.PasswordInput
import com.arcxp.thearcxp.ui.components.TitleText
import com.arcxp.thearcxp.ui.theme.AppTheme

@Composable
fun ChangePassword(
    changePasswordSuccess: () -> Unit = {},
) {

    val accountViewModel = LocalAccountViewModel.current
    val changePasswordState by accountViewModel.changePasswordState.collectAsState(initial = AccountViewModel.ChangePasswordState.IDLE)

    var enableAuthentication by remember { mutableStateOf(false) }

    var showLoadingIndicator = remember { mutableStateOf(false) }

    var originalPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var originalPasswordOk by remember { mutableStateOf(true) }
    var newPasswordOk by remember { mutableStateOf(true) }
    var confirmPasswordOk by remember { mutableStateOf(true) }
    
    val pwLowercaseRule = Regex(stringResource(R.string.lowercase_pattern))
    val pwUppercaseRule = Regex(stringResource(R.string.uppercase_pattern))
    val pwNumberRule = Regex(stringResource(R.string.numerical_pattern))
    val pwSpecialRule = Regex(stringResource(R.string.special_pattern))

    fun validatePassword(password: String): Boolean {
        val pwLowercase = password.contains(pwLowercaseRule)
        val pwUppercase = password.contains(pwUppercaseRule)
        val pwNumber = password.contains(pwNumberRule)
        val pwSpecial = password.contains(pwSpecialRule)
        val pwLength = password.length >= 6
        return pwLowercase and pwUppercase and pwNumber and pwLength and pwSpecial
    }

    fun checkInputs(): Boolean {
        originalPasswordOk = validatePassword(originalPassword)
        newPasswordOk = validatePassword(newPassword)
        confirmPasswordOk = validatePassword(confirmPassword)
        val oError = originalPassword.isBlank() or !originalPasswordOk
        val nError = newPassword.isBlank() or !newPasswordOk
        val cError = confirmPassword.isBlank() or !confirmPasswordOk
        return !(oError or nError or cError)
    }

    when (changePasswordState) {
        is AccountViewModel.ChangePasswordState.Success -> {
            showLoadingIndicator.value = false
            originalPassword = ""
            newPassword = ""
            confirmPassword = ""
            changePasswordSuccess()
            accountViewModel.idleChangePassword()
        }
        is AccountViewModel.ChangePasswordState.LOADING -> {
            showLoadingIndicator.value = true
        }
        is AccountViewModel.ChangePasswordState.Error -> {
            showLoadingIndicator.value = false
            val error = (changePasswordState as AccountViewModel.ChangePasswordState.Error).arcXPException

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
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            TitleText(textId = R.string.change_password, modifier = Modifier.align(Alignment.Start))
            val newPasswordFocusRequester = FocusRequester()
            val confirmPasswordFocusRequester = FocusRequester()
            PasswordInput(
                modifier = Modifier.fillMaxWidth(),
                password = originalPassword,
                labelId = R.string.old_password,
                onPasswordChanged = {
                    originalPassword = it
                    enableAuthentication = validatePwdInput(originalPassword, newPassword, confirmPassword)
                    originalPasswordOk = validatePassword(originalPassword)
                },
                onDoneClicked = { newPasswordFocusRequester.requestFocus() }
            )
            if (!originalPasswordOk) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = stringResource(id = R.string.password_invalid),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            PasswordInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(newPasswordFocusRequester),
                password = newPassword,
                labelId = R.string.new_password,
                onPasswordChanged = {
                    newPassword = it
                    enableAuthentication = validatePwdInput(originalPassword, newPassword, confirmPassword)
                    newPasswordOk = validatePassword(newPassword)
                },
                onDoneClicked = { confirmPasswordFocusRequester.requestFocus() }
            )
            if (!newPasswordOk) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = stringResource(id = R.string.password_invalid),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            PasswordInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester),
                password = confirmPassword,
                labelId = R.string.confirm_password,
                onPasswordChanged = {
                    confirmPassword = it
                    enableAuthentication = validatePwdInput(originalPassword, newPassword, confirmPassword)
                    confirmPasswordOk = validatePassword(confirmPassword)
                },
                onDoneClicked = {
                }
            )
            if (!confirmPasswordOk) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
                Text(
                    text = stringResource(id = R.string.password_invalid),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            Text(
                text = stringResource(id = R.string.password_error_full),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            CustomButton(
                buttonTextId = R.string.update_password,
                isEnabled = enableAuthentication,
                onNextClick = {
                    if (checkInputs()) {
                        accountViewModel.changePassword(newPassword, originalPassword)
                    }
                }
            )
        }
    }

    if (showLoadingIndicator.value) { LoadingSpinner() }

}

fun validatePwdInput(originalPassword: String, newPassword: String, confirmPassword: String): Boolean {
    if (originalPassword.isNotBlank() &&
        newPassword.isNotBlank() &&
        confirmPassword.isNotBlank()) {
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
fun ChangePasswordPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChangePassword()
        }
    }
}