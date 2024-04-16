package com.arcxp.thearcxp.utils

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.arcxp.content.models.ArcXPSection


fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()


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


