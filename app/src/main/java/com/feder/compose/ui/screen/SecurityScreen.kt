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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Background = Color(0xFF131313)
private val Surface = Color(0xFF131313)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val PrimaryFixedDim = Color(0xFFA1C9FF)
private val OnPrimaryContainer = Color(0xFF295483)
private val OnPrimaryFixed = Color(0xFF001C38)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Outline = Color(0xFF8C919A)
private val OutlineVariant = Color(0xFF42474F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Security",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 22.sp
                        ),
                        color = OnSurface
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
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
            // Header Illustration
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shield Icon in circle
                Box(
                    modifier = Modifier
                        .size(192.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryContainer.copy(alpha = 0.2f), Color.Transparent)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .background(PrimaryFixedDim)
                            .padding(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = OnPrimaryFixed,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Security",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 24.sp
                    ),
                    color = OnSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Your messages and calls are secured with end-to-end encryption. Only you and the person you're communicating with can read or listen to them.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Encryption Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0x4D1E1F20))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = PrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "End-to-end encryption",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.W500,
                                fontSize = 16.sp
                            ),
                            color = Primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Midnight Velocity cannot read your messages or listen to your calls because they are encrypted from start to finish.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Privacy & Notifications
            Text(
                "PRIVACY & NOTIFICATIONS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                ),
                color = Primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLow
            ) {
                Column {
                    // Toggle: Security notifications
                    var enabled by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                "Security notifications",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 16.sp
                                ),
                                color = OnSurface
                            )
                            Text(
                                "Get notified when your security code changes for a contact in an end-to-end encrypted chat.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00325B),
                                checkedTrackColor = PrimaryContainer,
                                uncheckedThumbColor = OnSurface,
                                uncheckedTrackColor = Outline
                            )
                        )
                    }

                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = OutlineVariant.copy(alpha = 0.3f)
                    )

                    // Link: Show security notifications
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Show security notifications on this phone",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 16.sp
                                ),
                                color = OnSurface
                            )
                            Text(
                                "Learn more about how encryption works on your device.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = OnSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.OpenInNew,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Visual Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCD1Sq0Mm_94CU-N3Zc2Kb9owYQqdOjaHslZWEfvKdiBvZNxidO6W06uZGkH4Ul4jGbQoqPiiSVqODcUAS0-Px-aINy2qDL4V3XEYuvAf1QC-bEO8rR8wytKUi2BJH5UmJI3zjzg3NoDYHMeB53LHh8WrZOBIwOaX3UhCmJ56Wy4Zhu6R5n0VrjlVbpTcM3TssfGAFrYir-bfD1Qyj6L-l96FU3Q7iWUBrElegn6qUWdD_wEUuTciXoBt2zFhX8g2okKOewhEzSmHg",
                    contentDescription = "Encryption",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Background)
                            )
                        )
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp),
                    shape = RoundedCornerShape(50),
                    color = PrimaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        "ENCRYPTION PROTOCOL v4.2",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 11.sp
                        ),
                        color = OnPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
