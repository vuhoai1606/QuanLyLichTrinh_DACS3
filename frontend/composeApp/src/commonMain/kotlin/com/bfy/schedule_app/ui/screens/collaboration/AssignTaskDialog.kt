package com.bfy.schedule_app.ui.screens.collaboration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bfy.schedule_app.data.remote.model.GroupMemberDto
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.GroupDetailViewModel

@Composable
fun AssignTaskDialog(
    groupId: String,
    viewModel: GroupDetailViewModel,
    onDismissRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var type by remember { mutableStateOf("ANNOUNCEMENT") } // ANNOUNCEMENT, TASK, EVENT
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // For assignees
    var assigneeExpanded by remember { mutableStateOf(false) }
    var selectedAssignees by remember { mutableStateOf<List<GroupMemberDto>>(emptyList()) }

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
                    Text("Thêm mới", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Type Selection
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ANNOUNCEMENT" to "Thông báo", "TASK" to "Công việc", "EVENT" to "Sự kiện").forEach { (key, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(if (type == key) PrimaryColor else Color(0xFF282A2D), RoundedCornerShape(8.dp))
                                .clickable { type = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (type == key) Color(0xFF003731) else TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextPrimary,
                        unfocusedBorderColor = Color(0xFF333538),
                        focusedBorderColor = PrimaryColor
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = TextPrimary,
                        unfocusedBorderColor = Color(0xFF333538),
                        focusedBorderColor = PrimaryColor
                    ),
                    minLines = 3
                )

                if (type != "ANNOUNCEMENT") {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box {
                        OutlinedTextField(
                            value = selectedAssignees.joinToString(", ") { it.full_name },
                            onValueChange = { },
                            label = { Text("Người thực hiện") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { assigneeExpanded = !assigneeExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimary)
                                }
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = TextPrimary,
                                unfocusedBorderColor = Color(0xFF333538),
                                focusedBorderColor = PrimaryColor
                            )
                        )
                        
                        // Transparent box to capture clicks
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { assigneeExpanded = true }
                        )
                        
                        DropdownMenu(
                            expanded = assigneeExpanded,
                            onDismissRequest = { assigneeExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF282A2D))
                        ) {
                            uiState.members.forEach { member ->
                                val isSelected = selectedAssignees.contains(member)
                                DropdownMenuItem(onClick = {
                                    if (isSelected) {
                                        selectedAssignees = selectedAssignees - member
                                    } else {
                                        selectedAssignees = selectedAssignees + member
                                    }
                                }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(member.full_name, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val assignees = if (type != "ANNOUNCEMENT") selectedAssignees.map { it.id } else emptyList()
                        viewModel.createSchedule(groupId, type, title, description, assignees)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor),
                    shape = RoundedCornerShape(25.dp),
                    enabled = title.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Tạo", color = Color(0xFF003731), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
