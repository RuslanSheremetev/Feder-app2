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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF131313)
private val Surface = Color(0xFF131313)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val TonalLayer1 = Color(0xFF1E1F20)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnPrimary = Color(0xFF00325B)
private val OnPrimaryContainer = Color(0xFF295483)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    var showTerms by remember { mutableStateOf(false) }
    var showChatWithUs by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    if (showChatWithUs) { ChatWithUsScreen(onBack = { showChatWithUs = false }); return }
    if (showTerms) {
        TermsScreen(onBack = { showTerms = false })
        return
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Help", color = Primary, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.8f))
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

            Text("How can we help?", color = OnSurface, fontWeight = FontWeight.W600, fontSize = 28.sp)

            Spacer(Modifier.height(16.dp))

            // Search
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search for help topics...", color = OnSurfaceVariant) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = OnSurfaceVariant) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainer,
                    unfocusedContainerColor = SurfaceContainer,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(24.dp))

            // Getting Started
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { },
                color = TonalLayer1,
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
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.RocketLaunch, null, tint = OnPrimaryContainer, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Getting Started", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                            Text("Setup your first chat room", color = OnSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Grid cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HelpGridCard(Icons.Filled.LockPerson, "Privacy & Security", Modifier.weight(1f))
                HelpGridCard(Icons.Filled.AccountCircle, "Account Management", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HelpGridCard(Icons.Filled.Build, "Troubleshooting", Modifier.weight(1f))
                HelpGridCard(Icons.Filled.Payments, "Premium Billing", Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // FAQs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Frequently Asked Questions", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                TextButton(onClick = {}) { Text("View all", color = Primary, fontWeight = FontWeight.W500, fontSize = 14.sp) }
            }

            Spacer(Modifier.height(8.dp))

            FaqItem("How to restore deleted messages?")
            FaqItem("Managing group notification settings")
            FaqItem("Using end-to-end encryption")

            Spacer(Modifier.height(24.dp))

            // Contact Support
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SecondaryContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Still need help?", color = OnSecondaryContainer, fontWeight = FontWeight.W500, fontSize = 22.sp)
                    Text(
                        "Our support team is available 24/7 to assist you with any technical issues.",
                        color = OnSecondaryContainer.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Chat with us", fontWeight = FontWeight.W500, fontSize = 14.sp)
                        }
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnSecondaryContainer.copy(alpha = 0.3f))
                        ) {
                            Text("Email Support", color = OnSecondaryContainer, fontWeight = FontWeight.W500, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Legal
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showTerms = true },
                color = SurfaceContainerLow,
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
                        Icon(Icons.Filled.Policy, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Terms and Privacy Policy", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                            Text("Review our latest guidelines", color = OnSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpGridCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { },
        color = TonalLayer1,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = OnSurface, fontWeight = FontWeight.W500, fontSize = 14.sp)
        }
    }
}

@Composable
private fun FaqItem(question: String) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        color = if (expanded) SurfaceContainer else SurfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(question, color = OnSurface, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null,
                    tint = if (expanded) Primary else OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Detailed answer goes here with step-by-step instructions.",
                    color = OnSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}
