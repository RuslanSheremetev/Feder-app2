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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF131313)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainer = Color(0xFF201F1F)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val PrimaryFixedDim = Color(0xFFA1C9FF)
private val OnPrimaryContainer = Color(0xFF295483)
private val Secondary = Color(0xFFB5C8E2)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Outline = Color(0xFF8C919A)
private val SurfaceVariant = Color(0xFF353534)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var selectedOption by remember { mutableStateOf(0) }
    val options = listOf("My contacts", "My contacts except...", "Only share with...")
    val icons = listOf(Icons.Filled.Contacts, Icons.Filled.PersonRemove, Icons.Filled.Favorite)

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Status", color = Primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceContainerLow.copy(alpha = 0.8f))
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
                color = OnSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Changes to your privacy settings will apply to status updates that you post from now on.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = OnSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Radio options
            options.forEachIndexed { index, option ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedOption = index },
                    color = SurfaceContainer,
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
                                    .background(SecondaryContainer.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icons[index],
                                    contentDescription = null,
                                    tint = Secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                option,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 16.sp
                                ),
                                color = OnSurface
                            )
                        }
                        RadioButton(
                            selected = selectedOption == index,
                            onClick = { selectedOption = index },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryContainer,
                                unselectedColor = Outline
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 24 Hour Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = PrimaryContainer.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = PrimaryFixedDim,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Status updates will disappear after 24 hours. Your current status updates will remain visible until they expire.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = OnPrimaryContainer
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
                    color = OnSurface
                )
                Text(
                    "CONNECT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    ),
                    color = Primary
                )
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { },
                color = SurfaceContainer,
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
                                color = OnSurface
                            )
                            Text(
                                "Tap to share status to Facebook",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
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
                                colors = listOf(SurfaceVariant, Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PrivacyTip,
                        contentDescription = null,
                        tint = OnSurfaceVariant.copy(alpha = 0.4f),
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
                    color = OnSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
