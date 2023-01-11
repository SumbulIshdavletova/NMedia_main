package ru.netology.nmedia.dto

import android.net.Uri
import java.io.File

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val show: Boolean = true,
    val attachment: Attachment? = null,
    val authorId: Long = 0,
    val ownedByMe: Boolean = false,
)

data class PhotoModel(
    val uri: Uri?,
    val file: File?
)

data class Media(val id: String)

data class Attachment(
    val url: String,
    val type: AttachmentType
)

enum class AttachmentType {
    IMAGE
}

