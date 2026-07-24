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
import androidx.compose.material3.MaterialTheme
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStorageScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    val chats = listOf(
        StorageChat("Julie Smith", "1.2 GB", 0.85f, "https://lh3.googleusercontent.com/aida-public/AB6AXuDejnAbz2QYbVTmcyasG0kWfuEcTogN4DcBNZrV2VOSHOFpTv1FAXF551X4FbU5ufmueoXV3cwp0W13IUKhcWxCq2lVAMXemGVENDynVoiL6EM393o-jKW9OHGj-CapSNNHO0L64iUpMqtqARntflliGLtZmphKCocsRZzVpxmivTAeQjunrFkEDSJNkz8nUIEzsYl2Ga2Z7AxGkch1n-4DNEwWWecSEoqEDGmbVujs8aeUkMECGmAJYHlCUaVz_yl-W-w4SXts-4A"),
        StorageChat("Dev Team Alpha", "840 MB", 0.60f, "https://lh3.googleusercontent.com/aida-public/AB6AXuCVXXePLj79fEzHuK9yxc7mtdSbkOCGeGvPd-wYZTXo_wNIsmAQOYDvg1kaO4H0qRjRSRJGRwJM9XepexcZIkHsMEAN9VJhv5aPPwfZVHMX5Afxc2OG8-QzjEqDYEn9Hv7CeYfLuQVpQ61B6BlPWDJLL9_nwx-Ji1DLkSIaOvTnqP9KVqutGkDKvOH-FX8h_vUaN-7j7cmJYHXz7Hc2dMtbfou-BrZuc0kTqNZBnfX2u_oSVWT-8uqoc3uXgwA5FVp8S1BKsRcCeXQ"),
        StorageChat("Sarah Jenkins", "612 MB", 0.45f, "https://lh3.googleusercontent.com/aida-public/AB6AXuC2W9tJJFtf64VJzbaZQMBZjWwJAg628XAFXTw3hv2cMUzJKRw_t1bKCeF81FSi-iQAEt74Zk_xYGaEMfHOvyG3baa0xPLkJ9b3w_1JIKs8CAkNUdCS56BAC_EUM7_bNQx8xa6v-7R8E5YWMFTyPymx1-Sx-svOjZWbP42coYgVX5S1eilfhE-QzpvoJE995vxGI0yjgbWzPW3-eq5ftq8jXnJ8aw-y9odK9mts-TDdoIwZN6Hj6MPbJdDoXZ58m37ot0-p3GadJfg"),
        StorageChat("Velocity Design Lab", "320 MB", 0.25f, "https://lh3.googleusercontent.com/aida-public/AB6AXuBg3-7wI1br2Z6Ca1Hu7w07GqwEol33HRzXScC-erYDNkm6bVCBiqobDNN87GbH1MU2BDOVsMCV2kgEsQNfqnkvn5wMW5ghrTbQL0odLMAbSiB6lhxUI3vfEvG_2NeEzBKbb5qdssrsOOQwYjQtFnd1T303IGOx3PkQMgwjxQdjgVYZS7xQZS6MWeDzoFEVaPVvndbHaXeRyp6n0sVW0vJIr2hyeipAIEA6HIYEhXu5ZriD4Xz--mCIhe4LUiw9xU17fnyB5BPtcO4")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Manage Storage", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
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

            Text("Manage Storage", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W600, fontSize = 24.sp)
            Text("Free up space by deleting large or unnecessary items.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)

            Spacer(Modifier.height(24.dp))

            // Storage Card
            MaterialTheme.colorScheme.surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0x4D1E1F20),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("USED SPACE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 2.sp)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("42.8", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W600, fontSize = 28.sp)
                                Text(" GB", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, letterSpacing = 2.sp)
                            Text("64 GB", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Storage bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Row(Modifier.fillMaxSize()) {
                            Box(Modifier.weight(0.45f).fillMaxHeight().background(MaterialTheme.colorScheme.primaryContainer))
                            Box(Modifier.weight(0.20f).fillMaxHeight().background(MaterialTheme.colorScheme.secondaryContainer))
                            Box(Modifier.weight(0.10f).fillMaxHeight().background(MaterialTheme.colorScheme.tertiaryContainer))
                            Spacer(Modifier.weight(0.25f))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendDot(MaterialTheme.colorScheme.primaryContainer, "Media (28.8 GB)")
                        LegendDot(MaterialTheme.colorScheme.secondaryContainer, "Files (12.8 GB)")
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LegendDot(MaterialTheme.colorScheme.tertiaryContainer, "Other (1.2 GB)")
                        LegendDot(Color.White.copy(alpha = 0.1f), "Free (21.2 GB)")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Review and delete
            Text("Review and delete items", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReviewCard(
                    icon = Icons.Filled.FilterTiltShift,
                    title = "Larger than 5 MB",
                    size = "1.8 GB",
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ReviewCard(
                    icon = Icons.Filled.Forward10,
                    title = "Forwarded many times",
                    size = "450 MB",
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Chats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Chats", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                Text("Sort by Size", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 14.sp)
            }

            Spacer(Modifier.height(12.dp))

            MaterialTheme.colorScheme.surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0x4D1E1F20),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column {
                    chats.forEachIndexed { index, chat ->
                        ChatStorageRow(chat, showDivider = index < chats.size - 1)
                    }
                }
            }

            TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Show more chats", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.W500, fontSize = 14.sp)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (color == Color.White.copy(alpha = 0.1f))
                        Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    else Modifier
                )
        )
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 14.sp)
    }
}

@Composable
private fun ReviewCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    size: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    MaterialTheme.colorScheme.surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .clickable { },
        color = Color(0x4D1E1F20),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
            }
            Column {
                Text(size, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W600, fontSize = 24.sp)
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.W500, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ChatStorageRow(chat: StorageChat, showDivider: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = chat.avatarUrl,
                contentDescription = chat.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(chat.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W500, fontSize = 16.sp)
                    Text(chat.size, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.W500, fontSize = 16.sp)
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(chat.fillFraction)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}

private data class StorageChat(
    val name: String,
    val size: String,
    val fillFraction: Float,
    val avatarUrl: String
)
