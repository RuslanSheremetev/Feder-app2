package com.feder.compose.ui.screen

import androidx.compose.animation.*
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
import kotlinx.coroutines.delay

private val Background = Color(0xFF131313)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainer = Color(0xFF201F1F)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnPrimaryContainer = Color(0xFF295483)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Outline = Color(0xFF8C919A)
private val OutlineVariant = Color(0xFF42474F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageTonesScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var selectedTone by remember { mutableStateOf("Midnight Velocity") }
    var previewTone by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Auto-hide preview after 3 seconds
    LaunchedEffect(previewTone) {
        if (previewTone != null) {
            delay(3000)
            previewTone = null
        }
    }

    val defaultTones = listOf(
        Tone("Midnight Velocity", "Default System Tone"),
        Tone("Sonic Boom", "Punchy & Fast"),
        Tone("Echo Wave", "Minimalist Sine")
    )
    val customTones = listOf(
        Tone("Neon Pulse.mp3", "Uploaded Oct 12"),
        Tone("Ghost Synth.wav", "Uploaded Sep 28")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Background,
            topBar = {
                TopAppBar(
                    title = { Text("Message Tones", color = Primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary)
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 120.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    "Message Tones",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 24.sp
                    ),
                    color = Primary
                )

                Spacer(Modifier.height(24.dp))

                // Default Section
                Text(
                    "DEFAULT",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    ),
                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainer
                ) {
                    Column {
                        defaultTones.forEachIndexed { index, tone ->
                            ToneRow(
                                tone = tone,
                                isSelected = selectedTone == tone.name,
                                onClick = {
                                    selectedTone = tone.name
                                    previewTone = tone.name
                                },
                                showDivider = index < defaultTones.size - 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Custom Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CUSTOM",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 14.sp
                        ),
                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                    TextButton(onClick = {}) {
                        Icon(Icons.Filled.Add, null, tint = PrimaryContainer, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add New", color = PrimaryContainer, fontWeight = FontWeight.W500, fontSize = 14.sp)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainer
                ) {
                    Column {
                        customTones.forEachIndexed { index, tone ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTone = tone.name
                                        previewTone = tone.name
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.MusicNote,
                                        null,
                                        tint = Primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            tone.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.W500,
                                                fontSize = 16.sp
                                            ),
                                            color = OnSurface
                                        )
                                        Text(
                                            tone.description,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.W500,
                                                fontSize = 11.sp
                                            ),
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = selectedTone == tone.name,
                                    onClick = {
                                        selectedTone = tone.name
                                        previewTone = tone.name
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PrimaryContainer,
                                        unselectedColor = Outline
                                    )
                                )
                            }
                            if (index < customTones.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = OutlineVariant.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }

        // Preview Panel
        AnimatedVisibility(
            visible = previewTone != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 96.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xCC1E1F20),
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
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.GraphicEq,
                                null,
                                tint = OnPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                "PREVIEWING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 11.sp,
                                    letterSpacing = 2.sp
                                ),
                                color = Primary
                            )
                            Text(
                                previewTone ?: "",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 16.sp
                                ),
                                color = OnSurface
                            )
                        }
                    }
                    IconButton(onClick = { previewTone = null }) {
                        Icon(Icons.Filled.Close, null, tint = OnSurfaceVariant)
                    }
                }
            }
        }
    }
}

private data class Tone(val name: String, val description: String)

@Composable
private fun ToneRow(
    tone: Tone,
    isSelected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    tone.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp
                    ),
                    color = OnSurface
                )
                Text(
                    tone.description,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 11.sp
                    ),
                    color = OnSurfaceVariant
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = PrimaryContainer,
                    unselectedColor = Outline
                )
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = OutlineVariant.copy(alpha = 0.2f)
            )
        }
    }
}
