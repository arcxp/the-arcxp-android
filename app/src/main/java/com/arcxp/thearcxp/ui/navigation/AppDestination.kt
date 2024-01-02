package com.arcxp.thearcxp.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

const val appName = "arcxp"//TODO update from somewhere maybe

sealed class AppDestination {
    abstract val route: String

    object Home : AppDestination() {
        override val route = "home"
    }
    object CreateAccount : AppDestination() {
        override val route = "create_account"
    }
    object SignIn : AppDestination() {
        override val route = "sign_in"
    }

    object PaywallArticleSignIn : AppDestination() {
        override val route = "paywall_article_sign_in"
    }

    object PaywallVideoSignIn : AppDestination() {
        override val route = "paywall_video_sign_in"
    }

    object SignUp : AppDestination() {
        override val route = "sign_up"
    }
    object SignUpSuccessScreen : AppDestination() {
        override val route = "sign_up_success"
    }

    object WebScreen : AppDestination() {
        override val route = "web_screen"
        const val urlArg = "url"
    }

    object AccountWebScreen : AppDestination() {
        override val route = "account_web_screen"
        const val urlArg = "url"
        const val javascriptArg = "javascript"
        val routeWithArgs = "${route}/{${urlArg}}/{${javascriptArg}}"

        val arguments = listOf(
            navArgument(urlArg) { type = NavType.StringType },
            navArgument(javascriptArg) { type = NavType.BoolType}
        )
    }

    object Article : AppDestination() {
        override val route = "article"
        const val uuidArg = "uuid"

        val routeWithArgs = "${route}/{${uuidArg}}"
        val deepLinks = listOf(
            navDeepLink { uriPattern = "$appName://$route/{$uuidArg}" }
        )
        val arguments = listOf(
            navArgument(uuidArg) { type = NavType.StringType }
        )
    }

    object Video : AppDestination() {
        override val route = "video"
        const val uuidArg = "uuid"

        val routeWithArgs = "${route}/{${uuidArg}}"
        val deepLinks = listOf(
            navDeepLink { uriPattern = "$appName://$route/{$uuidArg}" }
        )
        val arguments = listOf(
            navArgument(uuidArg) { type = NavType.StringType }
        )
    }
    object Search : AppDestination() {
        override val route = "search"
        const val searchTermsArgument = "search_terms"

        val routeWithArgs = "${route}/{${searchTermsArgument}}"
        val deepLinks = listOf(
            navDeepLink { uriPattern = "$appName://$route/{$searchTermsArgument}" }
        )
        val arguments = listOf(
            navArgument(searchTermsArgument) { type = NavType.StringType }
        )
    }

    object Settings : AppDestination() {
        override val route = "settings"
    }
    object ForgotPassword : AppDestination() {
        override val route = "forgot_password"
    }
    object ForgotPasswordSuccess : AppDestination() {
        override val route = "forgot_password_success"
    }
    object ChangePassword : AppDestination() {
        override val route = "change_password"
    }
    object ChangePasswordSuccess : AppDestination() {
        override val route = "change_password_success"
    }
    object VideoTab : AppDestination() {
        override val route = "video_tab"
    }
    object PreferencesScreen : AppDestination() {
        override val route = "preferences_screen"
    }
    object SplashLogoScreen : AppDestination() {
        override val route = "splash"
    }
}


