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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Background = Color(0xFF131313)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainer = Color(0xFF201F1F)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)
private val Outline = Color(0xFF8C919A)
private val OutlineVariant = Color(0xFF42474F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastSeenScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var lastSeenOption by remember { mutableStateOf(0) }
    var onlineOption by remember { mutableStateOf(0) }

    val lastSeenOptions = listOf("Everyone", "My contacts", "My contacts except...", "Nobody")
    val onlineOptions = listOf("Everyone", "Same as last seen")

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Last seen & online",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 22.sp
                        ),
                        color = OnSurface
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceContainerLow)
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
            Spacer(Modifier.height(24.dp))

            // Last Seen Section
            Text(
                "Who can see my last seen",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp
                ),
                color = Primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainer
            ) {
                Column {
                    lastSeenOptions.forEachIndexed { index, option ->
                        RadioOption(
                            text = option,
                            selected = lastSeenOption == index,
                            onClick = { lastSeenOption = index },
                            showDivider = index < lastSeenOptions.size - 1
                        )
                    }
                }
            }

            Text(
                "If you don't share your Last Seen, you won't be able to see the Last Seen of other people.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 11.sp
                ),
                color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Online Section
            Text(
                "Who can see when I'm online",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp
                ),
                color = Primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceContainer
            ) {
                Column {
                    onlineOptions.forEachIndexed { index, option ->
                        RadioOption(
                            text = option,
                            selected = onlineOption == index,
                            onClick = { onlineOption = index },
                            showDivider = index < onlineOptions.size - 1
                        )
                    }
                }
            }

            Text(
                "Online status allows your contacts to see when you are currently active on Midnight Velocity.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 11.sp
                ),
                color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Privacy First Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, SurfaceContainer)
                            )
                        )
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Icon(
                        Icons.Filled.LockPerson,
                        contentDescription = null,
                        tint = PrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Privacy First",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp
                        ),
                        color = OnSurface
                    )
                    Text(
                        "Your connectivity is under your absolute control.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 16.sp
                ),
                color = OnSurface
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) Modifier.border(2.dp, PrimaryContainer, CircleShape)
                        else Modifier.border(2.dp, Outline, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                    )
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = OutlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp
            )
        }
    }
}
