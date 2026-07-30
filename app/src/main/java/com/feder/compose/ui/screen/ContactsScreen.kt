package com.feder.compose.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class Contact(
    val name: String,
    val status: String,
    val avatarUrl: String? = null,
    val initials: String? = null,
    val online: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onBack: () -> Unit) {
    var searchText by remember { mutableStateOf("") }

    val contacts = listOf(
        Contact("Adeline Vance", "online", "https://lh3.googleusercontent.com/aida-public/AB6AXuCocqYj4XRvJvH4qN7gaRnx_tP2okau1VDNO0xe2iNzCUeNgJQuob3TqPDyRMUXtytRz6MZ1KGdJrrhZ5Cj5PobRn9HBuPvBBXkucHPc6tU_CmevyMZO6KIzyQYqiI1M3SypaYmvKfKD5N78E_MhseFzEP8f4DVdxqxrWWtO5spHA7fqv34RCuoHOCoB5yarWw8Y5if4EsRhds7cJgLkWJpV4mt_jJToarYRmVtRSedVxc8KHRaCZGeNAFYRShFhX8D2MuF_HnWuQQ", online = true),
        Contact("Arthur Shelby", "last seen 2h ago", initials = "AS"),
        Contact("Beatrice Thorne", "last seen yesterday at 11:42 PM", "https://lh3.googleusercontent.com/aida-public/AB6AXuD8rJMpBCcb158M4nqX0R0t9nP7u3IaCRJb7yUFC-VwBuQUeQhKHrUBAo4TqgnQ2MuzI6LKUtliGDsvvNYoFKVYXR4FYcLiisS6lmOKkO4gwB2AJfmPYySjnv0mOFJeCXt40TFnpRmG_qX2ra-lyG-2cRpeu7w34JSJuUaxRxQCeXxpP1Xnf0R2mlHQ8r4dQjGBDevqvKV3C0q4cFbHi2DY4ihGeHpCZLNBC9oDNgVKpHqmX3iBJH-lQf7cx_AaW8HCmbIG7aLSmnQ"),
        Contact("Cassian Wilde", "online", initials = "CW", online = true),
        Contact("Cyrus Moen", "last seen 5m ago", "https://lh3.googleusercontent.com/aida-public/AB6AXuA7p9auMo6q_AjZ1JmUb_vKzSsATp8Vs_EJpEwypiU05zLI_T42TeBWb9aF7zIkO6ixZ3idXUuMqsZX3-47E8s2g_uNPW8P59oKw8-Z7JoF5CRe-3utZWghgHampKQ-pXzjAb88lcN7R6j55-0OZqLOgMfUlbpXHqifGGSM2JD4wOLi0L0IOdZj2Cs1qsO9VYv7epYJAcIHPo1UfaQbBR9JBMi1RBZ3UbcpOHCP2LFQPrSbRBnIDJST2hAqTHIoxssxZMlMEkiG96I")
    )

    val groupedContacts = contacts.groupBy { it.name.first().uppercase() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Contacts", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.W600, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Search
                item {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search contacts...", color = MaterialTheme.colorScheme.outline) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.outline) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                // New Group, New Secret Chat, New Channel
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ActionButton(Icons.Filled.GroupAdd, "New Group")
                        ActionButton(Icons.Filled.Lock, "New Secret Chat")
                        ActionButton(Icons.Filled.Campaign, "New Channel")
                    }
                }

                // Contacts by letter
                groupedContacts.forEach { (letter, contacts) ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f)
                        ) {
                            Text(
                                letter,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W600,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    items(contacts) { contact ->
                        ContactRow(contact)
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }

            // FAB
            FloatingActionButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 96.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.PersonAdd, "Add contact", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.W500)
    }
}

@Composable
private fun ContactRow(contact: Contact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box {
            if (contact.avatarUrl != null) {
                AsyncImage(
                    model = contact.avatarUrl,
                    contentDescription = contact.name,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(contact.initials ?: "", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.W600, fontSize = 20.sp)
                }
            }
            if (contact.online) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.W500)
            Text(contact.status, color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
}
