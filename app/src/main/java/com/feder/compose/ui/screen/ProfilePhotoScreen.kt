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
import androidx.compose.material3.MaterialTheme
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePhotoScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var selectedOption by remember { mutableStateOf(0) }
    val options = listOf("Everyone", "My contacts", "My contacts except...", "Nobody")
    val visibilityText = listOf("Everyone", "My Contacts", "Selected Contacts", "Nobody")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile Photo", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
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
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            // Radio options
            MaterialTheme.colorScheme.surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
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
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isContactsExcept) {
                                        Text(
                                            "Exclude specific people",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.W500,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (selectedOption == index)
                                                Modifier.border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                            else
                                                Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedOption == index) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
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
            MaterialTheme.colorScheme.surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "If you don't share your profile photo, you won't see the profile photos of other people.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                            .border(2.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Your Preview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Your current photo is visible to ${visibilityText[selectedOption]}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
