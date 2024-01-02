package com.arcxp.thearcxp.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.LocalAccountViewModel
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.ui.components.TitleText
import com.arcxp.thearcxp.ui.theme.AppTheme
import com.arcxp.thearcxp.ui.theme.buttonBackground
import com.arcxp.thearcxp.ui.theme.buttonText
import com.arcxp.thearcxp.ui.theme.facebookButton
import com.arcxp.thearcxp.ui.theme.googleButton
import com.arcxp.thearcxp.ui.theme.googleSignInButtonTextColor
import com.arcxp.thearcxp.ui.theme.white

@Composable
fun CreateAccount(
    navigateToSignIn: () -> Unit = {},
    navigateToSignUp: () -> Unit = {},
    accountViewModel: AccountViewModel = LocalAccountViewModel.current
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_xlarge)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TitleText(
                text = stringResource(id = R.string.create_account),
                fontSize = integerResource(id = R.integer.text_size_large).sp,
                modifier = Modifier
                    .padding(bottom = dimensionResource(id = R.dimen.padding_small))
            )
            SignUpButton(
                buttonTextId = R.string.sign_up,
                drawableResourceId = R.drawable.arc_logo,
                buttonColor = buttonBackground,
                textColor = buttonText,
                onNextClick = navigateToSignUp
            )
            SignUpButton(
                buttonTextId = R.string.sign_up_with_facebook,
                drawableResourceId = R.drawable.ic_baseline_fb,
                buttonColor = facebookButton,
                textColor = white,
                onNextClick = { accountViewModel.facebookLogin() }
            )
            SignUpButton(
                buttonTextId = R.string.sign_up_with_google,
                drawableResourceId = R.drawable.googleg_standard_color_18,
                buttonColor = googleButton,
                textColor = googleSignInButtonTextColor,
                onNextClick = {
                    accountViewModel.googleLogin()
                }
            )
            Text(
                text = stringResource(id = R.string.terms_string),
                fontSize = integerResource(id = R.integer.text_size_small).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_xlarge))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.already_have_account),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = integerResource(id = R.integer.text_size_small).sp
                )
                TextButton(onClick = navigateToSignIn) {
                    Text(
                        text = stringResource(id = R.string.sign_in),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = integerResource(id = R.integer.text_size_small).sp
                    )
                }

            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateAccountPreview() {
    AppTheme {
        CreateAccount()
    }
}