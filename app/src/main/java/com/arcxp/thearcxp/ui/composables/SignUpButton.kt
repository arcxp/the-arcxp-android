package com.arcxp.thearcxp.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.ui.theme.AppTheme
import com.arcxp.thearcxp.ui.theme.buttonBackground
import com.arcxp.thearcxp.ui.theme.white

@Composable
fun SignUpButton(
    buttonTextId: Int,
    drawableResourceId: Int,
    buttonColor: Color,
    textColor: Color,
    onNextClick: () -> Unit
) {
    Button(
        onClick = onNextClick,
        shape = RoundedCornerShape(percent = 25),
        colors = ButtonDefaults.buttonColors(buttonColor),
        modifier = Modifier.padding(bottom = 15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 5.dp)) {
            Image(
                painterResource(id = drawableResourceId),
                contentDescription = stringResource(buttonTextId)
            )
            Text(
                text = stringResource(buttonTextId),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontSize = integerResource(id = R.integer.text_size_small).sp
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
@Composable
fun SignUpButtonPreview() {
    AppTheme {
        SignUpButton(
            buttonTextId = R.string.app_name,
            drawableResourceId = R.drawable.ic_baseline_fb,
            buttonColor = buttonBackground,
            textColor = white,
            onNextClick = { }
        )
    }
}