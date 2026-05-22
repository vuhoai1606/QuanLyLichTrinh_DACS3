package com.bfy.schedule_app.ui.screens.collaboration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bfy.schedule_app.data.remote.model.UserDto
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.CollaborationViewModel
import com.bfy.schedule_app.utils.Localization

@Composable
fun CreateGroupDialog(
    viewModel: CollaborationViewModel,
    onDismissRequest: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }

    // Map of User -> Role ("MEMBER", "DEPUTY")
    val selectedMembers = remember { mutableStateMapOf<UserDto, String>() }

    val uiState by viewModel.uiState.collectAsState()

    // Clear search when dialog closes
    DisposableEffect(Unit) {
        onDispose { viewModel.clearSearch() }
    }

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
                    Text("Tạo nhóm mới", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Group Info
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên nhóm") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextPrimary,
                        unfocusedBorderColor = Color(0xFF333538),
                        focusedBorderColor = PrimaryColor
                    ),
                    singleLine = true
                )


                Spacer(modifier = Modifier.height(24.dp))
                Text("Thêm thành viên", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it 
                        viewModel.searchUsers(it)
                    },
                    label = { Text("Tìm theo tên hoặc email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextPrimary,
                        unfocusedBorderColor = Color(0xFF333538),
                        focusedBorderColor = PrimaryColor
                    )
                )

                // Search Results
                if (uiState.searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp).background(Color(0xFF1E2125), RoundedCornerShape(8.dp))) {
                        LazyColumn {
                            items(uiState.searchResults) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!selectedMembers.containsKey(user)) {
                                                selectedMembers[user] = "MEMBER"
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

                Spacer(modifier = Modifier.height(16.dp))

                // Selected Members
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(selectedMembers.keys.toList()) { user ->
                        val role = selectedMembers[user] ?: "MEMBER"
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
                                    .background(PrimaryColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.full_name.take(1).uppercase(),
                                    color = PrimaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.full_name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Role Toggle
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
                                            .background(if (role == "MEMBER") PrimaryColor else Color.Transparent)
                                            .clickable { selectedMembers[user] = "MEMBER" }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Thành viên", 
                                            color = if (role == "MEMBER") Color(0xFF003731) else TextSecondary, 
                                            fontSize = 12.sp,
                                            fontWeight = if (role == "MEMBER") FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (role == "DEPUTY") PrimaryColor else Color.Transparent)
                                            .clickable { selectedMembers[user] = "DEPUTY" }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Phó nhóm", 
                                            color = if (role == "DEPUTY") Color(0xFF003731) else TextSecondary, 
                                            fontSize = 12.sp,
                                            fontWeight = if (role == "DEPUTY") FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                            
                            IconButton(onClick = { selectedMembers.remove(user) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val memberList = selectedMembers.map { 
                            com.bfy.schedule_app.data.remote.model.GroupMemberRequest(user_id = it.key.id, role = it.value) 
                        }
                        viewModel.createGroup(name, null, null, memberList, onSuccess = {
                            onSuccess(it)
                        })
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor),
                    shape = RoundedCornerShape(25.dp),
                    enabled = name.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Tạo Nhóm", color = Color(0xFF003731), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
