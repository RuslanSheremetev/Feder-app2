package com.feder.compose.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val Background = Color(0xFF131313)
private val Surface = Color(0xFF131313)
private val SurfaceContainerLow = Color(0xFF1C1B1B)
private val SurfaceContainer = Color(0xFF201F1F)
private val SurfaceContainerHigh = Color(0xFF2A2A2A)
private val Primary = Color(0xFFD2E3FF)
private val PrimaryContainer = Color(0xFFA1C9FF)
private val OnPrimaryContainer = Color(0xFF295483)
private val SecondaryContainer = Color(0xFF36485E)
private val OnSecondaryContainer = Color(0xFFA4B7D0)
private val OnSurface = Color(0xFFE5E2E1)
private val OnSurfaceVariant = Color(0xFFC2C6D0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    var showTerms by remember { mutableStateOf(false) }

    if (showTerms) {
        TermsScreen(onBack = { showTerms = false })
        return
    }
    val scrollState = rememberScrollState()
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Help",
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
            Spacer(Modifier.height(8.dp))

            // Search bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = {
                    Text("Search help articles", color = OnSurfaceVariant)
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, null, tint = OnSurfaceVariant)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainer,
                    unfocusedContainerColor = SurfaceContainer,
                    focusedBorderColor = PrimaryContainer,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            // Hero Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Help Center Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(192.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimaryContainer)
                        .clickable(onClick = onClick)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Help Center",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.W600,
                                    fontSize = 24.sp
                                ),
                                color = OnPrimaryContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Find answers and tutorials",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = OnPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            null,
                            tint = OnPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Contact Us Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(192.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SecondaryContainer)
                        .clickable(onClick = onClick)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Contact us",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.W600,
                                    fontSize = 24.sp
                                ),
                                color = OnSecondaryContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Message our support team",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = OnSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            Icons.Filled.SupportAgent,
                            null,
                            tint = OnSecondaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Legal & Information
            Text(
                "LEGAL & INFORMATION",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                ),
                color = Primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            HelpItem(Icons.Filled.Policy, "Terms and Privacy Policy", "Review our latest guidelines", onClick = { showTerms = true })
            HelpItem(Icons.Filled.Info, "App info", "Version 4.22.1 (Deep Dark)")
            HelpItem(Icons.Filled.BugReport, "Report a bug", "Help us improve the experience")

            Spacer(Modifier.height(24.dp))

            // Atmospheric Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDAE7xfqO1Wzg2zu9i2L6aP8yu-ZAn94ACV9c26aSzr61-vqLoaVmxXcrsL08Tt_E3GbPKn7wzs6CRyyGUOAWx--VD9yu1pBfLrPpbRldG87RV9oVnZss6EWPLph-JaBldPOF7_j9_-MA2FUuJSkY4EJJK9L6A8sGGIh2vpdHWPYdoaN_IND_XGYdGsFDg9L5qX6AEdEqsblLYJYdsAyyQkvxv_LZMxwbMIubGJndb_G2ex1g546msx1-KxauOKeRMnkK4-po8Y_gk",
                    contentDescription = "Support",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Background)
                            )
                        )
                )
                Text(
                    "Secure & Encrypted Support",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 11.sp
                    ),
                    color = OnSurfaceVariant,
                    modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
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
            null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
