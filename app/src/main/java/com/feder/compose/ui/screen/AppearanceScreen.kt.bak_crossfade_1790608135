package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    var textSize by remember { mutableFloatStateOf(2f) }
    var selectedTheme by remember { mutableIntStateOf(1) } // 0=System, 1=Light, 2=Dark
    var selectedWallpaper by remember { mutableIntStateOf(0) }

    val wallpapers = listOf(
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAQl5vzI0rcDjduGfC99V5M8WBFB6W4tA90opAjPTpXbJbVOhLGb51zn7RP0Hgjm2Hrler3SgbS1toyfx5r7YRvcS-E_sUEX8iE3R4N0bXqLEyQwFq5kzb8Pu3qHF9dlr9Zdatqy1nw3eFsKWmPtsWfx2vViQ37Co8B0Mr5uPb75qVjRvzMjdCaOjAFuX4tK84ZOUl4lw4UxZafDeSRhCg3diVybiHVIZWjwWDkBCwfWb-S9jlMKSiPAg",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAoAHG9kqczsTJkYZDhHPnnIea-6-jJ5gtt7gCtosJVhlrsUAGe5vGpeQwkP0-r4eZjD9fgK07ztxrLEBMkpdxjy9xZJmMnTlBY4YI0FvRCS2EaknrvbCEX-3pI0MixddfFqNRS8ZFECtd-WenAQyhAbqyOs-AyJpkaVdspUF5Nj_woGD8K_GPX8h1sqWCsWl8ZsdEqJFfyzByhNadwTm1SB-1zC3U_jFUWMuwQNq0uuWtditYRPhBYnA",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBoMeg_Fv9y6WyZuybffaVST6h3VjephobFPwsKBQhr5qj7FkHMS22idhatfPptLu3GlspZtqPP7a6sn3asjN0LaEe4Wy1jlmEfULzXDs0ItHTPHsewzVvQx2Nz9ETRZMnqa_6Xfo4vdPm567CO4T_hxcFYm6-9Umfyk9aN3dYd5hYa49HnfnEk6SyfHvEBzDUK9OHY4JU7tDzmQiu47MJQ8raWrLoexXLNrsH9_SKxDQtvAIwI3ZfRww",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAb7vsgclSSGqX71QS1iY5Fz42HJTgiNFekNgrS3GDEO177EigPIp0_y5nXaHLeNMHRLutESvvPctbi5Zns4KduhU4vTWKx8XHHWpG8rXAvEXD3UoumVKQ5F8ypbLPzr7X42RL07C4bmT3Pllhp_30R5PmQFU7lPMiMiY2S5GCJuk4vuRimQKbxOAUPo8y_n-JJGbKkr2sf-GIvnbJrTRIJqnx5oUNUAw81Q_g557LhCE1adkZ-gHMKEQ",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAH5AE-DJxxVRv-B7SAafMFZBTki1VwnsUcT7cQuFi01CuzBSsRiUJFHJWeR8dsfD_Fa2MXLTMTZS1sjI1JB01-bvA21w1eadcWLatzwIlNhU5WuK4Ne7p6VsWbT-x7wZcdcCELLLzg-mJ3L9FQEochp4CT1ay1zRVkwBPlqeqWf0eU26_ESxhJ-AckBsyZxV98AgAFyYrPeLsQujUM2bQv97aSkhRhhojNvmy3mSYlJ6mkJVhQC7v2cw"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Оформление", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W600, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Text Size
            Text("Размер текста", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("A", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
                    Slider(
                        value = textSize,
                        onValueChange = { textSize = it },
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text("A", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 20.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Theme
            Text("Тема", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(Modifier.padding(4.dp)) {
                    listOf("Системная", "Светлая", "Тёмная").forEachIndexed { index, title ->
                        val selected = selectedTheme == index
                        Button(
                            onClick = { selectedTheme = index },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = if (selected) ButtonDefaults.buttonElevation(1.dp) else ButtonDefaults.buttonElevation(0.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Chat Preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Incoming message
                    Surface(
                        modifier = Modifier.widthIn(max = 300.dp),
                        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Выберите тему, чтобы изменить фон и цвет сообщений 🎨", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("18:08", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.End))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // Outgoing message
                    Surface(
                        modifier = Modifier.widthIn(max = 300.dp).align(Alignment.End),
                        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Посмотрите, как с ней будут выглядеть ваши чаты", fontSize = 15.sp, color = MaterialTheme.colorScheme.onPrimary)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                                        Text("🔥 1", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = Color.White)
                                    }
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                                        Text("❤️ 1", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = Color.White)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("18:08", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(12.dp), tint = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    // Second incoming
                    Surface(
                        modifier = Modifier.widthIn(max = 300.dp),
                        shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Меняйте тему в любое время", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text("18:08", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Wallpapers
            Text("Простая", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                wallpapers.forEachIndexed { index, url ->
                    Box(
                        modifier = Modifier
                            .width(128.dp)
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(if (selectedWallpaper == index) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .clickable { selectedWallpaper = index }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Chat bubble preview overlay
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color.White, shadowElevation = 1.dp) {
                                Box(Modifier.width(40.dp).height(20.dp))
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.7f), shadowElevation = 1.dp) {
                                Box(Modifier.width(40.dp).height(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
