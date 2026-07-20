package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feder.compose.ui.theme.*

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 96.dp)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(96.dp)) {
                Box(Modifier.size(96.dp).clip(CircleShape).background(SurfaceContainerLow).border(2.dp, PrimaryContainer, CircleShape).padding(4.dp)) {
                    Box(Modifier.size(88.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, "avatar", tint = Primary, modifier = Modifier.size(48.dp))
                    }
                }
                Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF41B35D)).border(4.dp, Background, CircleShape).align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("Alex Thompson", color = OnSurface, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text("Active now", color = Secondary, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Surface(modifier = Modifier.clickable { }, shape = RoundedCornerShape(20.dp), color = Primary) {
                Text("Edit Profile", color = OnPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(32.dp))
        SettingsCard(Icons.Filled.Person, "Account", "Security, change number")
        SettingsCard(Icons.Filled.Lock, "Privacy", "Blocked contacts, status")
        SettingsCard(Icons.Filled.Notifications, "Notifications", "Message, group & call tones")
        SettingsCard(Icons.Filled.DataUsage, "Data and Storage", "Network usage, auto-download")
        var isDarkMode by remember { mutableStateOf(true) }
        Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.DarkMode, "dark", tint = Primary, modifier = Modifier.size(24.dp)) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) { Text("Appearance", color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text("Toggle dark mode", color = Secondary, fontSize = 11.sp) }
                Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary, uncheckedThumbColor = Color.White, uncheckedTrackColor = SurfaceContainerHighest))
            }
        }
        SettingsCard(Icons.Filled.Help, "Help", "Help center, contact us")
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth().clickable { }, shape = RoundedCornerShape(12.dp), color = Color.Transparent, border = BorderStroke(1.dp, Error.copy(alpha = 0.2f))) {
            Text("Log Out", color = Error, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun SettingsCard(icon: ImageVector, title: String, subtitle: String) {
    Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { }, shape = RoundedCornerShape(12.dp), color = SurfaceContainerLow) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(icon, title, tint = Primary, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) { Text(title, color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Secondary, fontSize = 11.sp) }
            Icon(Icons.Filled.ChevronRight, "next", tint = OutlineVariant, modifier = Modifier.size(20.dp))
        }
    }
}
