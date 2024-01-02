package com.arcxp.thearcxp.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.ui.theme.buttonBackground

@Composable
fun SignUpSuccess(navigateToSignIn: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
        Text(
            text = stringResource(id = R.string.success),
            fontSize = integerResource(id = R.integer.text_size_xlarge).sp
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
        Text(
            text = stringResource(id = R.string.your_account_has_been_created),
            fontSize = integerResource(id = R.integer.text_size).sp
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
        Button(
            onClick = {
               navigateToSignIn()
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonBackground)
        ) {
            Text(text = stringResource(id = R.string.back_to_sign_in), fontSize = integerResource(id = R.integer.text_size).sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpSuccessPreview() {
    SignUpSuccess()
}