package com.arcxp.thearcxp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arcxp.thearcxp.LocalArticleViewModel
import com.arcxp.thearcxp.LocalMainViewModel
import com.arcxp.thearcxp.LocalVideoViewModel
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.article.ArticleMainScreen
import com.arcxp.thearcxp.article.ArticleViewModel
import com.arcxp.thearcxp.home.HomeScreen
import com.arcxp.thearcxp.push.LoginNavigationItem
import com.arcxp.thearcxp.push.PaywallArticleNavigationItem
import com.arcxp.thearcxp.push.PaywallVideoNavigationItem
import com.arcxp.thearcxp.push.SettingsNavigationItem
import com.arcxp.thearcxp.ui.composables.AccountScreen
import com.arcxp.thearcxp.ui.composables.ChangePassword
import com.arcxp.thearcxp.ui.composables.ChangePasswordSuccess
import com.arcxp.thearcxp.ui.composables.CreateAccount
import com.arcxp.thearcxp.ui.composables.ForgotPassword
import com.arcxp.thearcxp.ui.composables.ForgotPasswordSuccess
import com.arcxp.thearcxp.ui.composables.LoginScreen
import com.arcxp.thearcxp.ui.composables.PreferenceScreen
import com.arcxp.thearcxp.ui.composables.SectionList
import com.arcxp.thearcxp.ui.composables.SectionType
import com.arcxp.thearcxp.ui.composables.SignUpScreen
import com.arcxp.thearcxp.ui.composables.SignUpSuccess
import com.arcxp.thearcxp.ui.composables.SplashScreen
import com.arcxp.thearcxp.ui.composables.navigateToAccountWebScreen
import com.arcxp.thearcxp.ui.composables.navigateToArticle
import com.arcxp.thearcxp.ui.composables.navigateToVideo
import com.arcxp.thearcxp.ui.navigation.AppDestination.AccountWebScreen
import com.arcxp.thearcxp.ui.navigation.AppDestination.Article
import com.arcxp.thearcxp.ui.navigation.AppDestination.ChangePassword
import com.arcxp.thearcxp.ui.navigation.AppDestination.ChangePasswordSuccess
import com.arcxp.thearcxp.ui.navigation.AppDestination.CreateAccount
import com.arcxp.thearcxp.ui.navigation.AppDestination.ForgotPassword
import com.arcxp.thearcxp.ui.navigation.AppDestination.ForgotPasswordSuccess
import com.arcxp.thearcxp.ui.navigation.AppDestination.Home
import com.arcxp.thearcxp.ui.navigation.AppDestination.PaywallArticleSignIn
import com.arcxp.thearcxp.ui.navigation.AppDestination.PaywallVideoSignIn
import com.arcxp.thearcxp.ui.navigation.AppDestination.PreferencesScreen
import com.arcxp.thearcxp.ui.navigation.AppDestination.Search
import com.arcxp.thearcxp.ui.navigation.AppDestination.Settings
import com.arcxp.thearcxp.ui.navigation.AppDestination.SignIn
import com.arcxp.thearcxp.ui.navigation.AppDestination.SignUp
import com.arcxp.thearcxp.ui.navigation.AppDestination.SignUpSuccessScreen
import com.arcxp.thearcxp.ui.navigation.AppDestination.SplashLogoScreen
import com.arcxp.thearcxp.ui.navigation.AppDestination.Video
import com.arcxp.thearcxp.ui.navigation.AppDestination.VideoTab
import com.arcxp.thearcxp.utils.decodeUrl
import com.arcxp.thearcxp.video.VideoMainScreen
import com.arcxp.thearcxp.video.VideoViewModel
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.arcxp.thearcxp.web.WebViewScreen

