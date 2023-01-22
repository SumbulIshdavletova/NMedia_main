package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError
import java.io.IOException

class PostPagingSource(
    private val apiService: ApiService
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {
            val result = when (params) {
                is LoadParams.Refresh -> apiService.getLatest(params.loadSize)
                is LoadParams.Prepend -> return LoadResult.Page(
                    data = emptyList(),
                    prevKey = params.key,
                    nextKey = null
                )
                is LoadParams.Append -> apiService.getBefore(id = params.key, count = params.loadSize)
            }

            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }
            val data = result.body() ?: throw ApiError(
                result.code(),
                result.message(),
            )

            val nextKey = if (data.isEmpty()) null else data.last().id
            return LoadResult.Page(
                data = data,
                prevKey = params.key,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }

//            if (!result.isSuccessful) {
//                throw HttpException(result)
//            }
//            val data = result.body() ?: ApiError(result.code(),result.message(),)
//
//            val nextKey = if (body.isEmpty()) null else body.last().id
//
//        //    val data = result.body().orEmpty()
//            return LoadResult.Page(
//                data = data,
//                prevKey = params.key,
//                nextKey = data.lastOrNull()?.id
//            )
//        } catch (e: IOException) {
//            return LoadResult.Error(e)
//        }
    }

}