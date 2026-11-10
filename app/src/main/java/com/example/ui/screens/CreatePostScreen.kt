package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaFilter
import com.example.ui.theme.InstaBlue
import com.example.ui.theme.InstaStoryGradient
import com.example.viewmodel.MeraInstaViewModel

@Composable
fun CreatePostScreen(
    viewModel: MeraInstaViewModel
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFilter by remember { mutableStateOf(MediaFilter.NORMAL) }
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var taggedPeople by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    val quickLocations = listOf("Mumbai, Bandra", "New Delhi, CP", "Goa Beach", "Bangalore", "Jaipur Palace")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("create_post_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Naya Post Banayein 📸",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val tags = taggedPeople.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    viewModel.createPost(
                        imageUrl = selectedUri?.toString() ?: "generative_gradient",
                        filter = selectedFilter,
                        caption = caption,
                        location = location,
                        taggedUsers = tags,
                        localUri = selectedUri?.toString()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = InstaBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("publish_post_button")
            ) {
                Text("Share Karein", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Preview & Picker
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                .testTag("photo_picker_box")
        ) {
            if (selectedUri != null) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = "Selected media",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val previewBrush = when (selectedFilter) {
                    MediaFilter.WARM -> Brush.verticalGradient(listOf(Color(0xFFFCB045), Color(0xFFFD1D1D), Color(0xFF833AB4)))
                    MediaFilter.COOL -> Brush.verticalGradient(listOf(Color(0xFF2193B0), Color(0xFF6DD5ED), Color(0xFF0F2027)))
                    MediaFilter.BW -> Brush.verticalGradient(listOf(Color(0xFF232526), Color(0xFF414345), Color(0xFF757F9A)))
                    MediaFilter.HDR -> Brush.radialGradient(listOf(Color(0xFFFF007F), Color(0xFF7928CA), Color(0xFF111111)))
                    MediaFilter.BLUR -> Brush.verticalGradient(listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2), Color(0xFF121212)))
                    MediaFilter.NORMAL -> InstaStoryGradient
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(previewBrush)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Pick Photo",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gallery se Photo chunein ya click karein",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Filter: ${selectedFilter.displayName}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Options Row
        Text(
            text = "Filter Lagayein ✨",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MediaFilter.values().forEach { filter ->
                val isSelected = filter == selectedFilter
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) InstaBlue else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { selectedFilter = filter }
                        .testTag("filter_option_${filter.name}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = filter.description,
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Caption Input
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text("Caption likhein...") },
            placeholder = { Text("Apne post ke baare me kuch batayein #MeraInsta") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("post_caption_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Location Input & Chips
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location jodein 📍") },
            placeholder = { Text("Jaise: Mumbai, India Gate, Goa Beach") },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = InstaBlue)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick location chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickLocations.forEach { loc ->
                AssistChip(
                    onClick = { location = loc },
                    label = { Text(loc, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tag People Input
        OutlinedTextField(
            value = taggedPeople,
            onValueChange = { taggedPeople = it },
            label = { Text("Tag People 👥") },
            placeholder = { Text("Jaise: @rahul_verma, @priya_sharma") },
            leadingIcon = {
                Icon(Icons.Default.PersonAdd, contentDescription = "Tag", tint = InstaBlue)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
