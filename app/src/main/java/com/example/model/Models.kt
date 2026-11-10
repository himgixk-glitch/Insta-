package com.example.model

data class User(
    val id: String,
    val name: String,
    val username: String,
    val bio: String = "",
    val avatarUrl: String = "",
    val avatarColorHex: Long = 0xFF833AB4,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowedByMe: Boolean = false,
    val isBlocked: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeen: String = "Abhi active"
)

data class Comment(
    val id: String,
    val username: String,
    val userAvatar: String = "",
    val text: String,
    val timestamp: String = "Abhi"
)

enum class MediaFilter(val displayName: String, val description: String) {
    NORMAL("Normal", "Original look"),
    WARM("Warm", "Golden sunny tones"),
    COOL("Cool", "Ocean breeze tones"),
    BW("Black & White", "Classic monochrome"),
    HDR("HDR", "High contrast and vibrant"),
    BLUR("Blur Background", "Portrait depth focus")
}

data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val userAvatar: String = "",
    val imageUrl: String = "",
    val filter: MediaFilter = MediaFilter.NORMAL,
    val caption: String = "",
    val location: String = "",
    val taggedUsers: List<String> = emptyList(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val timestamp: String = "Abhi",
    val localUri: String? = null
)

data class Story(
    val id: String,
    val userId: String,
    val username: String,
    val userAvatar: String = "",
    val mediaUrl: String = "",
    val caption: String = "",
    val timestamp: String = "2 ghante pehle",
    val isViewed: Boolean = false
)

data class Reel(
    val id: String,
    val userId: String,
    val username: String,
    val userAvatar: String = "",
    val caption: String = "",
    val musicName: String = "Original Audio - Trending Beat",
    val durationSec: Int = 30,
    val likesCount: Int = 120,
    val commentsCount: Int = 14,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val filter: MediaFilter = MediaFilter.NORMAL,
    val gradientColors: List<Long> = listOf(0xFF833AB4, 0xFFFD1D1D, 0xFFFCB045)
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: String,
    val isVoice: Boolean = false,
    val voiceDurationSec: Int = 0,
    val isSeen: Boolean = true,
    val isDelivered: Boolean = true
)

data class NotificationItem(
    val id: String,
    val type: String, // "follow", "like", "comment", "message"
    val actorName: String,
    val actorAvatar: String = "",
    val message: String,
    val timestamp: String = "Abhi",
    val targetPostId: String? = null,
    val isRead: Boolean = false
)

data class AppPermission(
    val id: String,
    val name: String,
    val reason: String,
    val manifestPermission: String?,
    val isGranted: Boolean = false,
    val iconName: String = "lock"
)
