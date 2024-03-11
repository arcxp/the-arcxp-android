package com.arcxp.thearcxp.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Success
import com.arcxp.content.extendedModels.title
import com.arcxp.content.models.ArcXPSection
import com.arcxp.thearcxp.LocalArticleViewModel
import com.arcxp.thearcxp.LocalMainViewModel
import com.arcxp.thearcxp.LocalVideoViewModel
import com.arcxp.thearcxp.LocalWebViewModel
import com.arcxp.thearcxp.MainActivity.Companion.CONTENT_TYPE_ARG_ARTICLE
import com.arcxp.thearcxp.MainActivity.Companion.CONTENT_TYPE_ARG_VIDEO
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.article.ArticleViewModel
import com.arcxp.thearcxp.push.BackNavigationItem
import com.arcxp.thearcxp.push.DoNotNavigateItem
import com.arcxp.thearcxp.push.IntentNavigationDataItem
import com.arcxp.thearcxp.push.LoginNavigationItem
import com.arcxp.thearcxp.push.NavigationDataItem
import com.arcxp.thearcxp.push.PaywallArticleNavigationItem
import com.arcxp.thearcxp.push.PaywallVideoNavigationItem
import com.arcxp.thearcxp.push.SettingsNavigationItem
import com.arcxp.thearcxp.ui.components.AppTitle
import com.arcxp.thearcxp.ui.navigation.AppDestination
import com.arcxp.thearcxp.ui.navigation.AppDestination.AccountWebScreen
import com.arcxp.thearcxp.ui.navigation.AppDestination.Article
import com.arcxp.thearcxp.ui.navigation.AppDestination.Home
import com.arcxp.thearcxp.ui.navigation.AppDestination.Search
import com.arcxp.thearcxp.ui.navigation.AppDestination.Settings
import com.arcxp.thearcxp.ui.navigation.AppDestination.Video
import com.arcxp.thearcxp.ui.navigation.AppDestination.VideoTab
import com.arcxp.thearcxp.ui.navigation.AppNavHost
import com.arcxp.thearcxp.ui.theme.AppTheme
import com.arcxp.thearcxp.ui.theme.alertBarActionText
import com.arcxp.thearcxp.ui.theme.alertBarBackground
import com.arcxp.thearcxp.ui.theme.alertBarText
import com.arcxp.thearcxp.utils.encodeUrl
import com.arcxp.thearcxp.utils.getNameToUseFromSection
import com.arcxp.thearcxp.utils.noRippleClickable
import com.arcxp.thearcxp.video.VideoViewModel
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.arcxp.thearcxp.web.WebViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp(
    mainViewModel: MainViewModel = LocalMainViewModel.current,
    articleViewModel: ArticleViewModel = LocalArticleViewModel.current,
    videoViewModel: VideoViewModel = LocalVideoViewModel.current,
    webViewModel: WebViewModel = LocalWebViewModel.current
) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val alertBar = remember { SnackbarHostState() }
    val context = LocalContext.current


    val alertBarState by mainViewModel.alertBarState().collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(alertBarState) {
        alertBarState?.let {
            val result = alertBar.showSnackbar(
                message = it.title(),
                duration = SnackbarDuration.Indefinite,
                actionLabel = context.getString(R.string.open_x, it.type),
                withDismissAction = true
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    val navItem = IntentNavigationDataItem(
                        uuid = it._id,
                        contentType = it.type
                    )
                    mainViewModel.triggerNavigation(navItem)
                }

                SnackbarResult.Dismissed -> {
                }
            }
        }
    }


    DisposableEffect(mainViewModel.navigateTo) {
        val observer = Observer<NavigationDataItem> { value ->
            when (value) {
                is IntentNavigationDataItem -> {
                    if (value.uuid.isNotEmpty()) {
                        if (value.contentType == CONTENT_TYPE_ARG_ARTICLE) {
                            navController.navigateToArticle(value.uuid)
                        } else if (value.contentType == CONTENT_TYPE_ARG_VIDEO) {
                            navController.navigateToVideo(value.uuid)
                        }
                        // Clear navigation event
                        mainViewModel.triggerNavigation(DoNotNavigateItem)
                    }
                }

                is BackNavigationItem -> navController.popBackStack()
                is LoginNavigationItem -> navController.popBackStack(
                    AppDestination.SignIn.route,
                    inclusive = false
                )

                is SettingsNavigationItem -> navController.popBackStack(
                    Settings.route,
                    inclusive = false
                )

                is PaywallArticleNavigationItem -> navController.popBackStack(
                    Article.routeWithArgs,
                    inclusive = false
                )

                is PaywallVideoNavigationItem -> navController.popBackStack(
                    Video.routeWithArgs,
                    inclusive = false
                )

                is DoNotNavigateItem -> {}
            }
        }
        mainViewModel.navigateTo.observe(lifecycleOwner, observer)

        onDispose {
            mainViewModel.navigateTo.removeObserver(observer)
        }
    }

    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current

    var barsVisible by remember { mutableStateOf(true) }

    val videoHideBars by videoViewModel.hideBars.observeAsState()
    val articleHideBars by articleViewModel.hideBars.observeAsState()
    val webHideBars by webViewModel.hideBars.observeAsState()
    fun splashScreenOff() =
        currentDestination.value?.destination?.route != AppDestination.SplashLogoScreen.route

    barsVisible = articleHideBars == false
            && videoHideBars == false
            && webHideBars == false
            && splashScreenOff()

    AppTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                //this should clear focus on text fields when clicking background on all screens
                .noRippleClickable { focusManager.clearFocus() },
            color = MaterialTheme.colorScheme.background
        ) {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = barsVisible,
                drawerContent = {
                    AppNavigationDrawer(
                        mainViewModel = mainViewModel,
                        drawerState = drawerState,
                        onHomeNavigate = { navController.navigate(Home.route) }
                    )
                },
                content = {
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = alertBar) {
                                Snackbar(
                                    snackbarData = it,
                                    containerColor = alertBarBackground,
                                    contentColor = alertBarText,
                                    actionColor = alertBarActionText,
                                )
                            }
                        },
                        topBar = {
                            AnimatedVisibility(
                                visible = barsVisible
                            )
                            {
                                CenterAlignedTopAppBar(
                                    title = {
                                        AppTitle(
                                            fontSize = integerResource(id = R.integer.text_size).sp
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    drawerState.open()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Menu,
                                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                                contentDescription = "Drawer Icon"
                                            )
                                        }
                                    },
                                    actions = {
                                        var hasFocus by remember { mutableStateOf(false) }
                                        // this defines the search bar width as fraction of device,
                                        // and expanded width when focused
                                        // when empty is more restricted
                                        val widthFraction by animateFloatAsState(
                                            targetValue = if (hasFocus) 0.65f else {
                                                if (searchText.isEmpty()) 0.1f else 0.35f
                                            },
                                            label = ""
                                        )

                                        // if we don't set this to null, it will reserve space
                                        // for the close button even when not visible
                                        val trailingIcon: @Composable (() -> Unit)? =
                                            if (searchText.isNotEmpty() && hasFocus) {
                                                {
                                                    IconButton(onClick = { searchText = "" }) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Clear,
                                                            contentDescription = "Clear text"
                                                        )
                                                    }
                                                }
                                            } else null
                                        TextField(
                                            value = searchText,
                                            onValueChange = { searchText = it },
                                            modifier = Modifier
                                                .fillMaxWidth(widthFraction)
                                                .padding(end = dimensionResource(id = R.dimen.padding_small))
                                                .onFocusChanged { hasFocus = it.isFocused },

                                            textStyle = TextStyle(
                                                textAlign = TextAlign.Start,
                                                color = MaterialTheme.colorScheme.inverseOnSurface
                                            ),
                                            keyboardOptions = KeyboardOptions.Default.copy(
                                                imeAction = ImeAction.Search
                                            ),
                                            keyboardActions = KeyboardActions(onSearch = {
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                                if (searchText.isNotEmpty()) {
                                                    navController.search(terms = searchText)
                                                }
                                            }),
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.inverseOnSurface
                                                )
                                            },
                                            trailingIcon = trailingIcon,
                                            singleLine = true,
                                            interactionSource = interactionSource,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.inverseSurface,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.inverseSurface
                                            )
                                        )

                                    },
                                    colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
                                )
                            }
                        },
                        bottomBar = {
                            AnimatedVisibility(
                                visible = barsVisible
                            )
                            {
                                AppBottomNavigation(navController = navController)
                            }
                        },

                        ) {
                        AppNavHost(
                            navHostController = navController,
                            modifier = Modifier.padding(it),
                        )
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
)

