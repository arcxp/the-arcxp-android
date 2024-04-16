package com.arcxp.thearcxp.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.models.ArcXPSection
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.push.NavigationDataItem
import com.arcxp.thearcxp.push.PushTopicSubscriptionItem
import com.arcxp.thearcxp.tabfragment.WebSectionFragment
import com.arcxp.thearcxp.ui.paging.CollectionPagingSource
import com.arcxp.thearcxp.ui.paging.SearchResultsPagingSource
import com.arcxp.thearcxp.utils.getNameToUseFromSection
import com.facebook.login.widget.LoginButton
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import kotlin.collections.set


/**
 * Main ViewModel class for the app
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    //Event sent when the section list has been loaded
    private val _sectionsListEvent =
        MutableLiveData<Either<ArcXPException, List<ArcXPSection>>>()
    val sectionsListEvent: LiveData<Either<ArcXPException, List<ArcXPSection>>> =
        _sectionsListEvent

    //Event sent when a section is selected from the view pager
    private val _sectionEvent = MutableLiveData<ArcXPSection?>()
    val sectionEvent: LiveData<ArcXPSection?> = _sectionEvent

    private val _pageEvent = MutableLiveData<Int?>()
    val pageEvent: LiveData<Int?> = _pageEvent

    //Event to trigger navigation to article
    private val _navigateTo = MutableLiveData<NavigationDataItem>()
    val navigateTo: LiveData<NavigationDataItem> = _navigateTo

    val sections = hashMapOf<String, ArcXPSection>()
    val sectionsIndexMap = hashMapOf<Int, String>()
    val indexSectionMap = hashMapOf<String, Int>()

    private val _alertBarState = MutableLiveData<ArcXPContentElement>()
    val alertBarState: LiveData<ArcXPContentElement>  = _alertBarState

    //to be replaced by identity solution
    var pushNotificationsTopicSubscriptions = hashMapOf<String, PushTopicSubscriptionItem>()

    private var sharedPreferences: SharedPreferences =
        application.getSharedPreferences("Preferences", Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            val alertBarResult =
                ArcXPMobileSDK.contentManager().getCollectionSuspend(collectionAlias = "alert-bar")
            if (alertBarResult is Success) {
                val list = alertBarResult.success
                if (list.isNotEmpty()) {
                    list[0]?.let {
                        _alertBarState.postValue(it)
                    }
                }
            }
        }
    }

    /**
     * Fetches the navigation items from site service, and propagates the results via live data
     */
    fun fetchSectionsList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val result = ArcXPMobileSDK.contentManager().getSectionListSuspend(
                    siteHierarchy = getApplication<Application>().resources.getString(R.string.navigation_endpoint) // create a site service hierarchy in advance
                )) {
                    is Success -> {
                        sections.clear()
                        result.success.forEachIndexed { index, section ->
                            val name = section.getNameToUseFromSection()
                            sectionsIndexMap[index] = name
                            indexSectionMap[name] = index
                            sections[name] = section
                        }
                        _sectionsListEvent.postValue(Success(result.success))
                    }

                    is Failure -> {
                        _sectionsListEvent.postValue(Failure(result.failure))
                    }
                }
            }
        }
    }

    fun sectionSelected(section: ArcXPSection?) {
        _sectionEvent.postValue(section)
    }

    fun pageSelected(section: Int?) {
        _pageEvent.postValue(section)
    }


    fun loginWithFacebook(fbButton: LoginButton, owner: LifecycleOwner): LiveData<ArcXPAuth> {
        return ArcXPMobileSDK.commerceManager().loginWithFacebook(fbButton)
    }

    private fun createWebSection(key: String?, name: String?) =
        WebSectionFragment().withUrlAndName(key, name)


    fun getCollection(collectionAlias: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            prefetchDistance = 1 //this controls how many units away when the next page is called for
        ),
        pagingSourceFactory = { CollectionPagingSource(id = collectionAlias, pageSize = 20) }
    ).flow
        .cachedIn(viewModelScope)

    fun getSearchResults(searchTerms: String) = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            prefetchDistance = 1 //TODO this controls how many units away when the next page is called for
        ),
        pagingSourceFactory = {
            SearchResultsPagingSource(
                searchTerms = searchTerms,
                pageSize = 20
            )
        }
    ).flow
        .cachedIn(viewModelScope)

    fun loadPushNotificationSubscribedTopics() {
        //get the current topics from saved preferences
        val json = sharedPreferences.getString("push_topics", "")
        if (json != null) {
            if (json.isNotEmpty()) {
                val gson = Gson()
                val type: Type =
                    object : TypeToken<HashMap<String, PushTopicSubscriptionItem?>?>() {}.type
                pushNotificationsTopicSubscriptions = gson.fromJson(json, type)
            }
        }
        if (pushNotificationsTopicSubscriptions["all"] == null) {
            pushNotificationsTopicSubscriptions["all"] = PushTopicSubscriptionItem("all", false)
        }
    }

    private fun savePushNotificationSubscribedTopics() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json: String = gson.toJson(pushNotificationsTopicSubscriptions)
        editor.putString("push_topics", json)
        editor.apply()
    }

    fun saveSubscribeToAll(subscribe: Boolean) {
        val pushTopicItem = PushTopicSubscriptionItem("all", subscribe)
        pushNotificationsTopicSubscriptions["all"] = pushTopicItem
        savePushNotificationSubscribedTopics()
    }

    fun subscribeToPushNotificationTopic(name: String, topic: String) {
        savePushNotificationSubscribedTopics()
        Firebase.messaging.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //to be replaced by identity solution
                    val pushTopicItem = PushTopicSubscriptionItem(topic, true)
                    pushNotificationsTopicSubscriptions[topic] = pushTopicItem
                    savePushNotificationSubscribedTopics()
                } else {
                    //remove before release
                    Toast.makeText(getApplication(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    fun unsubscribeFromPushNotificationTopic(topic: String) {
        Firebase.messaging.unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //to be replaced by identity solution
                    pushNotificationsTopicSubscriptions.remove(topic)
                    savePushNotificationSubscribedTopics()
                } else {
                    //remove before release
                    Toast.makeText(getApplication(), task.exception?.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    // Call this method to trigger navigation
    fun triggerNavigation(destination: NavigationDataItem) {
        _navigateTo.postValue(destination)
    }
}