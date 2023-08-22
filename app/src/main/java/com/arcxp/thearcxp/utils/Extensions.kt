package com.arcxp.thearcxp.utils

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.arcxp.content.sdk.models.ArcXPSection

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

//This extension allows us to use TAG in any class
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

//try to use nav title as expected, but default to name if not populated
fun ArcXPSection.getNameToUseFromSection() =
    if (this.navigation.nav_title != null) {
        this.navigation.nav_title!!
    } else {
        Log.e(TAG, "Nav Title was null! defaulting to Section Name: ${this.name}")
        this.name
    }

