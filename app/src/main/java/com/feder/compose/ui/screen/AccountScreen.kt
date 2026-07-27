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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onBack: () -> Unit) {
    var showSecurity by remember { mutableStateOf(false) }
    var showName by remember { mutableStateOf(false) }
    var showTwoStep by remember { mutableStateOf(false) }
    var showChangeNumber by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }

    if (showName) { NameScreen(onBack = { showName = false }); return }
    if (showSecurity) { SecurityScreen(onBack = { showSecurity = false }); return }
    if (showTwoStep) { TwoStepScreen(onBack = { showTwoStep = false }); return }
    if (showChangeNumber) { ChangeNumberScreen(onBack = { showChangeNumber = false }); return }
    if (showDeleteAccount) { DeleteAccountScreen(onBack = { showDeleteAccount = false }); return }

    Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Account", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 22.sp)
            }
            Spacer(Modifier.height(16.dp))
            
            OptionsList(
                onName = { showName = true },
                onSecurity = { showSecurity = true },
                onChangeNumber = { showChangeNumber = true },
                onTwoStep = { showTwoStep = true },
                onDeleteAccount = { showDeleteAccount = true }
            )
            Spacer(Modifier.height(24.dp))
            
    }
}

@Composable
private fun OptionsList(
    onName: () -> Unit,
    onSecurity: () -> Unit,
    onChangeNumber: () -> Unit,
    onTwoStep: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SettingsOption(Icons.Filled.Person, "Name", "Name, username, bio", onName)
        SettingsOption(Icons.Filled.Security, "Security", "Security notifications and encryption", onSecurity)
        SettingsOption(Icons.Filled.PhonelinkSetup, "Change Number", "Migrate account info & groups", onChangeNumber)
        TwoStepOption(onTwoStep)
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
            Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
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
            Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.VerifiedUser, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(24.dp))
            }
            Column {
                Text("Two-step verification", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text("Extra layer of security", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("On", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 14.sp)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
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
            Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
            }
            Column {
                Text("Delete account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text("Permanently erase your data", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Lightbulb, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("PRIVACY TIP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 14.sp, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Keep your primary phone number updated to ensure you never lose access to your encrypted chat history and media backups.",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        }
    }
}
