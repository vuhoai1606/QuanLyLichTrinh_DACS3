package com.bfy.schedule_app.ui.screens.collaboration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.bfy.schedule_app.rememberBitmapFromUrlOrBase64
import com.bfy.schedule_app.rememberImagePicker
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.GroupDetailViewModel
import com.bfy.schedule_app.utils.Localization

@Composable
fun GroupSettingsDialog(
    groupId: String,
    viewModel: GroupDetailViewModel,
    onDismissRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val group = uiState.group
    val currentUser = uiState.currentUser
    val members = uiState.members

    val myRole = members.find { it.id == currentUser?.id }?.role

    var name by remember { mutableStateOf(group?.name ?: "") }
    var avatarUrl by remember { mutableStateOf(group?.avatar_url ?: "") }
    var searchQuery by remember { mutableStateOf("") }
    var showAvatarDialog by remember { mutableStateOf(false) }

    val pickImage = rememberImagePicker { base64 ->
        avatarUrl = base64
        showAvatarDialog = false
    }

    val localMembers = remember(members) { mutableStateListOf(*members.toTypedArray()) }
    val isDirty = name != group?.name || avatarUrl != (group?.avatar_url ?: "") || localMembers.toList() != members

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Localization.get("group_settings") ?: "Group Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (myRole == "LEADER" || myRole == "DEPUTY") {
                    Text(Localization.get("group_avatar") ?: "Group Avatar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25262B))
                                .border(2.dp, Color(0xFF333538), CircleShape)
                                .clickable { showAvatarDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl.isNotBlank()) {
                                val bitmap = rememberBitmapFromUrlOrBase64(avatarUrl)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                                }
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = Localization.get("change_avatar") ?: "Change Avatar",
                            color = Color(0xFF59DBC7),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showAvatarDialog = true }.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Localization.get("group_name") ?: "Group Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextPrimary,
                            unfocusedBorderColor = Color(0xFF333538),
                            focusedBorderColor = PrimaryColor
                        ),
                        singleLine = true
                    )



                } else {
                    Text("${Localization.get("group_name") ?: "Group Name"}: ${group?.name}", color = TextPrimary, fontSize = 16.sp)

                }

                if (myRole == "LEADER" || myRole == "DEPUTY") {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(Localization.get("add_members") ?: "Add members", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it 
                            viewModel.searchUsers(it)
                        },
                        label = { Text(Localization.get("search_by_name_email") ?: "Search by name or email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = TextPrimary,
                            unfocusedBorderColor = Color(0xFF333538),
                            focusedBorderColor = PrimaryColor
                        )
                    )

                    if (uiState.searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).background(Color(0xFF1E2125), RoundedCornerShape(8.dp))) {
                            LazyColumn {
                                items(uiState.searchResults) { user ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val existing = localMembers.find { it.id == user.id }
                                                if (existing == null) {
                                                    localMembers.add(
                                                        com.bfy.schedule_app.data.remote.model.GroupMemberDto(
                                                            id = user.id,
                                                            email = user.email ?: "",
                                                            full_name = user.full_name,
                                                            avatar_url = user.avatar_url,
                                                            role = "MEMBER",
                                                            joined_at = ""
                                                        )
                                                    )
                                                }
                                                searchQuery = ""
                                                viewModel.clearSearch()
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(user.full_name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                            user.email?.let { Text(it, color = TextSecondary, fontSize = 12.sp) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("${Localization.get("members") ?: "Members"} (${localMembers.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(localMembers, key = { it.id }) { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color(0xFF282A2D), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar placeholder
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PrimaryColor.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.full_name.take(1).uppercase(),
                                    color = PrimaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.full_name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                
                                val roleColor = when(member.role) {
                                    "LEADER" -> Color(0xFFE2B93B) // Gold
                                    "DEPUTY" -> Color(0xFF4FC3F7) // Blue
                                    else -> TextSecondary
                                }
                                
                                val canEditRole = myRole == "LEADER" && member.role != "LEADER"
                                
                                if (canEditRole) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1A1C1E))
                                            .padding(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (member.role == "MEMBER") PrimaryColor else Color.Transparent)
                                                .clickable { 
                                                    val idx = localMembers.indexOfFirst { it.id == member.id }
                                                    if (idx != -1) localMembers[idx] = localMembers[idx].copy(role = "MEMBER") 
                                                }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(Localization.get("member") ?: "Member", color = if (member.role == "MEMBER") Color(0xFF003731) else TextSecondary, fontSize = 12.sp, fontWeight = if (member.role == "MEMBER") FontWeight.Bold else FontWeight.Normal)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (member.role == "DEPUTY") PrimaryColor else Color.Transparent)
                                                .clickable { 
                                                    val idx = localMembers.indexOfFirst { it.id == member.id }
                                                    if (idx != -1) localMembers[idx] = localMembers[idx].copy(role = "DEPUTY")
                                                }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(Localization.get("deputy") ?: "Deputy", color = if (member.role == "DEPUTY") Color(0xFF003731) else TextSecondary, fontSize = 12.sp, fontWeight = if (member.role == "DEPUTY") FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = when (member.role) {
                                            "LEADER" -> Localization.get("leader") ?: "Leader"
                                            "DEPUTY" -> Localization.get("deputy") ?: "Deputy"
                                            else -> Localization.get("member") ?: "Member"
                                        },
                                        color = roleColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            
                            // Can remove if we are LEADER, or we are DEPUTY and target is MEMBER
                            val canRemove = (myRole == "LEADER" && member.role != "LEADER") ||
                                          (myRole == "DEPUTY" && member.role == "MEMBER")
                            
                            if (canRemove) {
                                IconButton(onClick = { 
                                    localMembers.removeAll { it.id == member.id } 
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorColor)
                                }
                            }
                        }
                    }
                }

                if (myRole == "LEADER" || myRole == "DEPUTY") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val membersToAdd = localMembers.filter { lm -> members.none { it.id == lm.id } }
                                .map { it.id to it.role }
                            val membersToRemove = members.filter { m -> localMembers.none { it.id == m.id } }
                                .map { it.id }
                            val membersToUpdateRole = localMembers.filter { lm -> 
                                members.any { it.id == lm.id && it.role != lm.role } 
                            }.map { it.id to it.role }

                            viewModel.saveGroupChanges(
                                groupId = groupId,
                                name = name,
                                avatarUrl = avatarUrl.ifBlank { null },
                                membersToAdd = membersToAdd,
                                membersToRemove = membersToRemove,
                                membersToUpdateRole = membersToUpdateRole
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryColor,
                            disabledBackgroundColor = Color(0xFF333538)
                        ),
                        shape = RoundedCornerShape(25.dp),
                        enabled = isDirty && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(Localization.get("save_changes") ?: "Save changes", color = if (isDirty) Color(0xFF003731) else Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (showAvatarDialog) {
                val presetAvatars = listOf(
                    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=60",
                    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=60",
                    "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=150&auto=format&fit=crop&q=60",
                    "https://images.unsplash.com/photo-1607746882042-944635dfe10e?w=150&auto=format&fit=crop&q=60",
                    "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=60",
                    "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&auto=format&fit=crop&q=60"
                )

                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showAvatarDialog = false },
                    title = { Text("Select Avatar", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Choose from premium presets:", color = Color(0xFFBBCAC5), fontSize = 14.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val rows = presetAvatars.chunked(3)
                                rows.forEachIndexed { rowIndex, rowList ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowList.forEachIndexed { colIndex, url ->
                                            val globalIndex = rowIndex * 3 + colIndex
                                            val isSelected = avatarUrl == url
                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) Color(0xFF59DBC7) else Color.Transparent,
                                                        CircleShape
                                                    )
                                                    .clickable { avatarUrl = url; showAvatarDialog = false }
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "AV ",
                                                    color = if (isSelected) Color(0xFF003731) else Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.Button(
                                onClick = { pickImage() },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1B4A)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFAD7BFF))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Image", color = Color(0xFFAD7BFF), fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { showAvatarDialog = false }) {
                            Text("OK", color = Color(0xFF59DBC7))
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { showAvatarDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1E2023),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
