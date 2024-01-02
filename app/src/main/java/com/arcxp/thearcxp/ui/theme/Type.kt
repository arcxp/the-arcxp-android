package com.arcxp.thearcxp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.arcxp.thearcxp.R

val Typography = Typography(

    //style for top and app bar
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.old_english_five)),
        fontWeight = FontWeight.Bold,
    ),
    //for titles of pages
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
    ),
    //for large text views in pages
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif,
    ),
    //for buttons
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    ),
    //Section List Title
    bodyLarge = TextStyle(fontWeight = FontWeight.Bold),
    //Section List Description
    bodyMedium = TextStyle(fontWeight = FontWeight.Bold),
    //Account Header
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold),
    //Account Item
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold),
)