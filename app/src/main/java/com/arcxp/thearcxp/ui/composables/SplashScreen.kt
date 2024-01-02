package com.arcxp.thearcxp.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.ui.components.AppTitle
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navigateHome: () -> Unit) {
    LaunchedEffect(key1 = true) {
        delay(2000) // Adjust the delay as needed
        navigateHome()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.inverseSurface)
    ) {
        AppTitle(
            fontSize = integerResource(id = R.integer.text_size_xlarge).sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
