package com.arcxp.thearcxp.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.content.extendedModels.ArcXPCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchResultsPagingSource(val searchTerms: String, val pageSize: Int) : PagingSource<Int, ArcXPCollection>() {

    override fun getRefreshKey(state: PagingState<Int, ArcXPCollection>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(pageSize) ?: anchorPage?.nextKey?.minus(pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArcXPCollection> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val page = params.key ?: 0
                when (val response =
                    ArcXPMobileSDK.contentManager().searchCollectionSuspend(searchTerm = searchTerms, from = page)) {
                    is Success -> {
                        val list = response.success.toSortedMap().values.toList()
                        LoadResult.Page(
                            data = list,
                            prevKey = if (page == 0) null else page - pageSize,
                            nextKey = if (list.isEmpty()) null else page + pageSize
                        )
                    }

                    is Failure -> {
                        LoadResult.Error(throwable = response.failure)
                    }
                }
            } catch (e: Exception) {
                LoadResult.Error(throwable = e)
            }
        }
}