package com.arcxp.thearcxp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.ui.theme.blue
import com.arcxp.thearcxp.ui.theme.buttonBackground
import com.arcxp.thearcxp.ui.theme.buttonText
import com.arcxp.thearcxp.ui.theme.grey
import com.arcxp.thearcxp.ui.theme.white

@Composable
fun SignUpButtonIcon(
    buttonTextId: Int,
    drawableResourceId: Int,
    buttonColor: Color,
    textColor: Color,
    onNextClick: () -> Unit
) {
    Button(
        onClick = onNextClick,
        shape = RoundedCornerShape(percent = 25),
        colors = ButtonDefaults.buttonColors(buttonColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_small))
        ) {
            Image(
                painterResource(id = drawableResourceId),
                contentDescription = stringResource(buttonTextId)
            )
            Text(
                text = stringResource(buttonTextId),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun CustomButton(
    buttonTextId: Int,
    isEnabled: Boolean,
    onNextClick: () -> Unit
) {
    val buttonColor = if (isEnabled) buttonBackground else grey
    Button(
        onClick = onNextClick,
        shape = RoundedCornerShape(percent = 25),
        colors = ButtonDefaults.buttonColors(buttonColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(buttonTextId),
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_small)),
            textAlign = TextAlign.Center,
            color = buttonText,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun TitleText(textId: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = textId),
        fontSize = integerResource(id = R.integer.text_size).sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = modifier.padding(bottom = dimensionResource(id = R.dimen.padding_xlarge)))
}

//view for title of page:
@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = integerResource(id = R.integer.text_size_large).sp
) {
    Text(
        text = text,
        fontSize = fontSize,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Start
    )
    Spacer(modifier = modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small)))
}

//app title, used in top menu and splash screen
@Composable
fun AppTitle(fontSize: TextUnit, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.inverseOnSurface,
        fontSize = fontSize,
        modifier = modifier
    )
}


@Composable
fun EmailInput(
    modifier: Modifier = Modifier,
    email: String?,
    onEmailChanged: (email: String) -> Unit,
    onNextClicked: () -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = email ?: "",
        onValueChange = onEmailChanged,
        supportingText = {
            if (email?.isEmpty() == true) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.cannot_be_empty, "Email"),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        shape = RoundedCornerShape(percent = 25),
        label = { Text(text = stringResource(id = R.string.email_address)) },
        singleLine = true,
        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Email
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                onNextClicked()
            }
        )
    )
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
    return email.matches(emailRegex)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordInput(
    modifier: Modifier = Modifier,
    password: String?,
    labelId: Int,
    onError: (String) -> Unit = {},
    onPasswordChanged: (password: String) -> Unit,
    onDoneClicked: () -> Unit
) {
    var isPasswordHidden by remember {
        mutableStateOf(true)
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        modifier = modifier,
        value = password ?: "",
        onValueChange = onPasswordChanged,
        singleLine = true,
        label = { Text(text = stringResource(id = labelId)) },
        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            Icon(
                modifier = Modifier.clickable(
                    onClickLabel = if (isPasswordHidden) {
                        stringResource(id = R.string.cd_show_password)
                    } else stringResource(id = R.string.cd_hide_password)
                ) {
                    isPasswordHidden = !isPasswordHidden
                },
                imageVector = if (isPasswordHidden) {
                    Icons.Default.Visibility
                } else Icons.Default.VisibilityOff,
                contentDescription = null
            )
        },
        supportingText = {
            if (password?.isEmpty() == true) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.cannot_be_empty, "Password"),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        shape = RoundedCornerShape(percent = 25),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                onDoneClicked()
            }
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        visualTransformation = if (isPasswordHidden) {
            PasswordVisualTransformation()
        } else VisualTransformation.None
    )
}

@Composable
fun FirstNameInput(
    modifier: Modifier = Modifier,
    firstName: String?,
    onFirstNameChanged: (firstName: String) -> Unit,
    onNextClicked: () -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = firstName ?: "",
        onValueChange = onFirstNameChanged,
        colors = TextFieldDefaults.colors(
            // TODO border color for light theme
            focusedContainerColor = white,
            unfocusedContainerColor = white,
            focusedTextColor = blue,
            unfocusedTextColor = blue,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(percent = 25),
        label = { Text(text = "First Name") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                onNextClicked()
            }
        )
    )
}

@Composable
fun LastNameInput(
    modifier: Modifier = Modifier,
    lastName: String?,
    onLastNameChanged: (lastName: String) -> Unit,
    onNextClicked: () -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = lastName ?: "",
        onValueChange = onLastNameChanged,
        colors = TextFieldDefaults.colors(
            // TODO border color for light theme
            focusedContainerColor = white,
            unfocusedContainerColor = white,
            focusedTextColor = blue,
            unfocusedTextColor = blue,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(percent = 25),
        label = { Text(text = "Last Name") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                onNextClicked()
            }
        )
    )
}

@Composable
fun ErrorDialog(
    errorTitleId: Int,
    errorActionId: Int,
    error: String,
    dismissError: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            dismissError()
        },
        confirmButton = {
            TextButton(onClick = {
                dismissError()
            }) {
                androidx.compose.material.Text(text = stringResource(id = errorActionId))
            }
        },
        title = {
            androidx.compose.material.Text(
                text = stringResource(id = errorTitleId),
                fontSize = 18.sp
            )
        },
        text = {
            androidx.compose.material.Text(text = error)
        }
    )
}

