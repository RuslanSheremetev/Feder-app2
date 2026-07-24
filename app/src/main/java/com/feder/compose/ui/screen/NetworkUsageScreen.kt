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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF131313)
private val Surface = Color(0xFF131313)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val SurfaceContainerHighest = Color(0xFF353534)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnPrimaryContainer = Color(0xFF295483)
private val Secondary = Color(0xFFB5C8E2)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val TertiaryContainer = Color(0xFFC5C6C9)
private val OnTertiaryContainer = Color(0xFF505255)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val OutlineVariant = Color(0xFF42474F)
private val Error = Color(0xFFFFB4AB)
private val ErrorContainer = Color(0xFF93000A)
private val OnErrorContainer = Color(0xFFFFDAD6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkUsageScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Network Usage", color = Primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
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
            Spacer(Modifier.height(16.dp))

            // Hero Grid
            // Total Data Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh)
                    .padding(16.dp)
            ) {
                Column {
                    Text("Total Data Usage", color = OnSurfaceVariant, fontWeight = FontWeight.W500, fontSize = 14.sp)
                    Text("4.2 GB", color = Primary, fontWeight = FontWeight.W600, fontSize = 28.sp)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Primary)
                            )
                            Text("Last reset: Oct 24, 2023", color = OnSurfaceVariant, fontSize = 11.sp)
                        }
                        Icon(
                            Icons.Filled.Speed,
                            null,
                            tint = OnPrimaryContainer,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryContainer)
                                .padding(8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Sent / Received
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Upload, null, tint = Secondary, modifier = Modifier.size(20.dp))
                            Text("Total Sent", color = OnSurfaceVariant, fontWeight = FontWeight.W500, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("842.5 MB", color = Secondary, fontWeight = FontWeight.W500, fontSize = 22.sp)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Download, null, tint = Primary, modifier = Modifier.size(20.dp))
                            Text("Total Received", color = OnSurfaceVariant, fontWeight = FontWeight.W500, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("3.4 GB", color = Primary, fontWeight = FontWeight.W500, fontSize = 22.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Usage Breakdown
            Text("USAGE BREAKDOWN", color = Primary, fontWeight = FontWeight.W500, fontSize = 14.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLow
            ) {
                Column {
                    UsageRow(Icons.Filled.Call, "Calls", "135.6 MB", "15.4 MB sent • 120.2 MB received", SecondaryContainer, OnSecondaryContainer)
                    UsageRow(Icons.Filled.PhotoLibrary, "Media", "2.8 GB", "640.2 MB sent • 2.2 GB received", PrimaryContainer, OnPrimaryContainer)
                    UsageRow(Icons.Filled.CloudUpload, "Google Drive", "912.4 MB", "182.1 MB sent • 730.3 MB received", SurfaceContainerHighest, OnSurface)
                    UsageRow(Icons.Filled.Forum, "Messages", "42.8 MB", "4.2 MB sent • 38.6 MB received", SecondaryContainer.copy(alpha = 0.5f), Secondary)
                    UsageRow(Icons.Filled.TrackChanges, "Status", "320.1 MB", "0.6 MB sent • 319.5 MB received", TertiaryContainer.copy(alpha = 0.3f), OnTertiaryContainer, last = true)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Warning
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ErrorContainer.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.Info, null, tint = Error, modifier = Modifier.size(20.dp))
                    Text(
                        "Network usage statistics are based on application-level tracking and may differ from your mobile carrier's official billing reports.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = OnErrorContainer.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Reset Button
            OutlinedButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Filled.RestartAlt, null, tint = Error, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reset Statistics", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 14.sp)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UsageRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    total: String,
    details: String,
    iconBg: Color,
    iconTint: Color,
    last: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, color = OnSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text(total, color = OnSurfaceVariant, fontSize = 11.sp)
            }
            Text(details, color = OnSurfaceVariant, fontSize = 14.sp)
        }
    }
    if (!last) {
        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
    }
}
