package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Background = Color(0xFF131313)
private val Surface = Color(0xFF131313)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val SurfaceContainerHighest = Color(0xFF353534)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Outline = Color(0xFF8C919A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var selectedOption by remember { mutableStateOf(0) }
    val options = listOf("Everyone", "My contacts", "My contacts except...", "Nobody")
    val visibilityText = listOf("Everyone", "My Contacts", "Selected Contacts", "Nobody")

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Profile Photo", color = Primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.8f))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                "Who can see my profile photo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 24.sp
                ),
                color = OnSurface
            )

            Spacer(Modifier.height(16.dp))

            // Radio options
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column {
                    options.forEachIndexed { index, option ->
                        val isLast = index == options.size - 1
                        val isContactsExcept = index == 2

                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedOption = index }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        option,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.W400,
                                            fontSize = 16.sp
                                        ),
                                        color = OnSurface
                                    )
                                    if (isContactsExcept) {
                                        Text(
                                            "Exclude specific people",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.W500,
                                                fontSize = 11.sp
                                            ),
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (selectedOption == index)
                                                Modifier.border(2.dp, PrimaryContainer, CircleShape)
                                            else
                                                Modifier.border(2.dp, Outline, CircleShape)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedOption == index) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryContainer)
                                        )
                                    }
                                }
                            }
                            if (!isLast) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = Color.White.copy(alpha = 0.05f),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Info card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SecondaryContainer.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = OnSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "If you don't share your profile photo, you won't see the profile photos of other people.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDYvKVdOA9n1bxxVW0BokjtUhUWsRbpJfgxIaP-bQIskAm3CuLxVIXfSFwpWK-bhOCLg_d4yu-r4bZ0JtXIuqDyOGnqqznqgr7ZlCTVqOhukJMOceY7xlbTeKjHBiOgtSyAJMXAcmwygo_HQI2dIs51CnMT1rW0hnhKuxszQgy9afEPw2XCY_VYVq7OBAuuiZneb-2XlmtUDFTwAlbPvrdiyaX575jsf-1UPPP3cL3JJhqk0R6TcJNVTmi5SNBUnPgyhF7Q1GtI7gw",
                        contentDescription = "Profile preview",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryContainer.copy(alpha = 0.2f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Your Preview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp
                        ),
                        color = OnSurface
                    )
                    Text(
                        "Your current photo is visible to ${visibilityText[selectedOption]}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 11.sp
                        ),
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
