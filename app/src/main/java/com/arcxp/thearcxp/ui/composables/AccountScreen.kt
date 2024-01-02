package com.arcxp.thearcxp.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.util.Success
import com.arcxp.thearcxp.LocalAccountViewModel
import com.arcxp.thearcxp.LocalFirebaseAnalyticsManager
import com.arcxp.thearcxp.LocalMainViewModel
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.AccountViewModel
import com.arcxp.thearcxp.analytics.FirebaseAnalyticsManager
import com.arcxp.thearcxp.ui.components.DirectionalContent
import com.arcxp.thearcxp.ui.theme.AppTheme
import com.arcxp.thearcxp.utils.OnLifecycleEvent
import com.arcxp.thearcxp.utils.getPushTopicName
import com.arcxp.thearcxp.viewmodel.MainViewModel

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = LocalAccountViewModel.current,
    openCreateAccount: () -> Unit = {},
    openSignIn: () -> Unit = {},
    openChangePassword: () -> Unit = {},
    openWebViewWithUrl: (String, Boolean) -> Unit = { s: String, b: Boolean-> },
    openPreferences: () -> Unit = {},
    analyticsManager: FirebaseAnalyticsManager? = LocalFirebaseAnalyticsManager.current
) {
    val isLoggedIn by accountViewModel.isLoggedIn.observeAsState()

    LaunchedEffect(true) {
        accountViewModel.checkSession()
    }

    OnLifecycleEvent(onEvent = { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            analyticsManager?.logScreenView("AccountScreen", "AccountScreen")
        }
    })

    Column(
        modifier = modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_large))
    ) {
        if (ArcXPMobileSDK.commerceInitialized()) {
            AccountHeader(textId = R.string.account)
            if (isLoggedIn == true) {
                accountViewModel.getUserProfile()
                val userProfile by accountViewModel.userProfile.observeAsState()

                userProfile?.let { profileResult ->
                    when (profileResult) {
                        is Success -> {
                            val result = profileResult.success
                            Text(
                                text = stringResource(
                                    R.string.user_name,
                                    result.firstName.orEmpty(),
                                    result.lastName.orEmpty()
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.space))
                            )
                        }

                        else -> {}
                    }
                }
                AccountSection(
                    textId = R.string.change_password,
                    onNextClick = openChangePassword
                )
                AccountSection(
                    textId = R.string.logout,
                    onNextClick = { accountViewModel.logout() }
                )
            } else {
                AccountSection(
                    textId = R.string.create_account,
                    onNextClick = openCreateAccount
                )
                AccountSection(
                    textId = R.string.login,
                    onNextClick = openSignIn)
            }
        }
        if (!ArcXPMobileSDK.commerceManager().sessionIsActive()) {
            accountViewModel.logout()
        }

        if (accountViewModel.registeredForPushNotifications) {
            AccountHeader(textId = R.string.preferences)
            AccountSection(
                textId = R.string.push_notifications,
                onNextClick = openPreferences)
        }

        AccountHeader(textId = R.string.policies)
        val tosUrl = stringResource(id = R.string.tos_url)
        val privacyPolicyUrl = stringResource(id = R.string.pp_url)
        AccountSection(
            textId = R.string.terms_of_service,
            onNextClick = { openWebViewWithUrl(tosUrl, false) })
        AccountSection(
            textId = R.string.privacy_policy,
            onNextClick = { openWebViewWithUrl(privacyPolicyUrl, true) })

        AccountHeader(textId = R.string.software_versions)
        Text(
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.padding_large),
                    top = dimensionResource(id = R.dimen.padding_small)
                ),
            text = stringResource(id = R.string.app_version),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.padding_large),
                    top = dimensionResource(id = R.dimen.space_xsmall)
                ),
            text = stringResource(
                id = R.string.SDK_colon_version,
                ArcXPMobileSDK.getVersion(LocalContext.current.applicationContext)
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AccountSection(textId: Int, onNextClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.space))
            .clickable(onClick = onNextClick),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = textId),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Image(
            painter = painterResource(R.drawable.ic_chevron_right_24),
            contentDescription = stringResource(id = textId),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun AccountHeader(textId: Int) {
    Text(
        text = stringResource(id = textId),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .padding(top = dimensionResource(id = R.dimen.padding_small))
    )
}

@Composable
fun PreferenceScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = LocalMainViewModel.current,
) {
    val switchList = mutableListOf<Boolean>().apply {
        mainViewModel.sectionsIndexMap.toSortedMap().values.forEachIndexed { _, title ->
            val section = mainViewModel.sections[title]
            val item = mainViewModel.pushNotificationsTopicSubscriptions[section!!.getPushTopicName()]
            val checked = item?.subscribed ?: false
            add(checked)
        }
    }
    val switchStates = remember { switchList.toMutableStateList() }
    val allSwitchState = remember { mutableStateOf(mainViewModel.pushNotificationsTopicSubscriptions["all"]!!.subscribed) }

    PreferenceSwitches(
        allSwitchState = allSwitchState,
        switchStates = switchStates,
        mainViewModel = mainViewModel,
        modifier = modifier)
}

@Composable
fun PreferenceSwitches(
    allSwitchState: MutableState<Boolean>,
    switchStates: SnapshotStateList<Boolean>,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(
                start = dimensionResource(id = R.dimen.padding_large),
                end = dimensionResource(id = R.dimen.padding_large),
                top = dimensionResource(id = R.dimen.padding_large)
            )
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.push_categories_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        DirectionalContent {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(
                            id = R.dimen.padding_large
                        ),
                        vertical = dimensionResource(
                            id = R.dimen.padding_small
                        )
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.all_categories),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                )
                Switch(
                    checked = allSwitchState.value,
                    onCheckedChange = { isChecked ->
                        allSwitchState.value = isChecked
                        mainViewModel.sectionsIndexMap.toSortedMap().forEach {
                            switchStates[it.key] = isChecked
                            mainViewModel.saveSubscribeToAll(isChecked)
                            val sectionId = mainViewModel.sections[it.value]!!.getPushTopicName()
                            if (isChecked) {
                                mainViewModel.subscribeToPushNotificationTopic(it.value, sectionId.toString())
                            } else {
                                mainViewModel.unsubscribeFromPushNotificationTopic(sectionId.toString())
                            }
                        }
                    }
                )
            }
        }

        mainViewModel.sectionsIndexMap.toSortedMap().forEach {
            val sectionId = mainViewModel.sections[it.value]!!.getPushTopicName()
            DirectionalContent {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(
                                id = R.dimen.padding_large
                            ),
                            vertical = dimensionResource(
                                id = R.dimen.padding_small
                            )
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = it.value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                    )
                    Switch(
                        checked = switchStates[it.key],
                        onCheckedChange = { isChecked ->
                            switchStates[it.key] = isChecked
                            if (isChecked) {
                                mainViewModel.subscribeToPushNotificationTopic(it.value, sectionId.toString())
                            } else {
                                mainViewModel.unsubscribeFromPushNotificationTopic(sectionId.toString())
                                allSwitchState.value = false
                                mainViewModel.saveSubscribeToAll(false)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight",
    showBackground = true
)

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark",
    showBackground = true
)

@Composable
fun AccountPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AccountScreen()
        }
    }
}

