package com.arcxp.thearcxp.ui.composables

import ErrorDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPUser
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.ui.components.TitleText
import com.arcxp.thearcxp.ui.theme.buttonBackground
import com.arcxp.thearcxp.utils.isValidEmail

@Composable
fun SignUpScreen(
    backNavigation: () -> Unit = {},
    navigateToSignUpSuccess: () -> Unit = {},
    navigateToSignIn: () -> Unit = {},
) {
    //onscreen text values from input
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("An unexpected error occurred.") }

    var passwordVisibility by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    //strings
    val createAccount = stringResource(id = R.string.create_account)
    val firstNameError = stringResource(id = R.string.first_name_error)
    val lastNameError = stringResource(id = R.string.last_name_error)
    val emailNameError = stringResource(id = R.string.email_error)
    val passwordError = stringResource(id = R.string.password_error)

    //error states
    var firstNameIsError by remember { mutableStateOf(false) }
    var lastNameIsError by remember { mutableStateOf(false) }
    var emailIsError by remember { mutableStateOf(false) }
    var passwordIsError by remember { mutableStateOf(false) }

    var isEnabled by remember { mutableStateOf(true) }

    //password rules state
    var pwLowercase by remember { mutableStateOf(false) }
    var pwUppercase by remember { mutableStateOf(false) }
    var pwNumber by remember { mutableStateOf(false) }
    var pwSpecial by remember { mutableStateOf(false) }
    var pwLength by remember { mutableStateOf(false) }

    //password regex rules
    val pwLowercaseRule = Regex(stringResource(R.string.lowercase_pattern))
    val pwUppercaseRule = Regex(stringResource(R.string.uppercase_pattern))
    val pwNumberRule = Regex(stringResource(R.string.numerical_pattern))
    val pwSpecialRule = Regex(stringResource(R.string.special_pattern))

    fun validatePassword(): Boolean {
        pwLowercase = password.contains(pwLowercaseRule)
        pwUppercase = password.contains(pwUppercaseRule)
        pwNumber = password.contains(pwNumberRule)
        pwSpecial = password.contains(pwSpecialRule)
        pwLength = password.length >= 6
        return pwLowercase and pwUppercase and pwNumber and pwLength and pwSpecial
    }

    fun checkInputs(): Boolean {
        firstNameIsError = firstName.isBlank()
        lastNameIsError = lastName.isBlank()
        emailIsError = !email.isValidEmail()
        passwordIsError = password.isBlank()
        isEnabled = !(firstNameIsError or lastNameIsError or emailIsError or passwordIsError) and validatePassword()
        return isEnabled
    }

    fun checkInputsAndSubmit() {
        focusManager.clearFocus()
        if (checkInputs()) {
            ArcXPMobileSDK.commerceManager().signUp(//TODO refactor to use vm
                username = email,
                firstname = firstName,
                lastname = lastName,
                email = email,
                password = password,
                listener = object :
                    ArcXPIdentityListener() {
                    override fun onRegistrationSuccess(response: ArcXPUser) {
                        backNavigation()
                        backNavigation()
                        navigateToSignUpSuccess()
                    }

                    override fun onRegistrationError(error: ArcXPException) {
                        errorMessage = error.localizedMessage.orEmpty()
                        showDialog = true
                    }
                })

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
    ) {


        TitleText(
            text = createAccount,
        )
        val modifier = Modifier.fillMaxWidth()
        OutlinedTextField(value = firstName,
            onValueChange = {
                firstName = it
                checkInputs()
            },
            label = { Text(text = stringResource(id = R.string.hint_first_name)) },
            singleLine = true,
            modifier = modifier,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            supportingText = {
                if (firstNameIsError) {
                    Text(
                        text = firstNameError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
        OutlinedTextField(value = lastName,
            onValueChange = {
                lastName = it
                checkInputs()
            },
            label = { Text(text = stringResource(id = R.string.hint_last_name)) },
            singleLine = true,
            modifier = modifier,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            supportingText = {
                if (lastNameIsError) {
                    Text(
                        text = lastNameError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
        OutlinedTextField(value = email,
            onValueChange = {
                email = it
                checkInputs()
            },
            label = { Text(text = stringResource(id = R.string.email_address)) },
            singleLine = true,
            modifier = modifier,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            supportingText = {
                if (emailIsError) {
                    Text(
                        text = emailNameError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
        OutlinedTextField(value = password,
            onValueChange = {
                password = it
                checkInputs()
            },
            label = { Text(text = stringResource(id = R.string.password)) },
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(
                        imageVector = if (passwordVisibility) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisibility) "Hide Password" else "Show Password"
                    )
                }
            },
            singleLine = true,
            modifier = modifier,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                checkInputsAndSubmit()
            }),
            supportingText = {
                if (passwordIsError) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = passwordError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
        Text(
            text = stringResource(id = R.string.password_error_length),
            color = if (pwLength) Color.Green else MaterialTheme.colorScheme.error,
            fontSize = integerResource(id = R.integer.text_size_extra_small).sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(id = R.string.password_error_uppercase),
            color = if (pwUppercase) Color.Green else MaterialTheme.colorScheme.error,
            fontSize = integerResource(id = R.integer.text_size_extra_small).sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(id = R.string.password_error_lowercase),
            color = if (pwLowercase) Color.Green else MaterialTheme.colorScheme.error,
            fontSize = integerResource(id = R.integer.text_size_extra_small).sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(id = R.string.password_error_numeral),
            color = if (pwNumber) Color.Green else MaterialTheme.colorScheme.error,
            fontSize = integerResource(id = R.integer.text_size_extra_small).sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(id = R.string.password_error_special),
            color = if (pwSpecial) Color.Green else MaterialTheme.colorScheme.error,
            fontSize = integerResource(id = R.integer.text_size_extra_small).sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        Button(
            onClick = {
                checkInputsAndSubmit()
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(containerColor = buttonBackground, disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Text(text = createAccount, fontSize = integerResource(id = R.integer.text_size).sp)
        }
        Text(
            text = stringResource(id = R.string.terms_string),
            fontSize = integerResource(id = R.integer.text_size_extra_small).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_small))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.already_have_account),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = integerResource(id = R.integer.text_size).sp,
                modifier = Modifier.padding(all = dimensionResource(id = R.dimen.padding_large))
            )
            Text(
                text = stringResource(id = R.string.sign_in),
                fontSize = integerResource(id = R.integer.text_size).sp,
                color = buttonBackground,
                modifier = Modifier
                    .padding(all = dimensionResource(id = R.dimen.padding_large))
                    .clickable { navigateToSignIn() }
            )
        }

        ErrorDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            errorMessage = errorMessage
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}