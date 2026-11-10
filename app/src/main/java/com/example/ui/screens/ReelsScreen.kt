package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.MediaFilter
import com.example.model.Reel
import com.example.ui.theme.InstaBlue
import com.example.ui.theme.InstaHeartRed
import com.example.viewmodel.MeraInstaViewModel

@Composable
fun ReelsScreen(
    viewModel: MeraInstaViewModel
) {
    val reels by viewModel.reels.collectAsState()
    val users by viewModel.users.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { reels.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_screen")
    ) {
        if (reels.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Koi Reel maujood nahi hai. Pehli Reel banayein!",
                    color = Color.White
                )
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = reels[page]
                val author = users.find { it.username == reel.username }
                ReelItem(
                    reel = reel,
                    isFollowed = author?.isFollowedByMe ?: false,
                    onToggleLike = { viewModel.toggleLikeReel(reel.id) },
                    onToggleFollow = { author?.let { viewModel.toggleFollowUser(it.id) } },
                    onShare = { viewModel.showStatus("Reel share link copy ho gaya! 🎬") }
                )
            }
        }

        // Top Controls: "Reels" title & "+ Banayein" button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = "Reels",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("create_reel_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Create Reel",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ Nayi Reel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Create Reel Dialog
        if (showCreateDialog) {
            CreateReelDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { caption, music, duration, filter ->
                    val gradient = listOf(0xFF833AB4, 0xFFFD1D1D, 0xFFFCB045)
                    viewModel.createReel(caption, music, duration, filter, gradient)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun ReelItem(
    reel: Reel,
    isFollowed: Boolean,
    onToggleLike: () -> Unit,
    onToggleFollow: () -> Unit,
    onShare: () -> Unit
) {
    // Rotating disc animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Vertical 9:16 Video Canvas Representation
        val colors = reel.gradientColors.map { Color(it) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors))
        ) {
            // Visual simulated reel motion
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Playing",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${reel.durationSec}s Reel • 9:16 HD",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        // Bottom Shadow Gradient for text legibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Right Floating Action Column (Like, Comment, Share, Save, Music)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 64.dp)
        ) {
            // Like Button
            IconButton(
                onClick = onToggleLike,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like Reel",
                    tint = if (reel.isLiked) InstaHeartRed else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "${reel.likesCount}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comment Button
            IconButton(
                onClick = { /* Reel comments */ },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comments",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = "${reel.commentsCount}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Share Button
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Share",
                color = Color.White,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Rotating Music Disc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262626))
                    .border(2.dp, Color.White, CircleShape)
                    .rotate(rotation)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom Left Info Overlay: Username, Follow, Caption, Music
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.8f)
                .padding(start = 16.dp, bottom = 64.dp)
        ) {
            // User Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCB045))
                ) {
                    Text(
                        text = reel.username.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = reel.username,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Follow Button on Reel
                Surface(
                    color = if (isFollowed) Color.White.copy(alpha = 0.2f) else InstaBlue,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onToggleFollow() }
                ) {
                    Text(
                        text = if (isFollowed) "Following" else "Follow",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caption
            Text(
                text = reel.caption,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Music Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Audio",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = reel.musicName,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CreateReelDialog(
    onDismiss: () -> Unit,
    onCreate: (caption: String, music: String, duration: Int, filter: MediaFilter) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(30) }
    var selectedMusic by remember { mutableStateOf("Trending Beats - Mera Insta") }
    var selectedFilter by remember { mutableStateOf(MediaFilter.NORMAL) }

    val musicOptions = listOf(
        "Trending Beats - Mera Insta",
        "Bollywood Instrumental Chill",
        "Desi Hip-Hop Energy 🔥",
        "Acoustic Sunset Lo-Fi"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("create_reel_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "9:16 Reel Banayein 🎬",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Selector: 15s, 30s, 60s
                Text(
                    text = "Video Duration Chunein:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(15, 30, 60).forEach { dur ->
                        FilterChip(
                            selected = selectedDuration == dur,
                            onClick = { selectedDuration = dur },
                            label = { Text("${dur} Second") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Music Selector
                Text(
                    text = "Music / Audio Chunein 🎵:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    musicOptions.take(2).forEach { music ->
                        FilterChip(
                            selected = selectedMusic == music,
                            onClick = { selectedMusic = music },
                            label = { Text(music, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Caption
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Reel Caption") },
                    placeholder = { Text("Trending audio pe dance / comedy #Viral") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onCreate(
                            caption.ifEmpty { "Trending 9:16 Reel! 🔥 #MeraInsta" },
                            selectedMusic,
                            selectedDuration,
                            selectedFilter
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InstaBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("publish_reel_button")
                ) {
                    Text(
                        text = "Reel Upload Karein 🚀",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
