package com.bfy.schedule_app.ui.screens.calendar

import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.utils.Localization
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bfy.schedule_app.ui.viewmodel.CalendarViewModel

enum class CalendarViewType(val key: String) { 
    DAY("day"), 
    .WEEK("week"),
    MONTH("month"), 
    YEAR("year") 
}

@Composable
fun CalendarScreen() {
    val viewModel: CalendarViewModel = viewModel { CalendarViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate = uiState.selectedDate
    var viewType by remember { mutableStateOf(CalendarViewType.MONTH) }

    var selectedScheduleForAction by remember { mutableStateOf<com.bfy.schedule_app.data.remote.model.ScheduleDto?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var showEditScreen by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val platformContext = com.bfy.schedule_app.platform.rememberPlatformContext()
    
    // val syncLauncher = com.bfy.schedule_app.platform.rememberGoogleAuthLauncher { token ->
    //     if (token != null) {
    //         viewModel.triggerBackendSync()
    //     } else {
    //         viewModel.clearError() // Ensure no error on cancellation
    //     }
    // }

    LaunchedEffect(Unit) {
        viewModel.loadSchedules()
    }

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    if (uiState.error != null) {
        val isAuthError = uiState.error == "G_CAL_NOT_CONNECTED"
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text(if (isAuthError) "Kết nối Google" else "Thông báo", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(if (isAuthError) "Bạn cần cấp quyền truy cập Google Calendar để đồng bộ." else uiState.error!!, color = Color(0xFFBBCAC5)) },
            confirmButton = {
                if (isAuthError && uiState.authUrl != null) {
                    androidx.compose.material3.TextButton(onClick = { 
                        uriHandler.openUri(uiState.authUrl!!)
                        viewModel.clearError()
                    }) {
                        Text("Kết nối ngay", color = PrimaryColor)
                    }
                } else {
                    androidx.compose.material3.TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK", color = PrimaryColor)
                    }
                }
            },
            dismissButton = {
                if (isAuthError) {
                    androidx.compose.material3.TextButton(onClick = { viewModel.clearError() }) {
                        Text("Hủy", color = TextSecondary)
                    }
                }
            },
            containerColor = Color(0xFF1E2023),
            shape = RoundedCornerShape(16.dp)
        )
    }

    val headerTitle = when (viewType) {
        CalendarViewType.DAY -> "${selectedDate.dayOfMonth} ${Localization.getMonth(selectedDate.month)} ${selectedDate.year}"
        CalendarViewType.WEEK -> "Week of ${selectedDate.dayOfMonth} ${Localization.getMonth(selectedDate.month)}"
        CalendarViewType.MONTH -> "${Localization.getMonth(selectedDate.month)} ${selectedDate.year}"
        CalendarViewType.YEAR -> "${selectedDate.year}"
    }

    val onPrevClick = {
        when (viewType) {
            CalendarViewType.DAY -> viewModel.prevDay()
            CalendarViewType.WEEK -> viewModel.prevWeek()
            CalendarViewType.MONTH -> viewModel.prevMonth()
            CalendarViewType.YEAR -> viewModel.prevYear()
        }
    }

    val onNextClick = {
        when (viewType) {
            CalendarViewType.DAY -> viewModel.nextDay()
            CalendarViewType.WEEK -> viewModel.nextWeek()
            CalendarViewType.MONTH -> viewModel.nextMonth()
            CalendarViewType.YEAR -> viewModel.nextYear()
        }
    }

    val onItemClick: (com.bfy.schedule_app.data.remote.model.ScheduleDto) -> Unit = { schedule ->
        selectedScheduleForAction = schedule
        showActionDialog = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    headerTitle,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = PrimaryColor,
                        modifier = Modifier.clickable { viewModel.triggerBackendSync() }.padding(end = 12.dp)
                    )
                    Text(
                        text = Localization.get("today") ?: "Today",
                        color = PrimaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.goToToday() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Default.KeyboardArrowLeft, 
                        contentDescription = "Previous", 
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onPrevClick() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.KeyboardArrowRight, 
                        contentDescription = "Next", 
                        tint = TextSecondary,
                        modifier = Modifier.clickable { onNextClick() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Segmented Control
            CalendarSegmentedControl(
                modifier = Modifier.padding(horizontal = 24.dp),
                selectedOption = viewType,
                onOptionSelected = { viewType = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (viewType) {
                CalendarViewType.DAY -> CalendarDayViewScreen(viewModel, onItemClick)
                CalendarViewType.WEEK -> CalendarWeekViewScreen(viewModel, onItemClick)
                CalendarViewType.MONTH -> CalendarFullMonthViewScreen(viewModel, onItemClick)
                CalendarViewType.YEAR -> CalendarYearViewScreen(viewModel) { date ->
                    viewModel.onDateSelected(date)
                    viewType = CalendarViewType.MONTH
                }
            }
        }

        // Action Options Dialog (Edit/Delete options)
        if (showActionDialog && selectedScheduleForAction != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showActionDialog = false },
                title = { 
                    Text(
                        text = selectedScheduleForAction?.title ?: "",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!selectedScheduleForAction!!.description.isNullOrBlank()) {
                            Text(
                                text = selectedScheduleForAction!!.description!!,
                                color = Color(0xFFBBCAC5),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        
                        androidx.compose.material3.Surface(
                            onClick = {
                                showActionDialog = false
                                showEditScreen = true
                            },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Edit", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        
                        androidx.compose.material3.Surface(
                            onClick = {
                                showActionDialog = false
                                showDeleteConfirm = true
                            },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF7B7B), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Delete", color = Color(0xFFFF7B7B), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showActionDialog = false }) {
                        Text(Localization.get("cancel") ?: "Cancel", color = Color(0xFF59DBC7))
                    }
                },
                containerColor = Color(0xFF1E2023),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Edit Item Overlay Screen
        if (showEditScreen && selectedScheduleForAction != null) {
            com.bfy.schedule_app.ui.screens.createitem.CreateNewItemScreen(
                initialSchedule = selectedScheduleForAction,
                onDismiss = { 
                    showEditScreen = false 
                    selectedScheduleForAction = null
                },
                onSuccess = { viewModel.loadSchedules() }
            )
        }

        // Delete Confirm Dialog with proper contrast
        if (showDeleteConfirm && selectedScheduleForAction != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { 
                    Text(
                        Localization.get("delete_item"), 
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                text = { 
                    Text(
                        Localization.get("delete_confirm_msg").format(selectedScheduleForAction?.title ?: ""), 
                        color = Color(0xFFBBCAC5),
                        fontSize = 14.sp
                    ) 
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        scope.launch {
                            try {
                                com.bfy.schedule_app.data.repository.AppRepository().deleteSchedule(selectedScheduleForAction!!.id)
                                viewModel.loadSchedules()
                                showDeleteConfirm = false
                                selectedScheduleForAction = null
                            } catch (e: Exception) {}
                        }
                    }) {
                        Text(Localization.get("delete"), color = Color(0xFFFF7B7B), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(Localization.get("cancel"), color = Color(0xFF869490))
                    }
                },
                containerColor = Color(0xFF1E2023),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun CalendarSegmentedControl(
    modifier: Modifier = Modifier,
    selectedOption: CalendarViewType,
    onOptionSelected: (CalendarViewType) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9999.dp))
            .background(Color(0xFF282A2D))
            .border(1.dp, Color(0xFF333538), RoundedCornerShape(9999.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CalendarViewType.values().forEach { option ->
            val isSelected = selectedOption == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (isSelected) PrimaryColor else Color.Transparent)
                    .clickable { onOptionSelected(option) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localization.get(option.key),
                    color = if (isSelected) Color(0xFF003731) else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