@Composable
fun AppNavHost(
    navHostController: NavHostController,
    modifier: Modifier,
    mainViewModel: MainViewModel = LocalMainViewModel.current,
    articleViewModel: ArticleViewModel = LocalArticleViewModel.current,
    videoViewModel: VideoViewModel = LocalVideoViewModel.current
) {
    NavHost(
        navController = navHostController,
        startDestination = SplashLogoScreen.route,
        modifier = modifier
    ) {
        composable(route = Home.route) {
            HomeScreen(
                openArticle = navHostController::navigateToArticle,
                openVideo = navHostController::navigateToVideo,
            )
        }
        composable(route = CreateAccount.route) {
            CreateAccount(
                navigateToSignIn = { navHostController.navigate(route = SignIn.route) },
                navigateToSignUp = { navHostController.navigate(route = SignUp.route) }
            )
        }
        composable(route = SignUpSuccessScreen.route) {
            SignUpSuccess(
                navigateToSignIn = {
                    navHostController.navigate(route = SignIn.route)
                }
            )
        }
        composable(route = PaywallArticleSignIn.route) {
            LoginScreen(
                openForgotPassword = { navHostController.navigate(route = ForgotPassword.route) },
                openCreateAccount = { navHostController.navigate(route = CreateAccount.route) },
                navigation = { mainViewModel.triggerNavigation(destination = PaywallArticleNavigationItem) }
            )
        }
        composable(route = PaywallVideoSignIn.route) {
            LoginScreen(
                openForgotPassword = { navHostController.navigate(route = ForgotPassword.route) },
                openCreateAccount = { navHostController.navigate(route = CreateAccount.route) },
                navigation = { mainViewModel.triggerNavigation(destination = PaywallVideoNavigationItem) }
            )
        }
        composable(route = SignIn.route) {
            LoginScreen(
                openForgotPassword = { navHostController.navigate(route = ForgotPassword.route) },
                openCreateAccount = { navHostController.navigate(route = CreateAccount.route) },
                navigation = { mainViewModel.triggerNavigation(destination = SettingsNavigationItem) }
            )
        }
        composable(route = SignUp.route) {
            SignUpScreen(
                backNavigation = navHostController::popBackStack,
                navigateToSignUpSuccess = { navHostController.navigate(route = SignUpSuccessScreen.route) },
                navigateToSignIn = { navHostController.navigate(route = SignIn.route) })
        }
        composable(route = VideoTab.route) {
            //make video collection in advance with videos to pull from
            SectionList(
                collectionFlow = mainViewModel.getCollection(collectionAlias = stringResource(id = R.string.video_endpoint)),
                sectionType = SectionType.VIDEO,
                openVideo = navHostController::navigateToVideo
            )
        }
        composable(route = Settings.route) {
            AccountScreen(
                openCreateAccount = {
                    navHostController.navigate(route = CreateAccount.route)
                },
                openSignIn = {
                    navHostController.navigate(route = SignIn.route)
                },
                openChangePassword = {
                    navHostController.navigate(route = ChangePassword.route)
                },
                openWebViewWithUrl = navHostController::navigateToAccountWebScreen,
                openPreferences = {
                    navHostController.navigate(PreferencesScreen.route)
                }
            )
        }
        composable(
            route = Article.routeWithArgs,
            arguments = Article.arguments,
            deepLinks = Article.deepLinks
        ) {
            articleViewModel.getStory(id = it.arguments?.getString(Article.uuidArg).orEmpty())

            ArticleMainScreen(
                openCreateAccount = { navHostController.navigate(route = CreateAccount.route) },
                openSignIn = { navHostController.navigate(route = PaywallArticleSignIn.route) },
                backNavigation = navHostController::popBackStack,
            )
        }
        composable(
            route = Search.routeWithArgs,
            arguments = Search.arguments,
            deepLinks = Search.deepLinks
        ) {
            SectionList(
                collectionFlow = mainViewModel.getSearchResults(
                    searchTerms = it.arguments?.getString(
                        Search.searchTermsArgument
                    ).orEmpty()
                ),
                sectionType = SectionType.SEARCH,
                openArticle = navHostController::navigateToArticle,
                openVideo = navHostController::navigateToVideo
            )
        }
        composable(
            route = AccountWebScreen.routeWithArgs,
            arguments = AccountWebScreen.arguments
        ) {
            //having extra slashes in a route mess things up, so we encode/decode url in transit
            WebViewScreen(
                url = it.arguments?.getString(AccountWebScreen.urlArg).let { encodedUrl ->
                    decodeUrl(encodedUrl.orEmpty())
                },
                showTopBar = true,
                hideNavBars = true,
                backNavigation = navHostController::popBackStack,
                enableJavascript = it.arguments?.getBoolean(AccountWebScreen.javascriptArg) ?: true
            )
        }
        composable(
            route = Video.routeWithArgs,
            arguments = Video.arguments,
            deepLinks = Video.deepLinks
        ) {
            val uuid = it.arguments?.getString(Video.uuidArg).orEmpty()
            videoViewModel.fetchVideo(id = uuid)
            VideoMainScreen(
                videoViewModel = videoViewModel,
                openCreateAccount = { navHostController.navigate(route = CreateAccount.route) },
                openSignIn = { navHostController.navigate(route = PaywallVideoSignIn.route) },
                backNavigation = navHostController::popBackStack
            )
        }
        composable(
            route = ForgotPassword.route
        ) {
            ForgotPassword(
                successNavigation = { navHostController.navigate(ForgotPasswordSuccess.route) }
            )
        }
        composable(
            route = ForgotPasswordSuccess.route
        ) {
            ForgotPasswordSuccess(
                navigateToSignIn = { mainViewModel.triggerNavigation(LoginNavigationItem) }
            )
        }
        composable(route = ChangePassword.route) {
            ChangePassword(
                changePasswordSuccess = { navHostController.navigate(ChangePasswordSuccess.route)}
            )
        }
        composable(route = ChangePasswordSuccess.route) {
            ChangePasswordSuccess(
                navigateBack = { mainViewModel.triggerNavigation(SettingsNavigationItem) }
            )
        }

        composable(route = PreferencesScreen.route) {
            PreferenceScreen()
        }
        composable(route = SplashLogoScreen.route) {
            SplashScreen(navigateHome = {
                navHostController.navigate(Home.route) {
                    popUpTo(SplashLogoScreen.route) { inclusive = true }
                }
            })
        }
    }
}
