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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var selectedOption by remember { mutableStateOf(0) }
    val options = listOf("My contacts", "My contacts except...", "Only share with...")
    val icons = listOf(Icons.Filled.Contacts, Icons.Filled.PersonRemove, Icons.Filled.Favorite)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Status", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f))
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
            Spacer(Modifier.height(16.dp))

            // Header
            Text(
                "Who can see my status updates",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500, fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Changes to your privacy settings will apply to status updates that you post from now on.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Radio options
            options.forEachIndexed { index, option ->
                MaterialTheme.colorScheme.surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedOption = index },
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icons[index],
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                option,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        RadioButton(
                            selected = selectedOption == index,
                            onClick = { selectedOption = index },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 24 Hour Notice
            MaterialTheme.colorScheme.surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Status updates will disappear after 24 hours. Your current status updates will remain visible until they expire.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Facebook Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Facebook",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500, fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "CONNECT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            MaterialTheme.colorScheme.surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { },
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1877F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "f",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W700,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                "Facebook Stories",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Tap to share status to Facebook",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Bottom illustration
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(192.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.surfaceVariant, Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PrivacyTip,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Secure, end-to-end encrypted privacy controls",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
