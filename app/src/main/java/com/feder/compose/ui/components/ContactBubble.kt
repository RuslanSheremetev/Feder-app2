package com.feder.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.feder.compose.ui.screen.DrawCheck

@Composable
fun ContactBubble(
    contactName: String,
    contactPhones: String,
    contactAvatar: String?,
    time: String,
    isMine: Boolean,
    status: String,
    onClick: () -> Unit
) {
    // Парсим "Main:+7999|Work:+7888"
    val phones = contactPhones.split("|").filter { it.isNotBlank() }.map {
        val parts = it.split(":")
        if (parts.size >= 2) parts[0] to parts[1] else "Phone" to it
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .padding(
                start = if (isMine) 40.dp else 8.dp,
                end = if (isMine) 8.dp else 40.dp
            ),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isMine) Color(0xFF339DFF) else Color(0xFF2B2B2B))
                .clickable(onClick = onClick)
        ) {
            // Верхний блок: аватарка + имя + телефоны
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Вертикальная белая полоса
                Box(
                    Modifier
                        .width(3.dp)
                        .height(42.dp)
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(10.dp))
                
                // Аватарка
                if (!contactAvatar.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(contactAvatar).crossfade(true).build(),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(42.dp).clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    Text(
                        contactName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    phones.forEach { (label, number) ->
                        Text(
                            "$label: $number",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Разделитель
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
            
            // Нижний блок: "MESSAGE"
            Box(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("MESSAGE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            // Время + галочки
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(time, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                if (isMine) {
                    Spacer(Modifier.width(3.dp))
                    val checkColor = if (status == "read") Color(0xFF4CAF50) else Color.White.copy(alpha = 0.7f)
                    DrawCheck(
                        double = (status == "read" || status == "received"),
                        tint = checkColor,
                        size = 12.dp
                    )
                }
            }
        }
    }
}
