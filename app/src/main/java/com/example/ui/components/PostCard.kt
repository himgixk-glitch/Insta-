package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaFilter
import com.example.model.Post
import com.example.ui.theme.InstaBlue
import com.example.ui.theme.InstaHeartRed
import com.example.ui.theme.InstaStoryGradient

@Composable
fun PostCard(
    post: Post,
    onLikeToggle: () -> Unit,
    onSaveToggle: () -> Unit,
    onCommentClick: () -> Unit,
    onUserClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHeartAnim by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 12.dp)
            .testTag("post_card_${post.id}")
    ) {
        // Post Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // User Avatar with Story ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, InstaStoryGradient, CircleShape)
                    .padding(2.dp)
                    .clickable { onUserClick() }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF833AB4))
                ) {
                    val initial = post.username.take(1).uppercase()
                    Text(
                        text = initial,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onUserClick() }
            ) {
                Text(
                    text = post.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (post.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = post.location,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(onClick = { /* More options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Post Media Area with Double Tap detection & Filter overlay
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFF262626))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (!post.isLiked) {
                                onLikeToggle()
                            }
                            showHeartAnim = true
                        }
                    )
                }
        ) {
            // Background Visual Art depending on image/filter
            if (post.localUri != null) {
                AsyncImage(
                    model = post.localUri,
                    contentDescription = "Post image",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Generative vibrant placeholder canvas
                val brush = when (post.filter) {
                    MediaFilter.WARM -> Brush.verticalGradient(listOf(Color(0xFFFCB045), Color(0xFFFD1D1D), Color(0xFF833AB4)))
                    MediaFilter.COOL -> Brush.verticalGradient(listOf(Color(0xFF2193B0), Color(0xFF6DD5ED), Color(0xFF0F2027)))
                    MediaFilter.BW -> Brush.verticalGradient(listOf(Color(0xFF232526), Color(0xFF414345), Color(0xFF757F9A)))
                    MediaFilter.HDR -> Brush.radialGradient(listOf(Color(0xFFFF007F), Color(0xFF7928CA), Color(0xFF111111)))
                    MediaFilter.BLUR -> Brush.verticalGradient(listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2), Color(0xFF121212)))
                    MediaFilter.NORMAL -> Brush.verticalGradient(listOf(Color(0xFF1E3C72), Color(0xFF2A5298), Color(0xFF0F2027)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush)
                ) {
                    // Filter badge tag on top right
                    if (post.filter != MediaFilter.NORMAL) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "✨ ${post.filter.displayName}",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Decorative center typography
                    Text(
                        text = "Mera Insta",
                        color = Color.White.copy(alpha = 0.35f),
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Double Tap Heart animation
            DoubleTapHeartAnimation(
                visible = showHeartAnim,
                onAnimationEnd = { showHeartAnim = false }
            )
        }

        // Action Buttons Row (Like, Comment, Share, Save)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(
                onClick = onLikeToggle,
                modifier = Modifier.testTag("like_button_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) InstaHeartRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }

            IconButton(
                onClick = onCommentClick,
                modifier = Modifier.testTag("comment_button_${post.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier.testTag("share_button_${post.id}")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onSaveToggle,
                modifier = Modifier.testTag("save_button_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (post.isSaved) InstaBlue else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Likes Count
        Text(
            text = "${post.likesCount} logo ne pasand kiya",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        // Caption
        if (post.caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            val annotatedCaption = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                    append("${post.username} ")
                }
                append(post.caption)
            }
            Text(
                text = annotatedCaption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Comments Count shortcut
        if (post.comments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sabhi ${post.comments.size} comments dekhein...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .clickable { onCommentClick() }
            )
        }

        // Timestamp
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = post.timestamp,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
    }
}
