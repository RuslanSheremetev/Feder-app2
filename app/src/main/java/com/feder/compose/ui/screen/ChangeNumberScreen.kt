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
fun ChangeNumberScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var oldNumber by remember { mutableStateOf("") }
    var newNumber by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Change Number",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 120.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // Hero Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1E1F20))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.SyncAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Info Card
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .height(160.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1E1F20))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(
                                "Migrating your profile",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Changing your phone number will migrate your account info, groups, and settings from your old number to the new one.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Old Number
                BentoCard(
                    label = "Old phone number",
                    value = oldNumber,
                    onValueChange = { oldNumber = it },
                    placeholder = "Phone number"
                )

                Spacer(Modifier.height(12.dp))

                // New Number
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E1F20))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        "New phone number",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Country code
                        Box(
                            modifier = Modifier
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable { }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "+1",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.W500,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        // Input
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            TextField(
                                value = newNumber,
                                onValueChange = { newNumber = it },
                                placeholder = {
                                    Text("Phone number", color = MaterialTheme.colorScheme.outline)
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Info hint
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp).offset(y = 2.dp)
                        )
                        Text(
                            "Make sure your new number can receive SMS or calls to verify the change.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.W500,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Feature Migration Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureCard(Icons.Filled.AccountCircle, "Profile info", Modifier.weight(1f))
                    FeatureCard(Icons.Filled.Groups, "Groups", Modifier.weight(1f))
                    FeatureCard(Icons.Filled.Settings, "Settings", Modifier.weight(1f))
                }

                Spacer(Modifier.height(32.dp))
            }

            // Next button fixed at bottom
            MaterialTheme.colorScheme.surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .background(Color.Transparent)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Next",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.W500,
                                fontSize = 16.sp
                            )
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1E1F20))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.W500,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "+1",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.outline) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1F20))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


