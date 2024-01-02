package com.arcxp.thearcxp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val darkScheme = darkColorScheme(
    primary = white, //used for spinner, tab highlights currently
    onBackground = white,  //used for text
    background = black, //background color
    secondary = primaryAccent,//used for highlight color,
    tertiary = Color.Gray, //used for selections
    onTertiaryContainer = Color.LightGray, //used for selections inverse
    inverseSurface = black, //used for menu background color, is same in this example to keep menu colors consistent
    inverseOnSurface = white, //used for menu background text/icon colors, is same in this example to keep menu colors consistent
    error = error,  //used for error texts
)

private val lightScheme = lightColorScheme(
    primary = black, //used for spinner, tab highlights currently
    onBackground = black, //used for text
    background = white, //background color
    secondary = primaryAccent,//used for highlight color
    tertiary = Color.Gray, //used for selections
    onTertiaryContainer = Color.LightGray, //used for selections inverse
    inverseSurface = black, //used for menu background color, is same in this example to keep menu colors consistent
    inverseOnSurface = white, //used for menu background text/icon colors, is same in this example to keep menu colors consistent
    error = error, //used for error texts
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        //TODO improve dynamic Color implementation
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}