@Composable
fun AppNavigationDrawer(
    mainViewModel: MainViewModel,
    drawerState: DrawerState,
    onHomeNavigate: () -> Unit = {}
) {

    val sectionsResult: Either<ArcXPException, List<ArcXPSection>>? by mainViewModel.sectionsListEvent.observeAsState()

    val pageSelected: Int? by mainViewModel.pageEvent.observeAsState()

    when (sectionsResult) {
        is Success -> {
            val items = mutableListOf<ArcXPSection>()

            (sectionsResult as Success<List<ArcXPSection>>).success.forEach { section ->
                items.add(section)
            }
            if (items.isNotEmpty()) {
                val selectedItem = remember { mutableStateOf(items[0]) }

                val coroutineScope = rememberCoroutineScope()

                mainViewModel.pageEvent.observe(LocalLifecycleOwner.current) {
                    coroutineScope.launch {
                        if (pageSelected != null) {
                            val index = pageSelected ?: 0
                            val section = mainViewModel.sectionsIndexMap[index]
                            val sectionObject = mainViewModel.sections[section]
                            selectedItem.value = sectionObject!!
                            mainViewModel.pageSelected(null)
                        }
                    }
                }

                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.inverseSurface,
                    drawerContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    content = {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AppTitle(
                                fontSize = integerResource(id = R.integer.text_size_small).sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.close() } }
                            ) {
                                Image(
                                    painterResource(R.drawable.ic_baseline_clear_24),
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inverseOnSurface),
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.button_icon_size))
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                        items.forEach { section ->
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = section.getNameToUseFromSection(),
                                        color = MaterialTheme.colorScheme.inverseOnSurface
                                    )
                                },
                                selected = section == selectedItem.value,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    selectedItem.value = section
                                    mainViewModel.sectionSelected(section)
                                    onHomeNavigate()
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                shape = MaterialTheme.shapes.small,
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedTextColor = MaterialTheme.colorScheme.inverseOnSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    selectedTextColor = MaterialTheme.colorScheme.inverseOnSurface,
                                ),
                            )
                        }
                    }

                )
            }
        }

        else -> {

        }
    }
}


