package com.arcxp.thearcxp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.widget.ArcXPWidget.Companion.ACTION_ITEM_CLICKED


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [ArcXPWidgetConfigureActivity]
 */
class ArcXPWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val mgr = AppWidgetManager.getInstance(context)
        if (intent?.action.equals(ACTION_ITEM_CLICKED)) {
            val appWidgetId = intent?.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            val widgetArticleId = intent?.getStringExtra(ARTICLE_ID_KEY)
            val widgetItemIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            widgetItemIntent.putExtra(WIDGET_ARTICLE_ID_KEY, widgetArticleId)
            widgetItemIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            context?.startActivity(widgetItemIntent)
        }
        super.onReceive(context, intent)
    }

    companion object {
        const val ACTION_ITEM_CLICKED = "com.arcxp.thearcxp.widget.ArcXPWidget.ACTION_ITEM_CLICKED"
        const val ARTICLE_ID_KEY = "com.arcxp.thearcxp.widget.ArcXPWidget.ARTICLE_ID_KEY"
        const val WIDGET_ARTICLE_ID_KEY = "widgetArticleId"
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val views = RemoteViews(context.packageName, R.layout.arc_x_p_widget)

    val intentWidgetText = Intent(context, ArcXPWidgetService::class.java)

    intentWidgetText.apply{
        // Add the widget ID to the intent extras.
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }

    val widgetItemClickIntent = Intent(context, ArcXPWidget::class.java)
    widgetItemClickIntent.action = ACTION_ITEM_CLICKED
    widgetItemClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

    val widgetClickPendingIntent = PendingIntent.getBroadcast(
        context, 0, widgetItemClickIntent,
        if (Build.VERSION.SDK_INT >= 31) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )

    views.setPendingIntentTemplate(R.id.widgetListView, widgetClickPendingIntent)

    views.setRemoteAdapter(R.id.widgetListView, intentWidgetText)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}