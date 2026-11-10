package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Post
import com.example.model.User
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.InstaBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MeraInstaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MeraInstaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                MeraInstaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MeraInstaApp(viewModel: MeraInstaViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()
    val activeCommentsPost by viewModel.activeCommentsPost.collectAsState()
    val followersDialogData by viewModel.followersDialogData.collectAsState()
    val showPermissionGuide by viewModel.showPermissionGuide.collectAsState()
    val permissionStepIndex by viewModel.permissionStepIndex.collectAsState()
    val permissions by viewModel.permissions.collectAsState()
    val showChatList by viewModel.showChatList.collectAsState()
    val activeChatUser by viewModel.activeChatUser.collectAsState()
    val showNotifications by viewModel.showNotifications.collectAsState()
    val viewingUserProfile by viewModel.viewingUserProfile.collectAsState()
    val viewingPostDetail by viewModel.viewingPostDetail.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearStatus()
        }
    }

    // Back handling for sub-screens
    BackHandler(
        enabled = activeStory != null ||
                  activeCommentsPost != null ||
                  followersDialogData != null ||
                  showPermissionGuide ||
                  activeChatUser != null ||
                  showChatList ||
                  showNotifications ||
                  viewingUserProfile != null ||
                  viewingPostDetail != null ||
                  selectedTab != 0
    ) {
        when {
            activeStory != null -> viewModel.closeStory()
            activeCommentsPost != null -> viewModel.closeComments()
            followersDialogData != null -> viewModel.closeFollowersDialog()
            showPermissionGuide -> viewModel.closePermissionGuide()
            activeChatUser != null -> viewModel.closeChat()
            showChatList -> viewModel.closeChatList()
            showNotifications -> viewModel.closeNotifications()
            viewingPostDetail != null -> viewModel.closePostDetail()
            viewingUserProfile != null -> viewModel.closeUserProfile()
            selectedTab != 0 -> viewModel.selectTab(0)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Hide bottom bar on fullscreen modals (Story viewer, Reel fullscreen, Chat conversation)
            val hideBottomBar = activeStory != null || activeChatUser != null
            if (!hideBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    // Tab 0: Home
                    NavigationBarItem(
                        selected = selectedTab == 0 && viewingUserProfile == null,
                        onClick = { viewModel.selectTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0 && viewingUserProfile == null) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // Tab 1: Search
                    NavigationBarItem(
                        selected = selectedTab == 1 && viewingUserProfile == null,
                        onClick = { viewModel.selectTab(1) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_search")
                    )

                    // Tab 2: + Post
                    NavigationBarItem(
                        selected = selectedTab == 2 && viewingUserProfile == null,
                        onClick = { viewModel.selectTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Default.AddBox else Icons.Outlined.AddBox,
                                contentDescription = "Add Post"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_create")
                    )

                    // Tab 3: Reels
                    NavigationBarItem(
                        selected = selectedTab == 3 && viewingUserProfile == null,
                        onClick = { viewModel.selectTab(3) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 3) Icons.Default.Movie else Icons.Outlined.Movie,
                                contentDescription = "Reels"
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_reels")
                    )

                    // Tab 4: Profile
                    NavigationBarItem(
                        selected = selectedTab == 4 || viewingUserProfile != null,
                        onClick = { viewModel.selectTab(4) },
                        icon = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(currentUser?.avatarColorHex ?: 0xFF833AB4))
                                    .border(
                                        if (selectedTab == 4) 2.dp else 0.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape
                                    )
                            ) {
                                Text(
                                    text = currentUser?.name?.take(1)?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tabs Content
            when {
                viewingUserProfile != null -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        userToView = viewingUserProfile,
                        onBack = { viewModel.closeUserProfile() },
                        onPostClick = { viewModel.openPostDetail(it) },
                        onMessageUser = { viewModel.openChatWith(it) }
                    )
                }
                selectedTab == 0 -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenStory = { viewModel.openStory(it) },
                        onAddStory = {
                            viewModel.createStory("Nayi Story! ✨", "")
                        },
                        onCommentClick = { viewModel.openComments(it) },
                        onUserClick = { userId ->
                            val user = viewModel.users.value.find { it.id == userId }
                            if (user != null) viewModel.openUserProfile(user)
                        }
                    )
                }
                selectedTab == 1 -> {
                    SearchScreen(
                        viewModel = viewModel,
                        onPostClick = { viewModel.openPostDetail(it) },
                        onUserClick = { viewModel.openUserProfile(it) }
                    )
                }
                selectedTab == 2 -> {
                    CreatePostScreen(viewModel = viewModel)
                }
                selectedTab == 3 -> {
                    ReelsScreen(viewModel = viewModel)
                }
                selectedTab == 4 -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        onPostClick = { viewModel.openPostDetail(it) },
                        onMessageUser = { viewModel.openChatWith(it) }
                    )
                }
            }

            // Post Detail Modal View (e.g. from Search or Profile Grid)
            if (viewingPostDetail != null) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            IconButton(onClick = { viewModel.closePostDetail() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Text("Post Details", style = MaterialTheme.typography.titleMedium)
                        }
                        PostCard(
                            post = viewingPostDetail!!,
                            onLikeToggle = { viewModel.toggleLikePost(viewingPostDetail!!.id) },
                            onSaveToggle = { viewModel.toggleSavePost(viewingPostDetail!!.id) },
                            onCommentClick = { viewModel.openComments(viewingPostDetail!!) },
                            onUserClick = {
                                val u = viewModel.users.value.find { it.username == viewingPostDetail!!.username }
                                if (u != null) {
                                    viewModel.closePostDetail()
                                    viewModel.openUserProfile(u)
                                }
                            },
                            onShareClick = {
                                viewModel.showStatus("Post link share ho gaya!")
                            }
                        )
                    }
                }
            }

            // Direct Chat / Messages Overlay
            if (showChatList || activeChatUser != null) {
                ChatScreen(
                    viewModel = viewModel,
                    onBack = {
                        if (activeChatUser != null) viewModel.closeChat()
                        else viewModel.closeChatList()
                    }
                )
            }

            // Notifications Overlay
            if (showNotifications) {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.closeNotifications() }
                )
            }

            // Fullscreen 24-hour Story Viewer
            if (activeStory != null) {
                StoryViewer(
                    story = activeStory!!,
                    onClose = { viewModel.closeStory() },
                    onReply = { reply ->
                        viewModel.showStatus("${activeStory!!.username} ko message chala gaya: $reply")
                    }
                )
            }

            // Comments Bottom Sheet
            if (activeCommentsPost != null) {
                CommentsBottomSheet(
                    post = activeCommentsPost!!,
                    onDismiss = { viewModel.closeComments() },
                    onAddComment = { text ->
                        viewModel.addComment(activeCommentsPost!!.id, text)
                    }
                )
            }

            // Followers / Following List Dialog
            if (followersDialogData != null) {
                val (title, list) = followersDialogData!!
                FollowersDialog(
                    title = title,
                    users = list,
                    onDismiss = { viewModel.closeFollowersDialog() },
                    onToggleFollow = { viewModel.toggleFollowUser(it) },
                    onToggleBlock = { viewModel.toggleBlockUser(it) },
                    onUserClick = { viewModel.openUserProfile(it) }
                )
            }

            // Step-by-Step Permissions Guide (Requirement 8)
            if (showPermissionGuide) {
                PermissionsDialog(
                    permissions = permissions,
                    currentIndex = permissionStepIndex,
                    onAnswer = { granted ->
                        viewModel.nextPermissionStep(granted)
                    },
                    onDismiss = { viewModel.closePermissionGuide() }
                )
            }
        }
    }
}
