package com.arcxp.thearcxp.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.ui.components.CustomButton
import com.arcxp.thearcxp.ui.components.TitleText
import com.arcxp.thearcxp.ui.theme.AppTheme

@Composable
fun ForgotPasswordSuccess(navigateToSignIn: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_xlarge)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            TitleText(textId = R.string.forgot_password, modifier = Modifier)
            Text(
                text = stringResource(id = R.string.password_success), fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
            CustomButton(
                buttonTextId = R.string.back_to_sign_in,
                isEnabled = true,
                onNextClick = navigateToSignIn
            )
        }
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
fun ForgotPasswordSuccessPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ForgotPasswordSuccess(navigateToSignIn = { })
        }
    }
}