package com.arcxp.thearcxp

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.models.ArcXPCollection
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.content.sdk.util.Either
import com.arcxp.content.sdk.util.Success
import com.arcxp.thearcxp.ArcXPWidget.Companion.ARTICLE_ID_KEY
import com.arcxp.thearcxp.utils.TAG
import com.arcxp.thearcxp.utils.imageUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class ArcXPWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetItemFactory(this.applicationContext, intent)
    }
    class WidgetItemFactory(
        val context: Context,
        val intent: Intent
    ) : RemoteViewsFactory {

        private val headlinesBasics = mutableListOf<String>()
        private var imgUrl = mutableListOf<String>()
        private val widgetArticleId = mutableListOf<String>()
        private var sectionId = ""

        override fun onCreate() {
            getWidgetContent()
        }

        override fun onDataSetChanged() {
        }

        override fun onDestroy() {
        }

        private fun getWidgetContent(){
            val appWidgetId = intent.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

            sectionId = loadTitlePref(context, appWidgetId)

            val coroutineContext = CoroutineScope(Dispatchers.IO + SupervisorJob())
            coroutineContext.launch {
                val result: Either<ArcXPContentError, Map<Int, ArcXPCollection>> =
                    ArcXPContentSDK.contentManager().getCollectionSuspend(sectionId)
                if (result is Success) {
                    result.success.forEach { entry ->
                        entry.value.headlines.basic?.let { headlinesBasics.add(it) }
                        entry.value.id.let { widgetArticleId.add(it) }

                        if (entry.value.imageUrl().isNotEmpty()) {
                            imgUrl.add(entry.value.imageUrl())
                        }
                    }
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetListView)
                } else {
                    Log.d(TAG, "WidgetItemFactory Failure")
                }
            }
        }

        override fun getCount(): Int {
            return headlinesBasics.size
        }

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widgetrow)
            if(position < headlinesBasics.size && headlinesBasics[position].isNotEmpty()) {
                views.setTextViewText(R.id.tvHeadline, headlinesBasics[position])
            }

            if(position < imgUrl.size && imgUrl[position].isNotEmpty()) {
                var image: Bitmap? = null
                try {
                    val `in` = java.net.URL(imgUrl[position]).openStream()
                    image = BitmapFactory.decodeStream(`in`)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                views.setImageViewBitmap(R.id.imgArticle, image)
            }

            val appWidgetId = intent.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

            val fillInIntent = Intent(context, ArcXPWidget::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            fillInIntent.putExtra(ARTICLE_ID_KEY, widgetArticleId[position])
            views.setOnClickFillInIntent(R.id.widget_item, fillInIntent)

            return views
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }
    }
}