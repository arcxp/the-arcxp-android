package com.arcxp.thearcxp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.ui.theme.blue
import com.arcxp.thearcxp.utils.PasswordRequirement


@Composable
fun LoadingSpinner() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PasswordRequirements(
    modifier: Modifier = Modifier,
    satisfiedRequirements: List<PasswordRequirement>
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        PasswordRequirement.values().forEach { requirement ->
            Requirement(
                message = stringResource(id = requirement.label),
                satisfied = satisfiedRequirements.contains(requirement)
            )
        }
    }
}

@Composable
fun Requirement(
    modifier: Modifier = Modifier,
    message: String,
    satisfied: Boolean
) {
    val requirementStatus = if (satisfied) {
        stringResource(id = R.string.password_requirement_satisfied, message)
    } else {
        stringResource(id = R.string.password_requirement_needed, message)
    }
    Row(
        modifier = modifier
            .padding(dimensionResource(id = R.dimen.space))
            .semantics(mergeDescendants = true) {
                text = AnnotatedString(requirementStatus)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO change color for dark theme
        val tint = if (satisfied) {
            blue
        } else androidx.compose.material.MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
        Text(
            modifier = Modifier.clearAndSetSemantics { },
            fontSize = 13.sp,
            text = message,
            color = tint
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_PasswordRequirements() {
    androidx.compose.material.MaterialTheme {
        PasswordRequirements(
            modifier = Modifier.fillMaxWidth(),
            satisfiedRequirements = listOf(PasswordRequirement.UPPERCASE_LETTER)
        )
    }
}

@Composable
fun DirectionalContent(content: @Composable () -> Unit) {
    //Figure out a better way to do this
    if (booleanResource(id = R.bool.use_rtl)) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    } else {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            content()
        }
    }
}