package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MeraInstaRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mera_insta_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _reels = MutableStateFlow<List<Reel>>(emptyList())
    val reels: StateFlow<List<Reel>> = _reels.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _permissions = MutableStateFlow<List<AppPermission>>(emptyList())
    val permissions: StateFlow<List<AppPermission>> = _permissions.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _isDarkMode.value = prefs.getBoolean("is_dark_mode", false)
        _isOnline.value = prefs.getBoolean("is_online", true)

        // Load or create default users
        val savedUsers = loadUsersFromFile()
        if (savedUsers.isNotEmpty()) {
            _users.value = savedUsers
        } else {
            _users.value = defaultUsers()
            saveUsersToFile(_users.value)
        }

        // Load current user
        val userJson = prefs.getString("current_user_json", null)
        if (userJson != null) {
            try {
                val obj = JSONObject(userJson)
                _currentUser.value = User(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    username = obj.getString("username"),
                    bio = obj.optString("bio", "Namaste! Main Mera Insta use kar raha hoon ✨"),
                    avatarColorHex = obj.optLong("avatarColorHex", 0xFF833AB4),
                    followersCount = obj.optInt("followersCount", 125),
                    followingCount = obj.optInt("followingCount", 84),
                    isFollowedByMe = false
                )
            } catch (e: Exception) {
                initDefaultCurrentUser()
            }
        } else {
            initDefaultCurrentUser()
        }

        // Load posts
        val savedPosts = loadPostsFromFile()
        if (savedPosts.isNotEmpty()) {
            _posts.value = savedPosts
        } else {
            _posts.value = defaultPosts()
            savePostsToFile(_posts.value)
        }

        // Load stories
        _stories.value = defaultStories()

        // Load reels
        val savedReels = loadReelsFromFile()
        if (savedReels.isNotEmpty()) {
            _reels.value = savedReels
        } else {
            _reels.value = defaultReels()
            saveReelsToFile(_reels.value)
        }

        // Load notifications
        val savedNotifications = loadNotificationsFromFile()
        if (savedNotifications.isNotEmpty()) {
            _notifications.value = savedNotifications
        } else {
            _notifications.value = defaultNotifications()
            saveNotificationsToFile(_notifications.value)
        }

        // Load chats
        _chatMessages.value = defaultChats()

        // Initialize permissions
        _permissions.value = defaultPermissions()
    }

    private fun initDefaultCurrentUser() {
        val defaultMe = User(
            id = "user_me",
            name = "Aapka Naam",
            username = "merainsta_user",
            bio = "Digital Creator | Photophile 📸 | Namaste Dosto! ✨",
            avatarColorHex = 0xFF833AB4,
            followersCount = 142,
            followingCount = 68
        )
        _currentUser.value = defaultMe
        saveCurrentUser(defaultMe)
    }

    private fun saveCurrentUser(user: User) {
        val obj = JSONObject().apply {
            put("id", user.id)
            put("name", user.name)
            put("username", user.username)
            put("bio", user.bio)
            put("avatarColorHex", user.avatarColorHex)
            put("followersCount", user.followersCount)
            put("followingCount", user.followingCount)
        }
        prefs.edit().putString("current_user_json", obj.toString()).apply()
    }

    fun login(usernameInput: String): Boolean {
        val clean = usernameInput.trim().removePrefix("@")
        val user = User(
            id = "user_${clean.lowercase()}",
            name = clean.replaceFirstChar { it.uppercase() },
            username = clean.lowercase(),
            bio = "Mera Insta pe naya safar! 🚀",
            avatarColorHex = 0xFFFCB045,
            followersCount = 50,
            followingCount = 20
        )
        _currentUser.value = user
        saveCurrentUser(user)
        return true
    }

    fun signup(name: String, username: String, bio: String, avatarColorHex: Long) {
        val clean = username.trim().removePrefix("@")
        val newUser = User(
            id = "user_${clean.lowercase()}",
            name = name.trim().ifEmpty { clean },
            username = clean.lowercase(),
            bio = bio.trim().ifEmpty { "Namaste! Main Mera Insta me naya hoon ✨" },
            avatarColorHex = avatarColorHex,
            followersCount = 1,
            followingCount = 4
        )
        _currentUser.value = newUser
        saveCurrentUser(newUser)

        // Add welcome notification
        addNotification(
            NotificationItem(
                id = "notif_welcome_${System.currentTimeMillis()}",
                type = "follow",
                actorName = "Rahul Verma",
                message = "ne aapko follow kiya. Mera Insta me aapka swagat hai!",
                timestamp = "Abhi"
            )
        )
    }

    fun updateProfile(name: String, username: String, bio: String, avatarColorHex: Long) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            name = name,
            username = username.removePrefix("@"),
            bio = bio,
            avatarColorHex = avatarColorHex
        )
        _currentUser.value = updated
        saveCurrentUser(updated)
    }

    fun toggleDarkMode() {
        val newVal = !_isDarkMode.value
        _isDarkMode.value = newVal
        prefs.edit().putBoolean("is_dark_mode", newVal).apply()
    }

    fun toggleOnlineStatus() {
        val newVal = !_isOnline.value
        _isOnline.value = newVal
        prefs.edit().putBoolean("is_online", newVal).apply()
    }

    fun createPost(imageUrl: String, filter: MediaFilter, caption: String, location: String, taggedUsers: List<String>, localUri: String? = null) {
        val me = _currentUser.value ?: return
        val newPost = Post(
            id = "post_${System.currentTimeMillis()}",
            userId = me.id,
            username = me.username,
            imageUrl = imageUrl,
            filter = filter,
            caption = caption,
            location = location,
            taggedUsers = taggedUsers,
            likesCount = 1,
            isLiked = true,
            isSaved = false,
            timestamp = "Abhi",
            localUri = localUri
        )
        val updated = listOf(newPost) + _posts.value
        _posts.value = updated
        savePostsToFile(updated)

        // Notification simulation from follower
        addNotification(
            NotificationItem(
                id = "notif_like_${System.currentTimeMillis()}",
                type = "like",
                actorName = "Priya Sharma",
                message = "ne aapke naye post ko like kiya ❤️",
                timestamp = "Abhi",
                targetPostId = newPost.id
            )
        )
    }

    fun createReel(caption: String, musicName: String, durationSec: Int, filter: MediaFilter, gradientColors: List<Long>) {
        val me = _currentUser.value ?: return
        val newReel = Reel(
            id = "reel_${System.currentTimeMillis()}",
            userId = me.id,
            username = me.username,
            caption = caption,
            musicName = musicName,
            durationSec = durationSec,
            likesCount = 5,
            commentsCount = 1,
            isLiked = true,
            filter = filter,
            gradientColors = gradientColors
        )
        val updated = listOf(newReel) + _reels.value
        _reels.value = updated
        saveReelsToFile(updated)
    }

    fun createStory(caption: String, mediaUrl: String) {
        val me = _currentUser.value ?: return
        val newStory = Story(
            id = "story_${System.currentTimeMillis()}",
            userId = me.id,
            username = "Meri Story",
            mediaUrl = mediaUrl,
            caption = caption,
            timestamp = "Abhi"
        )
        val filtered = _stories.value.filter { it.userId != me.id }
        _stories.value = listOf(newStory) + filtered
    }

    fun toggleLikePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLiked
                val newCount = if (newLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
                post.copy(isLiked = newLiked, likesCount = newCount)
            } else post
        }
        savePostsToFile(_posts.value)
    }

    fun toggleSavePost(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(isSaved = !post.isSaved)
            } else post
        }
        savePostsToFile(_posts.value)
    }

    fun addComment(postId: String, commentText: String) {
        val me = _currentUser.value ?: return
        val comment = Comment(
            id = "c_${System.currentTimeMillis()}",
            username = me.username,
            text = commentText,
            timestamp = "Abhi"
        )
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(comments = post.comments + comment)
            } else post
        }
        savePostsToFile(_posts.value)
    }

    fun toggleLikeReel(reelId: String) {
        _reels.value = _reels.value.map { reel ->
            if (reel.id == reelId) {
                val newLiked = !reel.isLiked
                val newCount = if (newLiked) reel.likesCount + 1 else maxOf(0, reel.likesCount - 1)
                reel.copy(isLiked = newLiked, likesCount = newCount)
            } else reel
        }
        saveReelsToFile(_reels.value)
    }

    fun toggleFollowUser(userId: String) {
        val me = _currentUser.value ?: return
        var newFollowState = false
        var targetUserName = ""

        _users.value = _users.value.map { user ->
            if (user.id == userId) {
                val isNowFollowed = !user.isFollowedByMe
                newFollowState = isNowFollowed
                targetUserName = user.name
                val newFollowers = if (isNowFollowed) user.followersCount + 1 else maxOf(0, user.followersCount - 1)
                user.copy(isFollowedByMe = isNowFollowed, followersCount = newFollowers)
            } else user
        }
        saveUsersToFile(_users.value)

        // Update following count of current user
        val updatedMe = me.copy(
            followingCount = if (newFollowState) me.followingCount + 1 else maxOf(0, me.followingCount - 1)
        )
        _currentUser.value = updatedMe
        saveCurrentUser(updatedMe)

        // If newly followed, trigger notification
        if (newFollowState) {
            addNotification(
                NotificationItem(
                    id = "notif_follow_${System.currentTimeMillis()}",
                    type = "follow",
                    actorName = targetUserName,
                    message = "ne bhi aapko wapas follow kiya! 🎉",
                    timestamp = "Abhi"
                )
            )
        }
    }

    fun toggleBlockUser(userId: String) {
        _users.value = _users.value.map { user ->
            if (user.id == userId) {
                user.copy(isBlocked = !user.isBlocked)
            } else user
        }
        saveUsersToFile(_users.value)
    }

    fun sendMessage(receiverId: String, text: String, isVoice: Boolean = false, voiceDuration: Int = 0) {
        val me = _currentUser.value ?: return
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = me.id,
            receiverId = receiverId,
            text = text,
            timestamp = "Abhi",
            isVoice = isVoice,
            voiceDurationSec = voiceDuration,
            isSeen = false,
            isDelivered = true
        )
        val currentChat = _chatMessages.value[receiverId] ?: emptyList()
        val updatedMap = _chatMessages.value.toMutableMap()
        updatedMap[receiverId] = currentChat + newMsg
        _chatMessages.value = updatedMap
    }

    fun markChatSeen(userId: String) {
        val current = _chatMessages.value[userId] ?: return
        val updated = current.map { it.copy(isSeen = true) }
        val updatedMap = _chatMessages.value.toMutableMap()
        updatedMap[userId] = updated
        _chatMessages.value = updatedMap
    }

    fun addNotification(item: NotificationItem) {
        val updated = listOf(item) + _notifications.value
        _notifications.value = updated
        saveNotificationsToFile(updated)
    }

    fun setPermissionGranted(permissionId: String, granted: Boolean) {
        _permissions.value = _permissions.value.map {
            if (it.id == permissionId) it.copy(isGranted = granted) else it
        }
    }

    fun refreshFeed() {
        // Shuffle or refresh timestamps
        _posts.value = _posts.value.map { it.copy() }
    }

    // File Persistence Helpers
    private fun savePostsToFile(posts: List<Post>) {
        try {
            val array = JSONArray()
            posts.forEach { post ->
                val obj = JSONObject().apply {
                    put("id", post.id)
                    put("userId", post.userId)
                    put("username", post.username)
                    put("imageUrl", post.imageUrl)
                    put("filter", post.filter.name)
                    put("caption", post.caption)
                    put("location", post.location)
                    put("likesCount", post.likesCount)
                    put("isLiked", post.isLiked)
                    put("isSaved", post.isSaved)
                    put("timestamp", post.timestamp)
                    put("localUri", post.localUri ?: "")
                }
                array.put(obj)
            }
            File(context.filesDir, "posts.json").writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPostsFromFile(): List<Post> {
        val file = File(context.filesDir, "posts.json")
        if (!file.exists()) return emptyList()
        return try {
            val content = file.readText()
            val array = JSONArray(content)
            val list = mutableListOf<Post>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Post(
                        id = obj.getString("id"),
                        userId = obj.getString("userId"),
                        username = obj.getString("username"),
                        imageUrl = obj.optString("imageUrl", ""),
                        filter = try {
                            MediaFilter.valueOf(obj.optString("filter", "NORMAL"))
                        } catch (e: Exception) {
                            MediaFilter.NORMAL
                        },
                        caption = obj.optString("caption", ""),
                        location = obj.optString("location", ""),
                        likesCount = obj.optInt("likesCount", 0),
                        isLiked = obj.optBoolean("isLiked", false),
                        isSaved = obj.optBoolean("isSaved", false),
                        timestamp = obj.optString("timestamp", "Abhi"),
                        localUri = obj.optString("localUri").takeIf { it.isNotEmpty() }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveUsersToFile(users: List<User>) {
        try {
            val array = JSONArray()
            users.forEach { u ->
                val obj = JSONObject().apply {
                    put("id", u.id)
                    put("name", u.name)
                    put("username", u.username)
                    put("bio", u.bio)
                    put("avatarColorHex", u.avatarColorHex)
                    put("followersCount", u.followersCount)
                    put("followingCount", u.followingCount)
                    put("isFollowedByMe", u.isFollowedByMe)
                    put("isBlocked", u.isBlocked)
                    put("isOnline", u.isOnline)
                    put("lastSeen", u.lastSeen)
                }
                array.put(obj)
            }
            File(context.filesDir, "users.json").writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadUsersFromFile(): List<User> {
        val file = File(context.filesDir, "users.json")
        if (!file.exists()) return emptyList()
        return try {
            val content = file.readText()
            val array = JSONArray(content)
            val list = mutableListOf<User>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    User(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        username = obj.getString("username"),
                        bio = obj.optString("bio", ""),
                        avatarColorHex = obj.optLong("avatarColorHex", 0xFF833AB4),
                        followersCount = obj.optInt("followersCount", 0),
                        followingCount = obj.optInt("followingCount", 0),
                        isFollowedByMe = obj.optBoolean("isFollowedByMe", false),
                        isBlocked = obj.optBoolean("isBlocked", false),
                        isOnline = obj.optBoolean("isOnline", true),
                        lastSeen = obj.optString("lastSeen", "Abhi active")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveReelsToFile(reels: List<Reel>) {
        try {
            val array = JSONArray()
            reels.forEach { r ->
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("userId", r.userId)
                    put("username", r.username)
                    put("caption", r.caption)
                    put("musicName", r.musicName)
                    put("durationSec", r.durationSec)
                    put("likesCount", r.likesCount)
                    put("commentsCount", r.commentsCount)
                    put("isLiked", r.isLiked)
                }
                array.put(obj)
            }
            File(context.filesDir, "reels.json").writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadReelsFromFile(): List<Reel> {
        val file = File(context.filesDir, "reels.json")
        if (!file.exists()) return emptyList()
        return try {
            val content = file.readText()
            val array = JSONArray(content)
            val list = mutableListOf<Reel>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Reel(
                        id = obj.getString("id"),
                        userId = obj.getString("userId"),
                        username = obj.getString("username"),
                        caption = obj.optString("caption", ""),
                        musicName = obj.optString("musicName", "Trending Beat"),
                        durationSec = obj.optInt("durationSec", 30),
                        likesCount = obj.optInt("likesCount", 120),
                        commentsCount = obj.optInt("commentsCount", 14),
                        isLiked = obj.optBoolean("isLiked", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveNotificationsToFile(notifs: List<NotificationItem>) {
        try {
            val array = JSONArray()
            notifs.forEach { n ->
                val obj = JSONObject().apply {
                    put("id", n.id)
                    put("type", n.type)
                    put("actorName", n.actorName)
                    put("message", n.message)
                    put("timestamp", n.timestamp)
                }
                array.put(obj)
            }
            File(context.filesDir, "notifications.json").writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadNotificationsFromFile(): List<NotificationItem> {
        val file = File(context.filesDir, "notifications.json")
        if (!file.exists()) return emptyList()
        return try {
            val content = file.readText()
            val array = JSONArray(content)
            val list = mutableListOf<NotificationItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NotificationItem(
                        id = obj.getString("id"),
                        type = obj.getString("type"),
                        actorName = obj.getString("actorName"),
                        message = obj.getString("message"),
                        timestamp = obj.optString("timestamp", "Abhi")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Default Seed Data
    private fun defaultUsers(): List<User> = listOf(
        User(
            id = "u_rahul",
            name = "Rahul Verma",
            username = "rahul_verma",
            bio = "Photography | Delhi Vibes 🇮🇳 | Explore the world ✈️",
            avatarColorHex = 0xFF3897F0,
            followersCount = 1420,
            followingCount = 412,
            isFollowedByMe = true,
            isOnline = true,
            lastSeen = "Online"
        ),
        User(
            id = "u_priya",
            name = "Priya Sharma",
            username = "priya_sharma",
            bio = "Fashion Designer 👗 | Coffee & Sunshine ☕️☀️ | Mumbai",
            avatarColorHex = 0xFFFD1D1D,
            followersCount = 2890,
            followingCount = 350,
            isFollowedByMe = true,
            isOnline = true,
            lastSeen = "Online"
        ),
        User(
            id = "u_amit",
            name = "Amit Kumar",
            username = "amit_kumar",
            bio = "Tech Explorer 💻 | Reels Creator 🎬 | Bangalore",
            avatarColorHex = 0xFFFCB045,
            followersCount = 980,
            followingCount = 210,
            isFollowedByMe = false,
            isOnline = false,
            lastSeen = "10 min pehle"
        ),
        User(
            id = "u_sneha",
            name = "Sneha Kapoor",
            username = "sneha_kapoor",
            bio = "Foodie 🍕 | Travel Diary 🏔 | Positive Vibes Only ✨",
            avatarColorHex = 0xFF833AB4,
            followersCount = 3400,
            followingCount = 520,
            isFollowedByMe = true,
            isOnline = true,
            lastSeen = "Online"
        ),
        User(
            id = "u_rohan",
            name = "Rohan Singh",
            username = "rohan_singh",
            bio = "Fitness & Nutrition 🏋️‍♂️ | Chandigarh",
            avatarColorHex = 0xFF10B981,
            followersCount = 760,
            followingCount = 190,
            isFollowedByMe = false,
            isOnline = false,
            lastSeen = "Kal 8:30 PM"
        ),
        User(
            id = "u_pooja",
            name = "Pooja Patel",
            username = "pooja_patel",
            bio = "Artist 🎨 | Digital Sketches | Ahmedabad",
            avatarColorHex = 0xFFEC4899,
            followersCount = 1850,
            followingCount = 310,
            isFollowedByMe = false,
            isOnline = true,
            lastSeen = "Online"
        )
    )

    private fun defaultPosts(): List<Post> = listOf(
        Post(
            id = "p1",
            userId = "u_rahul",
            username = "rahul_verma",
            imageUrl = "sample_sunset",
            filter = MediaFilter.WARM,
            caption = "Delhi ka khubsurat shaam! Kuch pal sukoon ke 🌅 #Sunset #Delhi #MeraInsta #Peace",
            location = "India Gate, New Delhi",
            likesCount = 342,
            isLiked = false,
            isSaved = false,
            timestamp = "15 min pehle",
            comments = listOf(
                Comment("c1", "priya_sharma", "", "Bahut shandar click! 🔥", "10 min pehle"),
                Comment("c2", "sneha_kapoor", "", "Golden hour magic ✨", "5 min pehle")
            )
        ),
        Post(
            id = "p2",
            userId = "u_priya",
            username = "priya_sharma",
            imageUrl = "sample_fashion",
            filter = MediaFilter.COOL,
            caption = "Naya festive collection launch hua! Konsa color sabse best laga? 💃✨ Comment karke batao! #Fashion #Vibes",
            location = "Bandra, Mumbai",
            likesCount = 890,
            isLiked = true,
            isSaved = true,
            timestamp = "1 ghanta pehle",
            comments = listOf(
                Comment("c3", "pooja_patel", "", "Royal blue wala top-notch hai! 💙", "45 min pehle")
            )
        ),
        Post(
            id = "p3",
            userId = "u_sneha",
            username = "sneha_kapoor",
            imageUrl = "sample_cafe",
            filter = MediaFilter.HDR,
            caption = "Sunday brunch with cold coffee and pancakes 🥞☕️ weekend mood on! #Foodie #BrunchLove",
            location = "Goa Beach Cafe",
            likesCount = 512,
            isLiked = false,
            isSaved = false,
            timestamp = "3 ghante pehle",
            comments = emptyList()
        )
    )

    private fun defaultStories(): List<Story> = listOf(
        Story(
            id = "s_me",
            userId = "user_me",
            username = "Meri Story",
            caption = "Mera Insta pe live! 🎉 Tap karke naya story dalo",
            timestamp = "Abhi"
        ),
        Story(
            id = "s1",
            userId = "u_rahul",
            username = "rahul_verma",
            caption = "Shooting new landscape reel! 📸✨",
            timestamp = "2 ghante pehle"
        ),
        Story(
            id = "s2",
            userId = "u_priya",
            username = "priya_sharma",
            caption = "Studio diaries 🌸👗",
            timestamp = "4 ghante pehle"
        ),
        Story(
            id = "s3",
            userId = "u_sneha",
            username = "sneha_kapoor",
            caption = "Beach breeze in Goa 🏖️🌊",
            timestamp = "6 ghante pehle"
        ),
        Story(
            id = "s4",
            userId = "u_amit",
            username = "amit_kumar",
            caption = "Coding with chai ☕️💻",
            timestamp = "8 ghante pehle"
        )
    )

    private fun defaultReels(): List<Reel> = listOf(
        Reel(
            id = "r1",
            userId = "u_amit",
            username = "amit_kumar",
            caption = "Top 5 AI tools jo aapka kaam 10x fast karenge! ⚡️ #Tech #AI #ReelsIndia #Productivity",
            musicName = "Trending Synth Beats - Amit Audio",
            durationSec = 30,
            likesCount = 1420,
            commentsCount = 84,
            isLiked = false,
            gradientColors = listOf(0xFF833AB4, 0xFFFD1D1D, 0xFF3897F0)
        ),
        Reel(
            id = "r2",
            userId = "u_priya",
            username = "priya_sharma",
            caption = "Quick 30 second wardrobe transition! Konsa look pasand aaya? 💃✨ #FashionReel #StyleInspo",
            musicName = "Desi Pop Beats - Priya Mix",
            durationSec = 15,
            likesCount = 3840,
            commentsCount = 210,
            isLiked = true,
            gradientColors = listOf(0xFFFD1D1D, 0xFFFCB045, 0xFFFFDC80)
        ),
        Reel(
            id = "r3",
            userId = "u_rahul",
            username = "rahul_verma",
            caption = "Mountains calling! Manali scenic highway drive in 4K 🏔️❄️ #Travel #Nature #Wanderlust",
            musicName = "Acoustic Sunset Melody",
            durationSec = 60,
            likesCount = 2710,
            commentsCount = 143,
            isLiked = false,
            gradientColors = listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364)
        )
    )

    private fun defaultNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "n1",
            type = "follow",
            actorName = "Rahul Verma",
            message = "ne tumhe Follow kiya",
            timestamp = "10 min pehle"
        ),
        NotificationItem(
            id = "n2",
            type = "like",
            actorName = "Rahul Verma",
            message = "ne aapki photo Like ki ❤️",
            timestamp = "25 min pehle"
        ),
        NotificationItem(
            id = "n3",
            type = "follow",
            actorName = "Priya Sharma",
            message = "ne tumhe Follow kiya",
            timestamp = "1 ghanta pehle"
        ),
        NotificationItem(
            id = "n4",
            type = "comment",
            actorName = "Sneha Kapoor",
            message = "ne aapke post pe comment kiya: 'Bahut khoob! 🔥'",
            timestamp = "3 ghante pehle"
        )
    )

    private fun defaultChats(): Map<String, List<ChatMessage>> = mapOf(
        "u_rahul" to listOf(
            ChatMessage("m1", "u_rahul", "user_me", "Bhai kaisa hai? Naya post dekha maine!", "11:30 AM", isSeen = true),
            ChatMessage("m2", "user_me", "u_rahul", "Haan Rahul bhai! Kaisa laga?", "11:32 AM", isSeen = true),
            ChatMessage("m3", "u_rahul", "user_me", "Ekdam zabardast photography! Agla shoot kab hai?", "11:35 AM", isSeen = true)
        ),
        "u_priya" to listOf(
            ChatMessage("m4", "u_priya", "user_me", "Hey! Thanks for following 😊", "Yesterday", isSeen = true),
            ChatMessage("m5", "user_me", "u_priya", "Aapka collection bahut badhiya hai!", "Yesterday", isSeen = true)
        ),
        "u_sneha" to listOf(
            ChatMessage("m6", "u_sneha", "user_me", "Goa aao kabhi, beach cafe visit karenge 🏖️", "2 days ago", isSeen = true)
        )
    )

    private fun defaultPermissions(): List<AppPermission> = listOf(
        AppPermission("perm_camera", "Camera Permission", "Photo aur Video banane ke liye", "android.permission.CAMERA", false, "camera"),
        AppPermission("perm_mic", "Microphone Permission", "Reels me awaaz aur Voice Message ke liye", "android.permission.RECORD_AUDIO", false, "mic"),
        AppPermission("perm_storage", "Storage / Photos & Videos", "Photo Video save aur upload karne ke liye", "android.permission.READ_MEDIA_IMAGES", false, "image"),
        AppPermission("perm_notif", "Notifications Permission", "Like, Comment, Follow, Message ka batane ke liye", "android.permission.POST_NOTIFICATIONS", true, "notification"),
        AppPermission("perm_location", "Location Permission", "Post pe Location lagane aur aas-paas ke dost dhoondhne ke liye", "android.permission.ACCESS_FINE_LOCATION", false, "location"),
        AppPermission("perm_contacts", "Contacts Permission", "Apne Contacts se dost dhoondhne aur Follow karne ke liye", "android.permission.READ_CONTACTS", false, "contacts"),
        AppPermission("perm_accessibility", "Accessibility Permission", "App ko aasani se chalane aur dusre app se Post Share karne ke liye", null, false, "accessibility"),
        AppPermission("perm_overlay", "Display over other apps", "Kisi bhi app ke upar Notification aur Chat Bubble dikhane ke liye", "android.permission.SYSTEM_ALERT_WINDOW", false, "layers"),
        AppPermission("perm_internet", "Internet / Online Permission", "Online Post, Follow, Chat ke liye", "android.permission.INTERNET", true, "wifi"),
        AppPermission("perm_phone", "Phone State Permission", "Call aane pe App ko rokne ke liye", "android.permission.READ_PHONE_STATE", false, "phone")
    )
}
