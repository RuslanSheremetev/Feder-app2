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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Surface = Color(0xFF131313)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val TonalLayer1 = Color(0xFF1E1F20)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryFixedDim = Color(0xFFA1C9FF)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val Outline = Color(0xFF8C919A)
private val OutlineVariant = Color(0xFF42474F)
private val Error = Color(0xFFFFB4AB)
private val ErrorContainer = Color(0xFF93000A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Account",
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
                    containerColor = SurfaceContainerLow
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            HeroSection()
            Spacer(modifier = Modifier.height(24.dp))
            OptionsList()
            Spacer(modifier = Modifier.height(24.dp))
            PrivacyTipCard()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeroSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(2f)
                .height(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TonalLayer1)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuC1j_GFhimmor07r8ZIOvBZwF4uGqqTaLVneoIHPwejAzB8CSHtl35EzdY0gqPo7QaWgtyp0N87TSTcXv4Ucch2apAMC1WPiJz2GPn8GeAzEADMFYFpkqC6My053MfHOknYuZlswij_N0bSzKQiKUZeB9UrRmQ-28yd53S-Cg8bYUQGNYOaG1yozwKOETe7pirEaxwUM5fVlMYwS8oAtSVnsVKUlPfYlAXrwFkNHTZNIZsxvcrfaTSme1_kL3aLs2M5pYrP9Pr5pps",
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryFixedDim, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PrimaryFixedDim)
                            .border(2.dp, Surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF295483),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column {
                    Text(
                        "Alex Rivera",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 24.sp
                        ),
                        color = OnSurface
                    )
                    Text(
                        "+1 (555) 012-3456",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            letterSpacing = 0.25.sp
                        ),
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TonalLayer1)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.CloudDone,
                    contentDescription = "Cloud Sync",
                    tint = PrimaryFixedDim,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Cloud Sync",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp
                    ),
                    color = OnSurfaceVariant
                )
                Text(
                    "Connected",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun OptionsList() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SettingsOption(Icons.Filled.Security, "Security", "Security notifications and encryption")
        SettingsOption(Icons.Filled.PhonelinkSetup, "Change Number", "Migrate account info & groups")
        TwoStepVerificationOption()
        SettingsOption(Icons.Filled.Description, "Request account info", "Download your account report")

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            color = OutlineVariant,
            thickness = 1.dp
        )

        DeleteAccountOption()
    }
}

@Composable
private fun SettingsOption(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = OnSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
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

        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Outline,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TwoStepVerificationOption() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    tint = OnSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    "Two-step verification",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp
                    ),
                    color = OnSurface
                )
                Text(
                    "Extra layer of security",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        letterSpacing = 0.25.sp
                    ),
                    color = OnSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "On",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp
                ),
                color = Primary
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DeleteAccountOption() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ErrorContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    "Delete account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp
                    ),
                    color = Error
                )
                Text(
                    "Permanently erase your data",
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
private fun PrivacyTipCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(TonalLayer1)
            .border(
                1.dp,
                OutlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "PRIVACY TIP",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    ),
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Keep your primary phone number updated to ensure you never lose access to your encrypted chat history and media backups.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    letterSpacing = 0.25.sp
                ),
                color = OnSurface
            )
        }
    }
}
