package com.arcxp.thearcxp.utils

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.arcxp.content.models.ArcXPSection
import com.arcxp.thearcxp.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun Context.showErrorDialog(
    title: String = getString(R.string.error),
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

fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

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

fun ArcXPSection.getPushTopicName() =
    id.subSequence(1, id.lastIndex+1)

fun <T> FragmentActivity.collectOneTimeEvent(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launchWhenCreated {
        launch {
            flow.collect(collector = collect)
        }
    }
}

fun <T> Fragment.collectOneTimeEvent(flow: Flow<T>, collect: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launchWhenCreated {
        launch {
            flow.collect(collector = collect)
        }
    }
}

@Composable
fun OnLifecycleEvent(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            onEvent.invoke(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
                lifecycle.removeObserver(observer)
            }
    }
}


