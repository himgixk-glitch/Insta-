package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MeraInstaRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeraInstaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MeraInstaRepository(application)

    val currentUser = repository.currentUser
    val isOnline = repository.isOnline
    val isDarkMode = repository.isDarkMode
    val posts = repository.posts
    val stories = repository.stories
    val reels = repository.reels
    val users = repository.users
    val chatMessages = repository.chatMessages
    val notifications = repository.notifications
    val permissions = repository.permissions

    // Navigation and Modal State
    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Search, 2: Create, 3: Reels, 4: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _activeChatUser = MutableStateFlow<User?>(null)
    val activeChatUser: StateFlow<User?> = _activeChatUser.asStateFlow()

    private val _showChatList = MutableStateFlow(false)
    val showChatList: StateFlow<Boolean> = _showChatList.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _activeStory = MutableStateFlow<Story?>(null)
    val activeStory: StateFlow<Story?> = _activeStory.asStateFlow()

    private val _viewingUserProfile = MutableStateFlow<User?>(null)
    val viewingUserProfile: StateFlow<User?> = _viewingUserProfile.asStateFlow()

    private val _viewingPostDetail = MutableStateFlow<Post?>(null)
    val viewingPostDetail: StateFlow<Post?> = _viewingPostDetail.asStateFlow()

    private val _activeCommentsPost = MutableStateFlow<Post?>(null)
    val activeCommentsPost: StateFlow<Post?> = _activeCommentsPost.asStateFlow()

    // Step-by-step permissions dialog state (Requirement 8)
    private val _showPermissionGuide = MutableStateFlow(false)
    val showPermissionGuide: StateFlow<Boolean> = _showPermissionGuide.asStateFlow()

    private val _permissionStepIndex = MutableStateFlow(0)
    val permissionStepIndex: StateFlow<Int> = _permissionStepIndex.asStateFlow()

    // Follow/Following list dialog
    private val _followersDialogData = MutableStateFlow<Pair<String, List<User>>?>(null) // Title to user list
    val followersDialogData: StateFlow<Pair<String, List<User>>?> = _followersDialogData.asStateFlow()

    // Welcome banner state
    private val _showWelcomeBanner = MutableStateFlow(true)
    val showWelcomeBanner: StateFlow<Boolean> = _showWelcomeBanner.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Status message / toast
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
        _viewingUserProfile.value = null
        _viewingPostDetail.value = null
    }

    fun dismissWelcomeBanner() {
        _showWelcomeBanner.value = false
    }

    fun toggleDarkMode() {
        repository.toggleDarkMode()
    }

    fun toggleOnlineStatus() {
        repository.toggleOnlineStatus()
        val status = if (!isOnline.value) "Online dot chalu ho gaya 🟢" else "Offline dot dikhega ⚪️"
        showStatus(status)
    }

    fun showStatus(msg: String) {
        _statusMessage.value = msg
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    fun openStory(story: Story) {
        _activeStory.value = story
    }

    fun closeStory() {
        _activeStory.value = null
    }

    fun openComments(post: Post) {
        _activeCommentsPost.value = post
    }

    fun closeComments() {
        _activeCommentsPost.value = null
    }

    fun addComment(postId: String, text: String) {
        if (text.isNotBlank()) {
            repository.addComment(postId, text)
            // Refresh comments modal if open
            _activeCommentsPost.value = posts.value.find { it.id == postId }
        }
    }

    fun toggleLikePost(postId: String) {
        repository.toggleLikePost(postId)
    }

    fun toggleSavePost(postId: String) {
        repository.toggleSavePost(postId)
    }

    fun toggleLikeReel(reelId: String) {
        repository.toggleLikeReel(reelId)
    }

    fun toggleFollowUser(userId: String) {
        repository.toggleFollowUser(userId)
        val user = users.value.find { it.id == userId }
        if (user != null) {
            val text = if (!user.isFollowedByMe) "${user.name} ko Follow kar liya! ✅" else "${user.name} ko Unfollow kiya"
            showStatus(text)
        }
    }

    fun toggleBlockUser(userId: String) {
        repository.toggleBlockUser(userId)
        val user = users.value.find { it.id == userId }
        if (user != null) {
            val text = if (!user.isBlocked) "${user.name} ko Block kar diya" else "${user.name} Unblock ho gaya"
            showStatus(text)
        }
    }

    fun openUserProfile(user: User) {
        _viewingUserProfile.value = user
    }

    fun closeUserProfile() {
        _viewingUserProfile.value = null
    }

    fun openPostDetail(post: Post) {
        _viewingPostDetail.value = post
    }

    fun closePostDetail() {
        _viewingPostDetail.value = null
    }

    fun openChatWith(user: User) {
        _activeChatUser.value = user
        _showChatList.value = false
        repository.markChatSeen(user.id)
    }

    fun closeChat() {
        _activeChatUser.value = null
    }

    fun openChatList() {
        _showChatList.value = true
    }

    fun closeChatList() {
        _showChatList.value = false
    }

    fun openNotifications() {
        _showNotifications.value = true
    }

    fun closeNotifications() {
        _showNotifications.value = false
    }

    fun sendMessage(receiverId: String, text: String, isVoice: Boolean = false, voiceDuration: Int = 0) {
        if (text.isNotBlank() || isVoice) {
            repository.sendMessage(receiverId, text, isVoice, voiceDuration)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openFollowersDialog(title: String, userList: List<User>) {
        _followersDialogData.value = Pair(title, userList)
    }

    fun closeFollowersDialog() {
        _followersDialogData.value = null
    }

    fun createPost(imageUrl: String, filter: MediaFilter, caption: String, location: String, taggedUsers: List<String>, localUri: String? = null) {
        repository.createPost(imageUrl, filter, caption, location, taggedUsers, localUri)
        _selectedTab.value = 0 // Return to home feed
        showStatus("Aapka Post safaltapoorvak publish ho gaya! 🎉")
    }

    fun createReel(caption: String, musicName: String, durationSec: Int, filter: MediaFilter, gradientColors: List<Long>) {
        repository.createReel(caption, musicName, durationSec, filter, gradientColors)
        _selectedTab.value = 3 // Jump to reels
        showStatus("Naya 9:16 Reel upload ho gaya! 🎬")
    }

    fun createStory(caption: String, mediaUrl: String) {
        repository.createStory(caption, mediaUrl)
        showStatus("Story 24 ghante ke liye publish ho gayi! 📸")
    }

    fun updateProfile(name: String, username: String, bio: String, avatarColorHex: Long) {
        repository.updateProfile(name, username, bio, avatarColorHex)
        showStatus("Profile update ho gaya! ✨")
    }

    fun login(username: String) {
        repository.login(username)
        showStatus("Namaste! Login safal raha.")
    }

    fun signup(name: String, username: String, bio: String, avatarColorHex: Long) {
        repository.signup(name, username, bio, avatarColorHex)
        showStatus("Swagat hai! Naya account ban gaya.")
    }

    // Permission Step-by-Step Flow (Requirement 8)
    fun startPermissionGuide() {
        _permissionStepIndex.value = 0
        _showPermissionGuide.value = true
    }

    fun nextPermissionStep(granted: Boolean) {
        val perms = permissions.value
        val currentIndex = _permissionStepIndex.value
        if (currentIndex < perms.size) {
            val currentPerm = perms[currentIndex]
            repository.setPermissionGranted(currentPerm.id, granted)
            if (!granted) {
                showStatus("Koi baat nahi! Aap is feature ke bina bhi app maze se chala sakte hain 👍")
            } else {
                showStatus("${currentPerm.name} chalu kar diya gaya hai! ✅")
            }
        }
        if (currentIndex + 1 < perms.size) {
            _permissionStepIndex.value = currentIndex + 1
        } else {
            _showPermissionGuide.value = false
            showStatus("Saari Permissions review ho gayi! Dhanyawaad 🙏")
        }
    }

    fun closePermissionGuide() {
        _showPermissionGuide.value = false
    }

    fun refreshFeed() {
        repository.refreshFeed()
        showStatus("Feed update ho gayi! Naye posts upar hain 🔄")
    }
}
