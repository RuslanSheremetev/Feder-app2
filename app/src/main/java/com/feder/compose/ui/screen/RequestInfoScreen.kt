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
private val SurfaceVariant = Color(0xFF353534)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val PrimaryFixedDim = Color(0xFFA1C9FF)
private val OnPrimaryContainer = Color(0xFF295483)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Outline = Color(0xFF8C919A)
private val OutlineVariant = Color(0xFF42474F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestInfoScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var isRequested by remember { mutableStateOf(false) }
    var requestDate by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Request Account Info",
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Hero Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(SecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Assessment,
                        contentDescription = null,
                        tint = OnSecondaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Request account info",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 24.sp
                    ),
                    color = OnSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Create a report of your account settings and information to download at any time.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x991E1F20))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = PrimaryFixedDim,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Your report will be ready in about",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = OnSurface
                        )
                        Text(
                            "3 days",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryFixedDim
                        )
                        Text(
                            ". You'll have a few weeks to download it after it's available.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = OnSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Requesting a report won't delete any of your data.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Request Button
            Button(
                onClick = {
                    if (!isRequested) {
                        isRequested = true
                        val now = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date())
                        requestDate = now
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainer,
                    contentColor = OnPrimaryContainer,
                    disabledContainerColor = PrimaryContainer.copy(alpha = 0.5f),
                    disabledContentColor = OnPrimaryContainer.copy(alpha = 0.5f)
                ),
                enabled = !isRequested
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (isRequested) Icons.Filled.Check else Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        if (isRequested) "Report Requested" else "Request report",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Reports
            Text(
                "REPORTS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                ),
                color = Primary
            )

            Spacer(Modifier.height(16.dp))

            if (isRequested) {
                // Pending Report Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x991E1F20))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.PendingActions,
                                    contentDescription = null,
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Account Data Report",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.W500,
                                        fontSize = 16.sp
                                    ),
                                    color = OnSurface
                                )
                                Text(
                                    "Requested on $requestDate",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.W500,
                                        fontSize = 11.sp
                                    ),
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SecondaryContainer
                        ) {
                            Text(
                                "Pending",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 11.sp
                                ),
                                color = OnSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, OutlineVariant, RoundedCornerShape(12.dp))
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = Outline,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No reports requested yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
