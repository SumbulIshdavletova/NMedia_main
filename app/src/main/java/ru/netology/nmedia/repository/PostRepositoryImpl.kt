package ru.netology.nmedia.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import java.sql.Timestamp
import java.time.*
import java.time.temporal.ChronoField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor
    (
    private val postDao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) : PostRepository {

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        val current = OffsetDateTime.now().toEpochSecond()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 2),
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb
        ),
        pagingSourceFactory = postDao::getPagingSource,
    ).flow.map {

        it.map(PostEntity::toDto).insertSeparators { previous: Post?, next: Post? ->

            var difference1 = 0
            var difference2 = 0
            if (previous?.published != null && next?.published != null) {
                difference1 = current.minus(previous.published).toInt()
                difference2 = current.minus(next.published).toInt()
            }
            if (next != null && difference1 < 86400 && difference2 < 86400) {
                TimingSeparator(kotlin.random.Random.nextLong(), "Today")
            } else {
                null
            }
        }
//            if (difference1 in 86400..172800 && difference2 in 86400..172800) {
//                next?.let { it1 -> TimingSeparator(it1.id, "Yesterday") }
//            } else {
//                next?.let { it1 -> TimingSeparator(it1.id, "Last week") }
//            }


            .insertSeparators { previous, _ ->
                if (previous?.id?.rem(5) == 0L) {
                    Ad(kotlin.random.Random.nextLong(), "figma.jpg")
                } else {
                    null
                }
            }
    }


    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity(true))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity(false))
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)


    override suspend fun update() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            if (postDao.count() > 0) {
                postDao.update()
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            postDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun likeById(id: Long) {
        try {
            val response = apiService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            postDao.likeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun unlikeById(id: Long) {
        try {
            val response = apiService.dislikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            postDao.likeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun saveWithAttachment(post: Post, file: File) {
        try {
            val upload = upload(file)
            val postWithAttachment =
                post.copy(show = true, attachment = Attachment(upload.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(file: File): Media {
        try {
            val data = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody()
            )
            val response = apiService.upload(data)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}
