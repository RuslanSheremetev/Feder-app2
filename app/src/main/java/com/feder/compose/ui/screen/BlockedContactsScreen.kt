package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Background = Color(0xFF131313)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val SurfaceVariant = Color(0xFF353534)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnPrimaryContainer = Color(0xFF295483)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Outline = Color(0xFF8C919A)
private val OutlineVariant = Color(0xFF42474F)
private val Error = Color(0xFFFFB4AB)

data class BlockedContact(
    val name: String,
    val phone: String,
    val avatarUrl: String? = null,
    val hasAvatar: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedContactsScreen(onBack: () -> Unit) {
    var contacts by remember {
        mutableStateOf(
            listOf(
                BlockedContact("Marcus Holloway", "+1 (555) 012-3456", "https://lh3.googleusercontent.com/aida-public/AB6AXuAOBYCgHgvTP7TjpZUE31gCywPgowmOQN4_3YEQqXkx7pryRE6CVfOsPWL0FzQ9kEgQc3l-SMD6CwPCkJLoGDKOlgNc_jPW3APDID8uaH-6atIyGthq2rTbMODpUzkaWykob1hKyr-RYGUvshTRUowDxv5xC9D2I3GJ3sBy8ERUsFOdtQWW-W4Uqe87EltkqEXbvwJChEdNe1PbL-c3UG8M0_jU5ou4cgOsfhvFODMlncW4v9cf9rzyw8XfD9owI0m7Oaw5_l52t78"),
                BlockedContact("Elara Vance", "+44 7700 900123", "https://lh3.googleusercontent.com/aida-public/AB6AXuCQJI-QVSWs8Dp0QWtaiK9vP8RiKMl93AQi1isTJ0gMQ2OfFWSvhPhk4HSBb08eJ-9GSA1jo6d-ukPoLcootUaQVGfBr2XEAaiGPBhzSQprblXTYkAkqf2XCU1qUq1nzssNljE4nBUXZJw7eLD7oqeWyPtbQ5dIaoPF1PJGZptWdlLhtmub4gDTqPkdh8mJtzfY_j0pR29loKLav1oqPW4_P-QMGBtwuJt0VsRWKoJrnrjaCLm-BXKOfYuzxoVhblMZP0Kynm0zs24"),
                BlockedContact("Arthur Sterling", "+1 (555) 987-6543", "https://lh3.googleusercontent.com/aida-public/AB6AXuAUnJLlq3itvRRRX2YzHTITlaBdKD6fB-Zj985pJP5lKJL2s0tPnPLiDbBvdYzgCtT7lHpXsmNnbXMMqg92anGtGERdiLbzZaQPkuqBnH-Ckc36En5ZkNH4cArXFO1EXzoJDmXF47XgNaV77ipIcTAta03Xs5CsMERa4siI3NTTp6vl3AUwTiLwJne9VRWRR_yZSG06q_EGF7RIyk-DqpGZOkMQfkHaErmErpeDsU4e1oHe9TKCZpfyC5NpmmukrKlkGvlZlC68uHQ"),
                BlockedContact("+1 (555) 443-2210", "No Name Provided", null, false),
                BlockedContact("Sienna West", "+61 412 345 678", "https://lh3.googleusercontent.com/aida-public/AB6AXuC3e7V1mrqXS-xC51tuYEbU1_5BkufqQKIw3BuJjUivt0OnOWsb6ywDg_-HkG5NSCrkRQ0FVGu7Hy2ha_QDbee9lx7aOYFeTGFyXjL1VUHUc8qy2LC2R4wD3bw485-cl_Tf0Lf8UdUG7Gk_s7XeUdb6y6BJgP4TpelZAz3jKfhf7kTO8qnci8yxR_IsUJzd4wo8xv6hXs9A7JVMKB8jsIvt_Ot6W1_ARfxV5ZuhGWUIZcggc7Ad96-e6I8iCqx3lLcwg7DEWydskXE")
            )
        )
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Blocked Contacts", color = OnSurface, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.PersonAdd, "Add", tint = OnPrimaryContainer, modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                            .padding(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceContainerLow.copy(alpha = 0.8f))
            )
        }
    ) { paddingValues ->
        if (contacts.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Block, null, tint = OnSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("No blocked contacts", color = OnSurfaceVariant, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text("Users you block will appear here.", color = OnSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Spacer(Modifier.height(16.dp))

                Text(
                    "Blocked Contacts",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W600, fontSize = 24.sp),
                    color = Primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Info card
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(4.dp, Primary)
                ) {
                    Text(
                        "Blocked contacts will no longer be able to call you or send you messages.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(contacts, key = { it.phone }) { contact ->
                        BlockedContactRow(
                            contact = contact,
                            onUnblock = {
                                contacts = contacts.filter { it.phone != contact.phone }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedContactRow(contact: BlockedContact, onUnblock: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (contact.hasAvatar && contact.avatarUrl != null) {
                        AsyncImage(
                            model = contact.avatarUrl,
                            contentDescription = contact.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(SecondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null, tint = OnSecondaryContainer, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                Column {
                    Text(
                        contact.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500, fontSize = 16.sp),
                        color = OnSurface
                    )
                    Text(
                        contact.phone,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.W500, fontSize = 11.sp),
                        color = if (contact.phone == "No Name Provided") Error else OnSurfaceVariant
                    )
                }
            }

            // Unblock button
            OutlinedButton(
                onClick = onUnblock,
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Text("Unblock", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W500, fontSize = 14.sp))
            }
        }
        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f), thickness = 1.dp)
    }
}
