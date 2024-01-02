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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.LocalAccountViewModel
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.ui.components.CustomButton
import com.arcxp.thearcxp.ui.components.EmailInput
import com.arcxp.thearcxp.ui.components.LoadingSpinner
import com.arcxp.thearcxp.ui.components.TitleText
import com.arcxp.thearcxp.ui.components.isValidEmail
import com.arcxp.thearcxp.ui.theme.AppTheme

@Composable
fun ForgotPassword(
    accountViewModel: AccountViewModel = LocalAccountViewModel.current,
    successNavigation: () -> Unit = {}
) {

    val forgotPasswordState by accountViewModel.forgotPasswordState.collectAsState(initial = AccountViewModel.ForgotPasswordState.IDLE)

    var email by remember { mutableStateOf("") }
    val emailError by remember { mutableStateOf("") }
    var enableAuthentication by remember { mutableStateOf(false) }

    var showLoadingIndicator = remember { mutableStateOf(false) }

    when (forgotPasswordState) {
        is AccountViewModel.ForgotPasswordState.Success -> {
            showLoadingIndicator.value = false
            email = ""
            successNavigation()
            accountViewModel.idleForgotPassword()
        }

        is AccountViewModel.ForgotPasswordState.LOADING -> {
            showLoadingIndicator.value = true
        }

        is AccountViewModel.ForgotPasswordState.Error -> {
            showLoadingIndicator.value = false
            val error =
                (forgotPasswordState as AccountViewModel.ForgotPasswordState.Error).arcXPException

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
            TitleText(textId = R.string.forgot_password, modifier = Modifier)
            Text(
                text = stringResource(id = R.string.enter_email), fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            EmailInput(
                email = email,
                onEmailChanged = {
                    email = it
                    enableAuthentication = isValidEmail(it)
                },
                modifier = Modifier.fillMaxWidth(),
                onNextClicked = {
                    // TODO navigate to
                }
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            CustomButton(
                buttonTextId = R.string.send_password,
                isEnabled = enableAuthentication,
                onNextClick = {
                    accountViewModel.setForgotPasswordProgress()
                    accountViewModel.forgotPassword(email)
                }
            )
        }
    }

    if (showLoadingIndicator.value) {
        LoadingSpinner()
    }
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
fun ForgotPasswordPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ForgotPassword()
        }
    }
}