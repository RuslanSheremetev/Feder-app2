package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import kotlinx.coroutines.delay

private val Background = Color(0xFF131313)
private val Surface = Color(0xFF131313)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceVariant = Color(0xFF353534)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val PrimaryFixedDim = Color(0xFFA1C9FF)
private val OnPrimaryContainer = Color(0xFF295483)
private val OnPrimaryFixed = Color(0xFF001C38)
private val Secondary = Color(0xFFB5C8E2)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Error = Color(0xFFFFB4AB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWithUsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var topic by remember { mutableStateOf("Account Access") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }
    val topics = listOf("Account Access", "Billing & Subscription", "Feature Request", "Privacy & Security", "Other")

    LaunchedEffect(isSent) {
        if (isSent) {
            delay(3000)
            isSent = false
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Chat with us", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, null, tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.6f))
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

            Text("How can we help?", color = OnSurface, fontWeight = FontWeight.W600, fontSize = 24.sp)
            Text(
                "Our team is available 24/7 to assist with your technical needs and account questions.",
                color = OnSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(24.dp))

            // Bento Grid
            // Chat with us (Large)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryContainer)
                    .clickable { }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Icon(Icons.Filled.Chat, null, tint = OnPrimaryContainer, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Chat with us", color = OnPrimaryContainer, fontWeight = FontWeight.W500, fontSize = 16.sp)
                    }
                    Text("Average response: 2 mins", color = OnPrimaryContainer.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    tint = OnPrimaryContainer,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(24.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallCard(Icons.Filled.Mail, "Email support", "Response in 24h", Secondary, Modifier.weight(1f))
                SmallCard(Icons.Filled.Report, "Report a problem", "System bugs", Error, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // Submit Ticket Form
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainerLow
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Submit a ticket", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)

                    Spacer(Modifier.height(16.dp))

                    // Topic dropdown
                    Text("Topic", color = Primary, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { expanded = true },
                            color = SurfaceContainer
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(topic, color = OnSurface, fontSize = 14.sp)
                                Icon(Icons.Filled.ExpandMore, null, tint = OnSurfaceVariant)
                            }
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            topics.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t, color = OnSurface) },
                                    onClick = { topic = t; expanded = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Message
                    Text("Message", color = Primary, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        placeholder = { Text("Describe your issue in detail...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainer,
                            unfocusedContainerColor = SurfaceContainer,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Attach
                    OutlinedButton(
                        onClick = {},
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Filled.AttachFile, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                        Text("Add attachment", color = OnSurfaceVariant, fontSize = 11.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Send button
                    Button(
                        onClick = {
                            isSending = true
                            // simulate send
                            isSent = true
                            isSending = false
                            message = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryFixedDim,
                            contentColor = OnPrimaryFixed
                        ),
                        enabled = !isSending
                    ) {
                        if (isSent) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(8.dp))
                            Text("Sent Successfully", fontWeight = FontWeight.W500, fontSize = 16.sp)
                        } else if (isSending) {
                            CircularProgressIndicator(
                                color = OnPrimaryFixed,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send Message", fontWeight = FontWeight.W500, fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Filled.Send, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Quick Links
            Text(
                "QUICK LINKS",
                color = OnSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                letterSpacing = 2.sp
            )
            QuickLink(Icons.Filled.Help, "Browse Help Center")
            QuickLink(Icons.Filled.Security, "Security & Privacy")
            QuickLink(Icons.Filled.VerifiedUser, "Community Guidelines")

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SmallCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { },
        color = SurfaceContainer
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
            Column {
                Text(title, color = OnSurface, fontWeight = FontWeight.W500, fontSize = 14.sp)
                Text(subtitle, color = OnSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun QuickLink(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
            Text(label, color = OnSurface, fontSize = 16.sp)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
    }
    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f))
}
