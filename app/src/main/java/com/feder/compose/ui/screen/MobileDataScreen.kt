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
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val SurfaceContainerHighest = Color(0xFF353534)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnPrimary = Color(0xFF00325B)
private val Secondary = Color(0xFFB5C8E2)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val OutlineVariant = Color(0xFF42474F)
private val Error = Color(0xFFFFB4AB)
private val ErrorContainer = Color(0xFF93000A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileDataScreen(onBack: () -> Unit) {
    var photosEnabled by remember { mutableStateOf(true) }
    var audioEnabled by remember { mutableStateOf(false) }
    var videosEnabled by remember { mutableStateOf(false) }
    var documentsEnabled by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("When using mobile data", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column {
                    // Header
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Auto-Download",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.W600,
                                fontSize = 24.sp
                            ),
                            color = OnSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Select the media types you want to automatically download when connected to mobile data.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = OnSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    // Options
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        CheckOption("Photos", Icons.Filled.Photo, photosEnabled, { photosEnabled = it })
                        CheckOption("Audio", Icons.Filled.Audiotrack, audioEnabled, { audioEnabled = it })
                        CheckOption("Videos", Icons.Filled.Videocam, videosEnabled, { videosEnabled = it })
                        CheckOption("Documents", Icons.Filled.Description, documentsEnabled, { documentsEnabled = it })
                    }

                    Spacer(Modifier.height(8.dp))

                    // Info banner
                    Surface(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerHighest.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(4.dp, Primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Info, null, tint = Primary, modifier = Modifier.size(20.dp))
                            Text(
                                "Voice messages are always auto-downloaded for the best communication experience.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onBack) {
                            Text("Cancel", color = Primary, fontWeight = FontWeight.W500, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = OnPrimary
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("OK", fontWeight = FontWeight.W500, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Bottom cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(Icons.Filled.Wifi, "Wi-Fi Settings", "Configure heavy media for home connections.", SecondaryContainer.copy(alpha = 0.3f), Secondary, Modifier.weight(1f))
                InfoCard(Icons.Filled.BarChart, "Data Usage", "Monitor your monthly velocity traffic.", ErrorContainer.copy(alpha = 0.2f), Error, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CheckOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) Primary else OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp
                ),
                color = OnSurface
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryContainer,
                uncheckedColor = OnSurfaceVariant,
                checkmarkColor = OnPrimary
            )
        )
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable { },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W500, fontSize = 14.sp),
                color = OnSurfaceVariant
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = OnSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
