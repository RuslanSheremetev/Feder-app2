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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallTonesScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var selectedRingtone by remember { mutableStateOf(0) }
    var selectedVibration by remember { mutableStateOf(1) }
    var vibrateWhileRinging by remember { mutableStateOf(true) }
    var playingIndex by remember { mutableStateOf(-1) }
    var volume by remember { mutableStateOf(0.7f) }

    val ringtones = listOf(
        Ringtone("Stellar Velocity", "Default Melody"),
        Ringtone("Nebula Pulse", "Ambient Synth"),
        Ringtone("Retro Drive", "80s Digital"),
        Ringtone("Midnight Echo", "Minimal Bass")
    )

    val vibrations = listOf(
        Vibration("Off", "Silent alerts", Icons.Filled.FindReplace),
        Vibration("Short", "Double tap", Icons.Filled.Vibration),
        Vibration("Long", "Continuous pulse", Icons.Filled.Vibration),
        Vibration("Dynamic", "Matches melody", Icons.Filled.Waves)
    )

    // Auto-stop playback
    LaunchedEffect(playingIndex) {
        if (playingIndex >= 0) {
            delay(3000)
            playingIndex = -1
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Call Tones", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
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
                .padding(bottom = 80.dp)
        ) {
            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDXqGgF7EhCKnBsPmouGK8jGtfkgcoWWvYDoglZqBLh92DNew9uhpFV5l7G95UMcXqXYg2SVDWiOnPls5ihe-dqMLhI6yYCxBkCZDxRW3jUQOrBRN5qRGvOgdlNsOFiOt-gGHTMhaSvmGPCsvfXGe_9-9vncmySuV8fE2cI6eKeKJ1kQIlP86TtBMRysEtNPC0atOeFLQvrUeXflARaTPxpGAdL1J4JkXj1ib1e0SaACqv0FbBPWizfNLexELt4K4FaCIK1BvdtfBo",
                    contentDescription = "Call tones",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.4f), MaterialTheme.colorScheme.background)
                            )
                        )
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
                ) {
                    Text("Call Tones", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W600, fontSize = 28.sp)
                    Text("Personalize your velocity experience", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ringtones
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ringtones", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 16.sp)
                MaterialTheme.colorScheme.surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        "${ringtones.size} Available",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            MaterialTheme.colorScheme.surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Column {
                    ringtones.forEachIndexed { index, ringtone ->
                        val isSelected = selectedRingtone == index
                        val isPlaying = playingIndex == index

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isSelected) Modifier.background(MaterialTheme.colorScheme.surfaceContainer).border(
                                        4.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(0.dp)
                                    ) else Modifier
                                )
                                .clickable { selectedRingtone = index }
                                .padding(16.dp),
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
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.MusicNote,
                                        null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(ringtone.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                                    Text(ringtone.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { playingIndex = if (isPlaying) -1 else index }) {
                                    Icon(
                                        if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                        null,
                                        tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Add Custom Ringtone
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Custom Ringtone", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.W500, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Vibration
            Text(
                "Vibration",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..1) {
                            val index = row * 2 + col
                            val vibration = vibrations[index]
                            val isSelected = selectedVibration == index

                            MaterialTheme.colorScheme.surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedVibration = index },
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        vibration.icon,
                                        null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        vibration.name,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.W500,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        vibration.description,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(Modifier.height(16.dp))

            // Ringtone Volume
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Ringtone Volume", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                }
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.width(150.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primaryContainer,
                        activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // Vibrate while ringing
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Filled.Vibration, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Vibrate while ringing", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                }
                Switch(
                    checked = vibrateWhileRinging,
                    onCheckedChange = { vibrateWhileRinging = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private data class Ringtone(val name: String, val description: String)
private data class Vibration(val name: String, val description: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
