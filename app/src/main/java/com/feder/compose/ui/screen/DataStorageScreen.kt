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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageScreen(onBack: () -> Unit) {
    var showManageStorage by remember { mutableStateOf(false) }
    var showNetworkUsage by remember { mutableStateOf(false) }
    var showMobileData by remember { mutableStateOf(false) }

    if (showMobileData) { MobileDataScreen(onBack = { showMobileData = false }); return }
    if (showNetworkUsage) { NetworkUsageScreen(onBack = { showNetworkUsage = false }); return }
    if (showManageStorage) {
        ManageStorageScreen(onBack = { showManageStorage = false })
        return
    }
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Data and Storage",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                subtitle = "2.1 GB sent • 8.4 GB received",
                onClick = { showNetworkUsage = true }
            )
            SettingsItem(
                icon = Icons.Filled.Storage,
                title = "Storage management",
                subtitle = "Clear cache and delete old media",
                onClick = { showManageStorage = true }
            )

            Spacer(Modifier.height(16.dp))

            // Media Auto-Download
            SectionHeader("Media auto-download")
            Text(
                "Voice messages are always auto-downloaded",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            SimpleItem("When using mobile data", "Photos", onClick = { showMobileData = true })
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
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun StorageSummaryCard() {
    MaterialTheme.colorScheme.surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "85% full",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Storage bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.45f)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.25f)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.15f)
                            .background(MaterialTheme.colorScheme.outlineVariant)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {}) {
                    Text(
                        "Manage",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
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
    showDropdown: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        letterSpacing = 0.25.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            if (showDropdown) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SimpleItem(title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    letterSpacing = 0.25.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
