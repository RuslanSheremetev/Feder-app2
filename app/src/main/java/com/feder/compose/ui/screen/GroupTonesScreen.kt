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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupTonesScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var syncEnabled by remember { mutableStateOf(true) }
    var selectedTone by remember { mutableStateOf(0) }

    val tones = listOf(
        ToneItem("Midnight Default", Icons.Filled.MusicNote),
        ToneItem("Velocity Pulse", Icons.Filled.Speed),
        ToneItem("Digital Echo", Icons.Filled.Waves),
        ToneItem("Neon Spark", Icons.Filled.ElectricBolt)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Group Tones", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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

            Text(
                "Group Tones",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W600, fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Customize how you hear your communities.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Sync Toggle Card
            MaterialTheme.colorScheme.surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SyncAlt, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Use same as message tones", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                            Text("Sync global notification sounds", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = syncEnabled,
                        onCheckedChange = { syncEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Tones list (disabled if synced)
            Column(
                modifier = Modifier.then(
                    if (syncEnabled) Modifier else Modifier
                )
            ) {
                Text(
                    "AVAILABLE TONES",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    ),
                    color = if (syncEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(12.dp))

                val columns = 2
                val rows = (tones.size + columns - 1) / columns

                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until columns) {
                            val index = row * columns + col
                            if (index < tones.size) {
                                val tone = tones[index]
                                MaterialTheme.colorScheme.surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .then(
                                            if (!syncEnabled) Modifier.clickable { selectedTone = index }
                                            else Modifier
                                        ),
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
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                tone.icon,
                                                null,
                                                tint = if (syncEnabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                tone.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                                                color = if (syncEnabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        RadioButton(
                                            selected = selectedTone == index,
                                            onClick = if (!syncEnabled) {{ selectedTone = index }} else {{}},
                                            enabled = !syncEnabled,
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primaryContainer,
                                                unselectedColor = MaterialTheme.colorScheme.outline,
                                                disabledSelectedColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                disabledUnselectedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            )
                                        )
                                    }
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Audio Visualizer Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "LIVE PREVIEW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.W500,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Audio Waveform Analysis",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.W500,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    FloatingActionButton(
                        onClick = {},
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Info card
            MaterialTheme.colorScheme.surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(20.dp))
                    Text(
                        "Selecting a unique tone for groups helps you differentiate between direct messages and community activity without looking at your screen.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private data class ToneItem(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
