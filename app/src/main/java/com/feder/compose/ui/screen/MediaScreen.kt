package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

data class MediaGroup(val title: String, val items: List<MediaItem>, val showSelect: Boolean = false)
data class MediaItem(val url: String, val isVideo: Boolean = false, val duration: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Media", "Docs", "Links")
    
    val todayItems = listOf(
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuDCZKNOP-jvz-5LemOzciJN0CQd90MLqFqayoDmhYkJl_qOGGJqqOl_0IsOf1ltY1v-nV6hqLHojh_CsmSf-g_QSP1ZOYA5yZr1nFya5QSm-3CiADABJa5YR6tiE62YzWsvYz1NEOtgG1ffTArP3I9Zqjf4NpPITqtN7eSsnNL96yzIqssaR87qd51LpksLHN_1oHF5WKXgFnWF6SetiRdm8wCcKXo9tT6m6rDtsDdWtxXuYDrmRFRTzA"),
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuASUDlcP3_NthMuWGxWZEODDWtecohAZqi0CWaMYDH42jDWANOpS8N0_SiO59YYRznDSG-ljaAy51Ii-Im7-s1uwK30Q2p5EsS9Zp13J4NlXl9hlsUJ0b1Z-n5Og_OMuxVQBxj3oaMtIYhfx3DZTHUBOTZMm5slXAgPtx1lyIYyqusLEXw_1W5KSM-8yEKogdjEAKVT3me763ZoR56v67T8V9GI0cKJm0UxtjmEOrN4qEkaL77vRCYUHA", isVideo = true),
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuA5PoWPeuvf82PcSnq5w_cpPS9LPJGGMLQ3LS5A5R8iRQnuI92mRNZICtboIxqLFHJwFZ3Tvz8xNOfkblK1CqOkho7UIFDbmz5tcwxpt78LpJdrdZSC2o7N6LthQAbHRQq9opJtl1hIsHTDmSMEmWEPbLdwMaZUlzQ_qXR6NQbVTle1sdzcxF1xlm1WwD1JNpv_-zvXvCVT_Xr9k5UqnJtNgVFKaceUS0OQHq5BDehYEwF9eCo0pP8w2g")
    )
    
    val yesterdayItems = listOf(
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuDAPOVVBxgMf27RUwu9p-z-LV0yxhYqkL2NxQgiRyUCNthWdPLDYkO2m6gmnCkb5mIKMojvO4PwjBpLVsKN6gWtd8dbBjhI8eb-jmLUYCbDhwg_Kd8SnWh3BnFAGfHHJlr_i3_Djqr53Rpn-U45RjQgFJlvUUxNERY5XWQuhFoMb2ZQZ_d2dRJOcbdfX6SAMpK0TYnxUbx0TZOB-uE7cvOx7t6ER4bExSYzIak741QpSMul8jF9Npihaw"),
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuDckdeoVCgyoGBulcp9s6V_53u3f7PC6Jy-uKYyz01HuTjRBXt5FyCpj3zt87WQjOxKvzgucp0XQBlHSkHQxIfkbdTpeV_62FwDVXky3YQpEYvcQBTflGHLxIKiO87l5dibq81Xl9SBXrTAUfxQAQAFInFbo64AF-SdabH5oO8YGY_8GoeJJgW9e_iYlnHPmNWqrPPMmiyOgguiXH0sTSp1OqhQVwCTRCO7y_lpwr2IYZ06Kkh2pXMl2Q"),
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuBOYNIISOxxj4v9qfHJkkFR0YWbDwJ1ppU_VecpnK3bM80ffh3RkEV4F_6PgzxAFLbhAYzyv-miC-AmlbfQIxV94qXlUrZwhXCUaX4EOizBHs4k4c6WzTuYXpQ1klLTHyK1UwlMHPZRk_cqkR42T6-ILWHswstM8VsCB1JkJxTdKHAsp6UZvOOA43AawAfm_0eVjNtyage7ke5mhA4hdpKUw-GMeeUhj09aThkSxfrbU0JqoUtgBeD66Q"),
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuBHGzTYMFminIRpbGm5c1xX_0PaINN2pTObioBe3t0JLL0gcO3yTUxrkdBdf0f8zCmFOJAFJEoEvDRUngGjUP2nF-WvIo-A97nfgZ7Od5daAu0sCJ3nEZpgJ-0k6KdFMG07MAYOvCixKkBjnkCwukPC-v9Xmb50SAJTHI88zJcPsQoiPYDlCRRHWPtF3soS-iku9U6nBuNHAB8nSOzvWrmDzucFT2OOdkkjDWbdXtoB82W7a5Rdp9EhcQ"),
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuBSo3pQI17QFc6vivqNK4NpbPy01L0e_NdwoQPnAdvF2W90K07bgKNnytlnM3b95o6TMq_JOceL755BFBwp2F75bH2uwtnQEKsGTm04-rjlnVv1R1agfJf64hlMMezKoYkg-j9P2yvrItjMYAjzF805uvRdVG8-GcmhxLwGRqGhZ6BQVhnH4n43ckldcv6WAhGe9U46GgJaY6P5OWxHRg3TWEb2-ngNwSqdgbkM-8TbjwqbQG4Lb2me8A", duration = "0:15"),
        MediaItem("https://lh3.googleusercontent.com/aida-public/AB6AXuChtUkKqMWvAN-z_WoD5-r4Kp17uH9STqb1j4u_P7UhQGL3VnmAuzioKSsR0FSvoD0OZBTH7M5xvnWGkjCyPVSS67X0G0R5j5V7aA3NGf9IHRIqVfHZTJ3KXoVjanpkjHJKbqJhdSjv_fEH11RxptRHnezjSf2SU-3paGoA5ApKZxG7Q0fS0KbNo7dUSGwHzbihjpvIT9Opn8FHHuZMz1FJMmNfUEKvEF4o9IHU07egxL5YySbUxZTI-Q")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Media", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W600, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            // Tab Navigation
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(Modifier.fillMaxWidth().height(48.dp).padding(4.dp)) {
                    tabs.forEachIndexed { index, title ->
                        val selected = selectedTab == index
                        Button(
                            onClick = { selectedTab = index },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Content
            if (selectedTab == 0) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    columns = GridCells.Fixed(3)
                ) {
                    // Today
                    item(span = { GridItemSpan(3) }) {
                        Text("Today", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(8.dp))
                    }
                    items(todayItems) { item ->
                        MediaGridItem(item)
                    }
                    // Yesterday
                    item(span = { GridItemSpan(3) }) {
                        Text("Yesterday", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
                    }
                    items(yesterdayItems) { item ->
                        MediaGridItem(item)
                    }
                }
            } else {
                // Empty state for Docs/Links
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (selectedTab == 1) Icons.Filled.Description else Icons.Filled.Link,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (selectedTab == 1) "No documents yet" else "No links yet",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (selectedTab == 1) "Shared files and PDFs will appear here." else "URLs and shared web content will appear here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaGridItem(item: MediaItem) {
    Box(
        modifier = Modifier
            .padding(1.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(item.url).crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(4.dp)
            ) {
                Icon(Icons.Filled.Videocam, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        if (item.duration != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(item.duration, color = Color.White, fontSize = 10.sp)
            }
        }
    }
}
