package com.arcxp.thearcxp.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.R

@Composable
fun Paywall(
    modifier: Modifier = Modifier,
    openCreateAccount: () -> Unit = {},
    openSignIn: () -> Unit = {},
    backNavigation: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_large)),

        ) {
        Row(
            modifier = Modifier
                .padding(end = dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth(), horizontalArrangement = Arrangement.End
        ) {
            Image(painter = painterResource(id = R.drawable.ic_baseline_clear_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inverseSurface),
                modifier = Modifier
                    .clickable { backNavigation() })
        }
        Text(
            text = stringResource(id = R.string.continue_reading),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = dimensionResource(id = R.dimen.text_size_small).value.sp,
            color = MaterialTheme.colorScheme.inverseSurface
        )
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = stringResource(id = R.string.have_account),
                fontSize = dimensionResource(id = R.dimen.text_size).value.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseSurface
            )
            Text(
                text = stringResource(id = R.string.sign_in),
                fontSize = dimensionResource(id = R.dimen.text_size).value.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.padding_small))
                    .clickable {
                        openSignIn()
                    }
            )
        }
        Text(
            text = stringResource(id = R.string.get_every_story),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = dimensionResource(id = R.dimen.text_size_xlarge).value.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.inverseSurface
        )

        Button(
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.padding_mlarge)),
            content = { Text(text = stringResource(id = R.string.create_account)) },
            onClick = {
                openCreateAccount()
            }
        )
    }
}
