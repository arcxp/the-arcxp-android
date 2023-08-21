package com.arcxp.thearcxp.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import java.util.*

fun Context.showErrorDialog(
    title: String = "Error",
    message: String? = null,
    posBtnTxt: String? = null,
    posAction: (() -> Unit)? = null
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(posBtnTxt) { _, _ ->
            posAction?.invoke()
        }.setNegativeButton("Cancel", null)
        .show()
}

//fun ArcXPStory.print(): String {
//    val output = StringBuilder()
//    output.appendLine("<h1 style=text-align:center;> ${headlines?.basic}</h1>")
//    return output.toString()
//}


fun Context.showAlertDialog(
    title: String = "Error",
    message: String? = null,
    posBtnTxt: String? = null,
    posAction: (() -> Unit)? = null
) {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(posBtnTxt) { _, _ ->
            posAction?.invoke()
        }
        .show()
}

