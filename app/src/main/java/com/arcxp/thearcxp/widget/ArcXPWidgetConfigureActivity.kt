package com.arcxp.thearcxp.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.util.Success
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.ArcXPWidgetConfigureBinding
import com.arcxp.thearcxp.utils.TAG
import kotlinx.coroutines.*


/**
 * The configuration screen for the [ArcXPWidget] AppWidget.
 */
class ArcXPWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var configListView: ListView
    private lateinit var binding: ArcXPWidgetConfigureBinding
    private val configSectionListItems = mutableListOf<String>()
    private val configListIds = mutableListOf<String>()

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainContext = CoroutineScope(Dispatchers.Main + SupervisorJob()).coroutineContext

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = ArcXPWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ioScope.launch {
            ArcXPMobileSDK.contentManager().getSectionListSuspend().apply {
                if (this is Success) {
                    success.forEach {
                        configSectionListItems.add(it.navigation.nav_title.toString())
                        configListIds.add(it.id)
                    }
                    withContext(context = mainContext) {
                        finishInit()
                    }
                } else {
                    Log.d(TAG, "Widget Section List Retrieval Failure")
                }
            }
        }
    }

    //after successful request to get section list names, we finish initializing:
    private fun finishInit() {

        configListView = findViewById(R.id.widgetConfigListView)
        binding.widgetConfigListView.isClickable = true

        val widgetConfigAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, configSectionListItems as List<String>
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val text = view.findViewById<View>(android.R.id.text1) as TextView
                text.setTextColor(ContextCompat.getColor(context, R.color.text))
                return view
            }
        }
        binding.widgetConfigListView.adapter = widgetConfigAdapter
        binding.widgetConfigListView.setOnItemClickListener { parent, view, position, id ->
            val context = this@ArcXPWidgetConfigureActivity

            val widgetText = configListIds[position].substring(1)
            saveTitlePref(context, appWidgetId, widgetText)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateAppWidget(context, appWidgetManager, appWidgetId)

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }
}

private const val PREFS_NAME = "com.arcxp.thearcxp.widget.ArcXPWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    return titleValue ?: ""
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}