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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF131313)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val SurfaceVariant = Color(0xFF353534)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Secondary = Color(0xFFB5C8E2)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OutlineVariant = Color(0xFF42474F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Data and Storage",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 22.sp
                        ),
                        color = Primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceContainerLow)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Storage Summary Card
            StorageSummaryCard()

            Spacer(Modifier.height(8.dp))

            // Usage Section
            SectionHeader("Usage")
            SettingsItem(
                icon = Icons.Filled.NetworkCheck,
                title = "Network usage",
                subtitle = "2.1 GB sent • 8.4 GB received"
            )
            SettingsItem(
                icon = Icons.Filled.Storage,
                title = "Storage management",
                subtitle = "Clear cache and delete old media"
            )

            Spacer(Modifier.height(16.dp))

            // Media Auto-Download
            SectionHeader("Media auto-download")
            Text(
                "Voice messages are always auto-downloaded",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            SimpleItem("When using mobile data", "Photos")
            SimpleItem("When connected on Wi-Fi", "All media")
            SimpleItem("When roaming", "No media")

            Spacer(Modifier.height(16.dp))

            // Media Upload Quality
            SectionHeader("Media upload quality")
            SettingsItem(
                icon = Icons.Filled.PhotoCamera,
                title = "Photo upload quality",
                subtitle = "Auto (recommended)",
                showDropdown = true
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            letterSpacing = 2.sp
        ),
        color = Primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun StorageSummaryCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Storage usage",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp
                    ),
                    color = OnSurface
                )
                Text(
                    "85% full",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Storage bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceVariant)
            ) {
                Row(Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.45f)
                            .background(PrimaryContainer)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.25f)
                            .background(Secondary)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.15f)
                            .background(OutlineVariant)
                    )
                    Spacer(modifier = Modifier.weight(0.15f))
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "12.4 GB used of 15 GB",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = OnSurfaceVariant
                )
                TextButton(onClick = {}) {
                    Text(
                        "Manage",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 14.sp
                        ),
                        color = Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    showDropdown: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = OnSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp
                    ),
                    color = OnSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        letterSpacing = 0.25.sp
                    ),
                    color = OnSurfaceVariant
                )
            }
        }
        Icon(
            if (showDropdown) Icons.Filled.ExpandMore else ArrowForward,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SimpleItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp
                ),
                color = OnSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    letterSpacing = 0.25.sp
                ),
                color = OnSurfaceVariant
            )
        }
    }
}