@Composable
fun AppBottomNavigation(navController: NavHostController) {

    val homeDescription = stringResource(id = R.string.home)
    val videoDescription = stringResource(id = R.string.videos)
    val accountDescription = stringResource(id = R.string.account)
    val backStackEntry = navController.currentBackStackEntryAsState()

    val bottomNavItems = listOf(
        BottomNavItem(
            name = homeDescription,
            route = Home.route,
            icon = Icons.Rounded.Home,
        ),
        BottomNavItem(
            name = videoDescription,
            route = VideoTab.route,
            icon = Icons.Rounded.VideoLibrary,
        ),
        BottomNavItem(
            name = accountDescription,
            route = Settings.route,
            icon = Icons.Rounded.Settings,
        ),
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.inverseSurface) {
        bottomNavItems.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route

            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigate(item.route) },
                label = {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.tertiary
                    )
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        tint = if (selected) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.tertiary,
                        contentDescription = "${item.name} Icon",
                    )
                }
            )
        }
    }
}

fun NavHostController.navigateToArticle(uuid: String) = navigate("${Article.route}/$uuid")
fun NavHostController.navigateToVideo(uuid: String) = navigate("${Video.route}/$uuid")
fun NavHostController.search(terms: String) = navigate("${Search.route}/$terms")

fun NavHostController.navigateToAccountWebScreen(url: String, enableJavascript: Boolean) {
    val encodedUrl = encodeUrl(url)
    this.navigate("${AccountWebScreen.route}/$encodedUrl/$enableJavascript") {
        popUpTo(AccountWebScreen.route) {
            inclusive = true
        }
        launchSingleTop = true
    }
}