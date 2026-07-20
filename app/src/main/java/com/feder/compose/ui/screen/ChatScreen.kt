package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feder.compose.ui.theme.*

data class Message(
    val text: String,
    val time: String,
    val isOutgoing: Boolean,
    val isRead: Boolean = false
)

@Composable
fun ChatScreen(chatName: String, onBack: () -> Unit) {
    val messages = remember {
        listOf(
            Message("Hey! Did you have a chance to look at the latest UI proposal?", "10:42 AM", false),
            Message("Just finished reviewing it. The tonal layers are looking really solid!", "10:45 AM", true, true),
            Message("Awesome! Should we sync at 2 PM to finalize?", "10:46 AM", false),
            Message("Sounds good. I'll prepare the updated mocks for the meeting.", "10:50 AM", true, true)
        )
    }
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Header
        Surface(color = Surface, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "back", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                }
                
                // Avatar
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, OutlineVariant, CircleShape)) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Text(chatName.take(1).uppercase(), color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Name + online
                Column(Modifier.weight(1f)) {
                    Text(chatName, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Primary))
                        Spacer(Modifier.width(4.dp))
                        Text("online", color = Primary, fontSize = 11.sp)
                    }
                }
                
                // Icons
                IconButton(onClick = { }) { Icon(Icons.Filled.Videocam, "video", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                IconButton(onClick = { }) { Icon(Icons.Filled.Call, "call", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
                IconButton(onClick = { }) { Icon(Icons.Filled.MoreVert, "more", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp)) }
            }
        }
        
        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                // Date separator
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Surface(shape = RoundedCornerShape(20.dp), color = SurfaceContainerLow) {
                        Text("Today", color = OnSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                }
            }
            
            items(messages) { msg ->
                MessageBubble(msg)
            }
        }
        
        // Input area
        Surface(color = Surface, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.Add, "add", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                        }
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            singleLine = true,
                            textStyle = TextStyle(color = OnSurface, fontSize = 14.sp),
                            cursorBrush = SolidColor(Primary),
                            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text("Message", color = OnSurfaceVariant, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                        IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Filled.AttachFile, "attach", tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                
                Spacer(Modifier.width(8.dp))
                
                // Send button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (inputText.isEmpty()) Icons.Filled.Mic else Icons.Filled.Send,
                        "send",
                        tint = OnPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: Message) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = if (msg.isOutgoing) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 340.dp),
            shape = if (msg.isOutgoing) RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                    else RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
            color = if (msg.isOutgoing) PrimaryContainer else SecondaryContainer
        ) {
            Text(
                msg.text,
                color = if (msg.isOutgoing) OnPrimaryContainer else OnSurface,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        
        Row(
            modifier = Modifier.padding(top = 2.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(msg.time, color = OnSurfaceVariant, fontSize = 11.sp)
            if (msg.isOutgoing && msg.isRead) {
                Spacer(Modifier.width(2.dp))
                Icon(Icons.Filled.DoneAll, "read", tint = Primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
