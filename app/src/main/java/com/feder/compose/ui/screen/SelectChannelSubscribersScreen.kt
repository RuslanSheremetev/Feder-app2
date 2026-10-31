package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.feder.compose.ChatItem

@Composable
fun SelectChannelSubscribersScreen(
    contacts: List<ChatItem>,
    myUsername: String,
    onBack: () -> Unit,
    onFinish: (List<String>) -> Unit
) {
    val selected = remember { mutableStateListOf<String>() }
    var search by remember { mutableStateOf("") }
    val filtered = contacts.filter { 
        it.username != myUsername && 
        (it.name.contains(search, true) || it.username.contains(search, true)) 
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("Выберите подписчиков", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Найти по имени", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color(0xFF1C1C1E),
                focusedContainerColor = Color(0xFF1C1C1E)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(Modifier.weight(1f).padding(top = 16.dp)) {
            items(filtered, key = { it.username }) { c ->
                val isChecked = selected.contains(c.username)
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (isChecked) selected.remove(c.username) else selected.add(c.username)
                    }.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = if (!c.avatarUrl.isNullOrEmpty()) "http://2.26.71.102:8010${c.avatarUrl}" else "http://2.26.71.102:8010/avatars/${c.username}/avatar.jpg",
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.name, color = Color.White, fontSize = 16.sp)
                        Text("был(-а) недавно", color = Color.Gray, fontSize = 12.sp)
                    }
                    Icon(
                        if (isChecked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        null,
                        tint = if (isChecked) Color(0xFF339DFF) else Color.Gray
                    )
                }
            }
        }

        Button(
            onClick = { onFinish(selected.toList()) },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF339DFF))
        ) {
            Text("Пропустить" + if (selected.isNotEmpty()) " (${selected.size})" else "", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
