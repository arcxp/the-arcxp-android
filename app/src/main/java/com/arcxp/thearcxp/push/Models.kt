package com.arcxp.thearcxp.push

data class PushTopicSubscriptionItem(
    val sectionId: String,
    val subscribed: Boolean
)

sealed class NavigationDataItem
data class IntentNavigationDataItem(
    val uuid: String,
    val contentType: String
): NavigationDataItem()
object BackNavigationItem: NavigationDataItem()
object SettingsNavigationItem: NavigationDataItem()
object LoginNavigationItem: NavigationDataItem()
object PaywallArticleNavigationItem: NavigationDataItem()
object PaywallVideoNavigationItem: NavigationDataItem()
object DoNotNavigateItem: NavigationDataItem()



