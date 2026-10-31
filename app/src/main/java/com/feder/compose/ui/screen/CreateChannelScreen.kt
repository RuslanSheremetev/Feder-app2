package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChannelScreen(
    onBack: () -> Unit,
    onNext: (name: String, isPrivate: Boolean) -> Unit
) {
    var channelName by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(true) }
    var requireApproval by remember { mutableStateOf(false) }
    val inviteLink = remember { "https://max.ru/join/${Random.nextInt(100000, 999999)}" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)) // Темный фон как на скрине
            .padding(16.dp)
    ) {
        // Шапка
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp, top = 24.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("Тип канала и ссылка", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedTextField(
            value = channelName,
            onValueChange = { channelName = it },
            placeholder = { Text("Название канала", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF339DFF),
                unfocusedBorderColor = Color.Gray
            )
        )

        Text("ТИП КАНАЛА", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

        // Приватный
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
                .clickable { isPrivate = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Приватный", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("Канал доступен только по ссылке", color = Color.Gray, fontSize = 12.sp)
            }
            RadioButton(selected = isPrivate, onClick = { isPrivate = true })
        }
        Spacer(Modifier.height(8.dp))
        // Публичный
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
                .clickable { isPrivate = false }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Публичный", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("Канал можно найти в поиске", color = Color.Gray, fontSize = 12.sp)
            }
            RadioButton(selected = !isPrivate, onClick = { isPrivate = false })
        }

        Spacer(Modifier.height(24.dp))
        
        if (isPrivate) {
            Text("ССЫЛКА-ПРИГЛАШЕНИЕ", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1C1C1E))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(inviteLink, color = Color(0xFF339DFF), modifier = Modifier.weight(1f), maxLines = 1)
                Icon(Icons.Filled.ContentCopy, "Copy", tint = Color.Gray)
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Заявки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1C1C1E))
                    .clickable { requireApproval = !requireApproval }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Заявки на вступление", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Подписаться можно только после одобрения", color = Color.Gray, fontSize = 12.sp)
                }
                Switch(checked = requireApproval, onCheckedChange = { requireApproval = it })
            }
        }

        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = { if (channelName.isNotBlank()) onNext(channelName, isPrivate) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF339DFF)),
            enabled = channelName.isNotBlank()
        ) {
            Text("Продолжить", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
