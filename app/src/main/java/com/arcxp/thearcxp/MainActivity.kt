package com.arcxp.thearcxp

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.SearchView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.size
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.arcxp.content.sdk.models.ArcXPSection
import com.arcxp.content.sdk.util.Failure
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.ArcXPWidget.Companion.WIDGET_ARTICLE_ID_KEY
import com.arcxp.thearcxp.account.CreateAccountFragment
import com.arcxp.thearcxp.databinding.ActivityMainBinding
import com.arcxp.thearcxp.tabfragment.*
import com.arcxp.thearcxp.utils.AnsTypes
import com.arcxp.thearcxp.utils.collectOneTimeEvent
import com.arcxp.thearcxp.utils.getNameToUseFromSection
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.arcxp.thearcxp.viewmodel.MainViewModel.FragmentView.*
import com.google.android.material.navigation.NavigationView

/**
 * The only activity in the app.
 */
@SuppressLint("SourceLockedOrientationActivity")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var drawer: DrawerLayout
    private lateinit var toolbar: Toolbar

    private lateinit var navigationView: NavigationView

    private lateinit var vm: MainViewModel

    private var isFragmentTransactionSafe = true

    private lateinit var searchViewItem: MenuItem
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = ViewModelProvider(this).get(
            MainViewModel::class.java
        )

        if (vm.isStartup) {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    binding.splashscreen.visibility = GONE
                    vm.isStartup = false
                },
                2000
            )
        } else {
            binding.splashscreen.visibility = GONE
        }

        //Orientation is handled manually by the app.  The only fragment that is not
        //locked in portrait is PlayVideoFragment.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        drawer = binding.drawerLayout
        toolbar = binding.toolbar

        navigationView = binding.navView

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()

        //If returning from the paywall sign-in/registration flow then
        //this method is used to return to the last article that was shown
        vm.openLastArticleEvent.observe(this, this::returnToViewFromPaywall)

        vm.sectionsListEvent.observe(this) {
            when (it) {
                is Success -> setupDrawer(it.success)
                is Failure -> {
                    //error message handled by home fragment
                }
            }

        }

        vm.getSectionList(this)
        collectOneTimeEvent(flow = vm.openArticleEvent, collect = ::openArticle)
        collectOneTimeEvent(flow = vm.openVideoEvent, collect = ::openVideo)

        vm.sensorLockEvent.observe(this, this::sensorLock)

        supportFragmentManager.addOnBackStackChangedListener {
            checkFragments()
        }

        init()

        // if opening from the widget
        openArticleFromWidget()
    }

    private fun returnToViewFromPaywall(item: Pair<String, String>) {
        val contentType = item.first
        val contentId = item.second
        when (contentType) {
            AnsTypes.STORY.type -> openArticle(contentId)
            AnsTypes.VIDEO.type -> openVideo(contentId)
        }
        vm.clearLastView()
    }

    private fun setupDrawer(sections: List<ArcXPSection>) {

        val hView = View.inflate(this, R.layout.drawer_header, null)
        val closeButton = hView.findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
        }
        val hv = navigationView.getHeaderView(0)
        navigationView.removeHeaderView(hv)
        navigationView.addHeaderView(hView)

        navigationView.menu.clear()
        val menu = navigationView.menu

        sections.forEach {
            menu.add(it.getNameToUseFromSection())

            val menuItem = menu.getItem(menu.size - 1)
            menuItem.setActionView(R.layout.drawer_item)
        }
        navigationView.invalidate()

        navigationView.setNavigationItemSelectedListener {

            val section = vm.sections[it.title]
            if (section != null) {
                vm.sectionSelected(section)
                binding.bottomNavigationView.selectedItemId = R.id.home
                drawer.close()
                true
            } else {
                false
            }

        }
    }

    private fun init() {
        when (vm.currentFragmentTag) {
            HOME -> setCurrentFragment(HomeFragment(), HOME)
            VIDEO -> setCurrentFragment(VideoFragment(), VIDEO)
            ACCOUNT -> setCurrentFragment(AccountFragment(), ACCOUNT)
        }
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(HomeFragment(), HOME)
                R.id.video -> setCurrentFragment(VideoFragment(), VIDEO)
                R.id.account -> setCurrentFragment(AccountFragment(), ACCOUNT)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment, view: MainViewModel.FragmentView) {
        vm.currentFragmentTag = view
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, fragment, view.tag)
            commit()
        }
    }

    fun getFragmentContainerViewId() = R.id.nav_host_fragment

    override fun onResumeFragments() {
        super.onResumeFragments()
        checkFragments()
    }

    private fun checkFragments() {
        val noShowBottomNavBar = setOf(
            getString(R.string.playvideo),
            getString(R.string.article),
            getString(R.string.sign_up),
            getString(R.string.sign_in),
            getString(R.string.login),
            getString(R.string.create_account),
            getString(R.string.forgot_password),
            getString(R.string.web_section)
        )
        val unlockDrawer = setOf(
            getString(R.string.videotag),
            getString(R.string.hometag),
            getString(R.string.accounttag),
            getString(R.string.sign_up),
            getString(R.string.sign_in),
            getString(R.string.login),
            getString(R.string.create_account),
            getString(R.string.forgot_password),
            getString(R.string.web_section),
            getString(R.string.paywall),
            getString(R.string.glide_manager)
        )
        val current = supportFragmentManager.fragments.last()
        if (noShowBottomNavBar.contains(current.tag)) {
            binding.bottomNavigationView.visibility = GONE
        } else {
            binding.bottomNavigationView.visibility = VISIBLE
        }
        sensorLock(rotationOn = getString(R.string.playvideo) == current.tag)
        unlockDrawer(unlock = unlockDrawer.contains(current.tag))
    }

    fun openFragment(
        fragment: Fragment,
        addToBackStack: Boolean = false,
        tag: String
    ) {
        if (isFinishing || !isFragmentTransactionSafe) {
            return
        }
        if (getFragmentContainerViewId() == 0) {
            return
        }
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
            .replace(getFragmentContainerViewId(), fragment, tag)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
        }
        fragmentTransaction.commit()
        checkFragments()
    }

    override fun onStart() {
        super.onStart()
        isFragmentTransactionSafe = true
    }

    override fun onStop() {
        isFragmentTransactionSafe = false
        super.onStop()
    }

    fun navigateToSignIn() {
        supportFragmentManager.popBackStack()
        openFragment(LoginFragment(), true, getString(R.string.sign_in))
    }

    fun navigateToCreateAccount() {
        supportFragmentManager.popBackStack()
        openFragment(CreateAccountFragment(), true, getString(R.string.create_account))
    }

    private fun openArticle(id: String) {
        val fragment = ArticleFragment.newInstance(id = id)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.contentView, fragment, getString(R.string.article))
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

    private fun openVideo(id: String) {
        val fragment = PlayVideoFragment.newInstance(id = id)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.contentView, fragment, getString(R.string.playvideo))
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

    fun openSearchResults(query: String) {
        val fragment = SearchResultsFragment.create(query)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.contentView, fragment, getString(R.string.searchtag))
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        searchViewItem = menu.findItem(R.id.app_bar_search)
        searchView = MenuItemCompat.getActionView(searchViewItem) as SearchView

        searchView.setOnCloseListener {
            binding.appTitleTextView.visibility = VISIBLE
            false
        }

        searchView.setOnSearchClickListener {
            binding.appTitleTextView.visibility = GONE
        }

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {

                    openSearchResults(query)
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        return super.onCreateOptionsMenu(menu)
    }

    private fun openArticleFromWidget() {
        val extras = intent.extras
        if (extras != null) {
            val articleId = extras.getString(WIDGET_ARTICLE_ID_KEY)
            if (articleId != null) {
                this.openArticle(articleId)
            }
        }
    }

    fun clearSearch() {
        searchView.setQuery("", false)
        searchView.isIconified = true
    }

    private fun unlockDrawer(unlock: Boolean) {
        binding.drawerLayout.setDrawerLockMode(if (unlock) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun sensorLock(rotationOn: Boolean) {
        requestedOrientation =
            if (rotationOn) ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}