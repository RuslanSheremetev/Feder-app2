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
private val OnPrimaryContainer = Color(0xFF295483)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onBack: () -> Unit) {
    var showSecurity by remember { mutableStateOf(false) }
    var showTwoStep by remember { mutableStateOf(false) }
    var showChangeNumber by remember { mutableStateOf(false) }
    var showRequestInfo by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }

    if (showSecurity) { SecurityScreen(onBack = { showSecurity = false }); return }
    if (showTwoStep) { TwoStepScreen(onBack = { showTwoStep = false }); return }
    if (showChangeNumber) { ChangeNumberScreen(onBack = { showChangeNumber = false }); return }
    if (showRequestInfo) { RequestInfoScreen(onBack = { showRequestInfo = false }); return }
    if (showDeleteAccount) { DeleteAccountScreen(onBack = { showDeleteAccount = false }); return }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = { Text("Account", color = Primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            HeroSection()
            Spacer(Modifier.height(24.dp))
            OptionsList(
                onSecurity = { showSecurity = true },
                onChangeNumber = { showChangeNumber = true },
                onTwoStep = { showTwoStep = true },
                onRequestInfo = { showRequestInfo = true },
                onDeleteAccount = { showDeleteAccount = true }
            )
            Spacer(Modifier.height(24.dp))
            PrivacyTipCard()
            Spacer(Modifier.height(32.dp))
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
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = "https://via.placeholder.com/80",
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, PrimaryFixedDim, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier.size(24.dp).clip(CircleShape).background(PrimaryFixedDim).border(2.dp, Surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Verified, null, tint = OnPrimaryContainer, modifier = Modifier.size(16.dp))
                    }
                }
                Column {
                    Text("Alex Rivera", color = OnSurface, fontWeight = FontWeight.W600, fontSize = 24.sp)
                    Text("+1 (555) 012-3456", color = OnSurfaceVariant, fontSize = 14.sp)
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
                Icon(Icons.Filled.CloudDone, null, tint = PrimaryFixedDim, modifier = Modifier.size(36.dp))
                Text("Cloud Sync", color = OnSurfaceVariant, fontSize = 14.sp)
                Text("Connected", color = Primary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun OptionsList(
    onSecurity: () -> Unit,
    onChangeNumber: () -> Unit,
    onTwoStep: () -> Unit,
    onRequestInfo: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SettingsOption(Icons.Filled.Security, "Security", "Security notifications and encryption", onSecurity)
        SettingsOption(Icons.Filled.PhonelinkSetup, "Change Number", "Migrate account info & groups", onChangeNumber)
        TwoStepOption(onTwoStep)
        SettingsOption(Icons.Filled.Description, "Request account info", "Download your account report", onRequestInfo)
        HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), color = OutlineVariant)
        DeleteOption(onDeleteAccount)
    }
}

@Composable
private fun SettingsOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(SecondaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = OnSecondaryContainer, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(title, color = OnSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text(subtitle, color = OnSurfaceVariant, fontSize = 14.sp)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Outline, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TwoStepOption(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(SecondaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.VerifiedUser, null, tint = OnSecondaryContainer, modifier = Modifier.size(24.dp))
            }
            Column {
                Text("Two-step verification", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text("Extra layer of security", color = OnSurfaceVariant, fontSize = 14.sp)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("On", color = Primary, fontWeight = FontWeight.W500, fontSize = 14.sp)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Outline, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun DeleteOption(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(ErrorContainer.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Delete, null, tint = Error, modifier = Modifier.size(24.dp))
            }
            Column {
                Text("Delete account", color = Error, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text("Permanently erase your data", color = OnSurfaceVariant, fontSize = 14.sp)
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
            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Lightbulb, null, tint = Primary, modifier = Modifier.size(20.dp))
                Text("PRIVACY TIP", color = Primary, fontWeight = FontWeight.W500, fontSize = 14.sp, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Keep your primary phone number updated to ensure you never lose access to your encrypted chat history and media backups.",
                color = OnSurface,
                fontSize = 14.sp
            )
        }
    }
}
