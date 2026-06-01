package com.bfy.schedule_app.ui.screens.collaboration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.utils.Localization
import com.bfy.schedule_app.ui.viewmodel.CollaborationViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState


@Composable
fun CollaborationScreen(onGroupClick: (String) -> Unit) {
    val viewModel: CollaborationViewModel = viewModel { CollaborationViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var newGroupDesc by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf(0) } // 0: My Groups, 1: Assigned to me

    LaunchedEffect(currentTab) {
        viewModel.loadGroups()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = Localization.get("collaboration"),
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(Color(0xFF282A2D))
                .border(1.dp, Color(0xFF333538), RoundedCornerShape(9999.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (currentTab == 0) PrimaryColor else Color.Transparent)
                    .clickable { currentTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(Localization.get("my_groups"), color = if (currentTab == 0) Color(0xFF003731) else TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (currentTab == 1) PrimaryColor else Color.Transparent)
                    .clickable { currentTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(Localization.get("assigned_to_me"), color = if (currentTab == 1) Color(0xFF003731) else TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentTab == 0) Localization.get("active_groups") else Localization.get("shared_items") ?: "Shared Items",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (currentTab == 0) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = "Add Group", 
                            tint = PrimaryColor,
                            modifier = Modifier.clip(CircleShape).clickable { showCreateDialog = true }.padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (currentTab == 0 && uiState.groups.isEmpty() && !uiState.isLoading) {
                item {
                    Text(Localization.get("no_groups"), color = TextSecondary, modifier = Modifier.padding(vertical = 16.dp))
                }
            } else if (currentTab == 1 && uiState.sharedSchedules.isEmpty() && !uiState.isLoading) {
                item {
                    Text(Localization.get("no_assigned_tasks"), color = TextSecondary, modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            if (uiState.isLoading) {
                item { CircularProgressIndicator(color = PrimaryColor, modifier = Modifier.padding(vertical = 16.dp)) }
            }

            if (currentTab == 0) {
                items(uiState.groups.size) { index ->
                    val group = uiState.groups[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E2125))
                            .clickable { onGroupClick(group.id) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (index % 2 == 0) Color(0xFFAD7BFF) else Color(0xFF92B4FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(group.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(group.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(Localization.get("updated_recently"), color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                items(uiState.sharedSchedules.size) { index ->
                    val schedule = uiState.sharedSchedules[index]
                    com.bfy.schedule_app.ui.screens.homedashboard.TimelineCard(
                        schedule = schedule,
                        onClick = { },
                        onStatusChange = { viewModel.loadGroups() }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateGroupDialog(
            viewModel = viewModel,
            onDismissRequest = { showCreateDialog = false },
            onSuccess = { groupId ->
                showCreateDialog = false
                onGroupClick(groupId)
            }
        )
    }
}

