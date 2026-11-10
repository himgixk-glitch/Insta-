package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Post
import com.example.model.Story
import com.example.ui.components.PostCard
import com.example.ui.components.StoryCircle
import com.example.ui.theme.InstaBlue
import com.example.ui.theme.OnlineGreen
import com.example.viewmodel.MeraInstaViewModel

@Composable
fun HomeScreen(
    viewModel: MeraInstaViewModel,
    onOpenStory: (Story) -> Unit,
    onAddStory: () -> Unit,
    onCommentClick: (Post) -> Unit,
    onUserClick: (String) -> Unit
) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val showWelcomeBanner by viewModel.showWelcomeBanner.collectAsState()
    val unreadNotifsCount = notifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Instagram Style Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // "Mera Insta" brand logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Mera Insta",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Online / Offline Dot Indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) OnlineGreen else Color.Gray)
                        .clickable { viewModel.toggleOnlineStatus() }
                        .testTag("online_status_dot")
                )
            }

            // Theme Toggle (Light / Dark mode)
            IconButton(
                onClick = { viewModel.toggleDarkMode() },
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = "Theme Toggle",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Notifications Heart Icon
            IconButton(
                onClick = { viewModel.openNotifications() },
                modifier = Modifier.testTag("notifications_top_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotifsCount > 0) {
                            Badge(containerColor = Color.Red) {
                                Text("$unreadNotifsCount", fontSize = 9.sp)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Chat / DM Icon
            IconButton(
                onClick = { viewModel.openChatList() },
                modifier = Modifier.testTag("chat_dm_top_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Direct Messages",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp)

        // Main Feed (Stories + Welcome Banner + Posts)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_feed_list")
        ) {
            // Welcome Banner (Requirement 9 & first screen message)
            if (showWelcomeBanner) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .testTag("welcome_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Namaste! Mera Insta me Welcome.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Yaha se Post, Reels, Story sab kar sakte ho, aur Dosto ko Follow kar sakte ho.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { viewModel.startPermissionGuide() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = InstaBlue),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = "Permissions Check Karein (10) →",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.dismissWelcomeBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Stories Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(stories, key = { it.id }) { story ->
                        StoryCircle(
                            story = story,
                            isCurrentUser = story.userId == "user_me",
                            onClick = { onOpenStory(story) },
                            onAddClick = { onAddStory() }
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
            }

            // Refresh indicator button
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.refreshFeed() }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh Feed",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Feed Refresh Karein",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Posts List
            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onLikeToggle = { viewModel.toggleLikePost(post.id) },
                    onSaveToggle = { viewModel.toggleSavePost(post.id) },
                    onCommentClick = { onCommentClick(post) },
                    onUserClick = { onUserClick(post.userId) },
                    onShareClick = {
                        viewModel.showStatus("${post.username} ka post share link copy ho gaya! 🔗")
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)
            }
        }
    }
}
