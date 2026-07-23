package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
private val SurfaceVariant = Color(0xFF353534)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    var showLastSeen by remember { mutableStateOf(false) }
    var showProfilePhoto by remember { mutableStateOf(false) }
    var showBlockedContacts by remember { mutableStateOf(false) }

    if (showBlockedContacts) { BlockedContactsScreen(onBack = { showBlockedContacts = false }); return }
    if (showProfilePhoto) { ProfilePhotoScreen(onBack = { showProfilePhoto = false }); return }
    if (showLastSeen) {
        LastSeenScreen(onBack = { showLastSeen = false })
        return
    }
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy",
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
                .padding(bottom = 100.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Header description
            Text(
                "Control who can see your personal info and which messages you receive.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, letterSpacing = 0.25.sp),
                color = OnSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Personal Info Section
            SectionHeader("Who can see my personal info")
            SettingsGroup {
                PrivacyItem(
                    icon = Icons.Filled.Visibility,
                    title = "Last seen and online",
                    subtitle = "Nobody"
                )
                PrivacyItem(
                    icon = Icons.Filled.AccountCircle,
                    title = "Profile photo",
                    subtitle = "Everyone"
                )
                PrivacyItem(
                    icon = Icons.Filled.AutoAwesome,
                    title = "Status",
                    subtitle = "My contacts"
                )
            }

            Spacer(Modifier.height(24.dp))

            // Disappearing Messages
            SectionHeader("Disappearing Messages")
            SettingsGroup {
                PrivacyItem(
                    icon = Icons.Filled.Timer,
                    title = "Default message timer",
                    subtitle = "Off"
                )
            }

            Spacer(Modifier.height(24.dp))

            // Advanced
            SectionHeader("Advanced")
            SettingsGroup {
                PrivacyItem(
                    icon = Icons.Filled.Block,
                    title = "Blocked contacts",
                    subtitle = "12 contacts"
                )
                FingerprintLockItem()
            }

            Spacer(Modifier.height(24.dp))

            // Encryption Card
            EncryptionCard()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            letterSpacing = 2.sp
        ),
        color = Primary.copy(alpha = 0.8f),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun PrivacyItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp
                    ),
                    color = OnSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        letterSpacing = 0.25.sp
                    ),
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FingerprintLockItem() {
    var enabled by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Filled.Fingerprint,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    "Fingerprint lock",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp
                    ),
                    color = OnSurface
                )
                Text(
                    if (enabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        letterSpacing = 0.25.sp
                    ),
                    color = OnSurfaceVariant
                )
            }
        }

        Switch(
            checked = enabled,
            onCheckedChange = { enabled = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryContainer,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SurfaceContainerHighest
            )
        )
    }
}

@Composable
private fun EncryptionCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerHigh)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Your privacy is permanent",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp
                ),
                color = Primary
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Messenger secures your conversations with end-to-end encryption. Your personal messages stay between you and the people you choose.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                letterSpacing = 0.25.sp
            ),
            color = OnSurfaceVariant
        )
    }
}
