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
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val PrimaryFixedDim = Color(0xFFA1C9FF)
private val Secondary = Color(0xFFB5C8E2)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Outline = Color(0xFF8C919A)
private val OutlineVariant = Color(0xFF42474F)
private val Error = Color(0xFFFFB4AB)
private val ErrorContainer = Color(0xFF93000A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoStepScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Two-step verification",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface.copy(alpha = 0.8f)
                )
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

            // Status Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x4D1E1F20))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Shield icon with pulse
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = PrimaryFixedDim,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Two-step verification",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 24.sp
                        ),
                        color = OnSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SecondaryContainer.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer)
                            )
                            Text(
                                "Status: On",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                ),
                                color = Secondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Why it matters card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Why it matters",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.W500,
                                fontSize = 16.sp
                            ),
                            color = Primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Two-step verification adds a layer of security by requiring a PIN when registering your phone number with Midnight Velocity again. This prevents unauthorized access even if your SIM card is stolen.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Configuration
            Text(
                "Configuration",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                ),
                color = Outline,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            // Disable
            TwoStepAction(
                icon = Icons.Filled.Cancel,
                title = "Disable",
                iconTint = Error,
                iconBg = ErrorContainer.copy(alpha = 0.2f)
            )

            // Change PIN
            TwoStepAction(
                icon = Icons.Filled.Password,
                title = "Change PIN",
                iconTint = PrimaryFixedDim,
                iconBg = PrimaryContainer.copy(alpha = 0.1f)
            )

            // Change Email
            TwoStepAction(
                icon = Icons.Filled.AlternateEmail,
                title = "Change email address",
                iconTint = PrimaryFixedDim,
                iconBg = PrimaryContainer.copy(alpha = 0.1f)
            )

            Spacer(Modifier.height(24.dp))

            // Bottom illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = Color(0xFF353534),
                        modifier = Modifier.size(64.dp)
                    )
                    Icon(
                        Icons.Filled.Encrypted,
                        contentDescription = null,
                        tint = Color(0xFF353534),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TwoStepAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconTint: Color,
    iconBg: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x4D1E1F20))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable { }
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp
                ),
                color = OnSurface
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
