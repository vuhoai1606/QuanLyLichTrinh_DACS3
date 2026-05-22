package com.bfy.schedule_app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.viewmodel.ProfileViewModel
import com.bfy.schedule_app.ui.viewmodel.AuthViewModel
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.data.remote.model.*
import com.bfy.schedule_app.utils.Localization
import com.bfy.schedule_app.utils.SettingsManager
import com.bfy.schedule_app.utils.Language
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

@Composable
fun ProfileScreen(onLogout: () -> Unit = {}) {
    val viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    val focusStats = uiState.focusStats
    
    var showEditProfileScreen by remember { mutableStateOf(false) }
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    if (showEditProfileScreen) {
        EditProfileScreen(
            user = user,
            onDismiss = { showEditProfileScreen = false },
            onSave = { fullName, bio, gender, dob ->
                viewModel.updateProfile(fullName = fullName, bio = bio, gender = gender, dob = dob)
                showEditProfileScreen = false
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111316))
            .padding(horizontal = 24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    Localization.get("profile"),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { showEditProfileScreen = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF59DBC7))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.error != null) {
            item {
                Card(
                    backgroundColor = Color(0xFFFFB4AB).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { viewModel.clearMessages() }
                ) {
                    Text(
                        uiState.error!!,
                        color = Color(0xFFFFB4AB),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        if (uiState.successMessage != null) {
            item {
                Card(
                    backgroundColor = Color(0xFF59DBC7).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { viewModel.clearMessages() }
                ) {
                    Text(
                        uiState.successMessage!!,
                        color = Color(0xFF59DBC7),
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Section - Gamified Profile Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2023))
                    .border(1.dp, Color(0x4D3C4946), RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0x8059DBC7), CircleShape)
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = if (uiState.isLoading) Localization.get("loading") else (user?.full_name ?: Localization.get("unknown_user")),
                                color = Color(0xFFE2E2E6),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(user?.email ?: "", color = Color(0xFF869490), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Timezone: ${user?.timezone ?: "UTC"}", color = Color(0xFF869490), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33AD7BFF))
                                    .border(1.dp, Color(0x4DAD7BFF), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Text(user?.current_rank?.uppercase() ?: "ROOKIE", color = Color(0xFFAD7BFF), fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    if (user?.bio != null && user.bio.isNotBlank()) {
                        Text(
                            user.bio,
                            color = Color(0xFFBBCAC5),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val level = ((user?.total_exp ?: 0) / 1000) + 1
                        val expInLevel = (user?.total_exp ?: 0) % 1000
                        val nextLevelExp = 1000
                        
                        Text("${Localization.get("lvl")} $level", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$expInLevel", color = Color(0xFF59DBC7), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("/ $nextLevelExp ${Localization.get("exp")}", color = Color(0xFF869490), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 3.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x1A59DBC7))
                    ) {
                        val expInLevel = (user?.total_exp ?: 0) % 1000
                        val progress = expInLevel.toFloat() / 1000f
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF59DBC7), Color(0xFFD5BAFF))))
                        )
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Text("PERSONAL DETAILS", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2023))
                    .border(1.dp, Color(0x333C4946), RoundedCornerShape(12.dp))
            ) {
                InfoDetailRow(title = "Full Name", value = user?.full_name ?: "N/A", icon = Icons.Default.Person)
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                InfoDetailRow(title = "Email Address", value = user?.email ?: "N/A", icon = Icons.Default.Email)
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                InfoDetailRow(title = "Gender", value = user?.gender ?: "N/A", icon = Icons.Default.Face)
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                InfoDetailRow(title = "Date of Birth", value = user?.dob ?: "N/A", icon = Icons.Default.DateRange)
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        item {
            Text(Localization.get("focus_stats"), color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth().height(156.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2023))
                        .border(1.dp, Color(0x333C4946), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD5BAFF), modifier = Modifier.size(21.dp))
                        Column {
                            val totalHours = (focusStats?.total_minutes ?: 0) / 60
                            Text("${totalHours}${Localization.get("hour_1").substringAfter("1").take(1)}", color = Color(0xFFE2E2E6), fontSize = 34.sp, fontWeight = FontWeight.Bold)
                            Text(Localization.get("total_focus_time"), color = Color(0xFF869490), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2023))
                            .border(1.dp, Color(0x333C4946), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${focusStats?.completed_sessions ?: 0}", color = Color(0xFFE2E2E6), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF59DBC7), modifier = Modifier.size(16.dp))
                            }
                            Text(Localization.get("sessions"), color = Color(0xFF869490), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2023))
                            .border(1.dp, Color(0x333C4946), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                            val avgHours = if ((focusStats?.completed_sessions ?: 0) > 0) 
                                (focusStats?.total_minutes?.toFloat() ?: 0f) / 60 / (focusStats?.completed_sessions ?: 1)
                            else 0f
                            Text("${"%.1f".format(avgHours)}${Localization.get("hour_1").substringAfter("1").take(1)}", color = Color(0xFFE2E2E6), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                            Text(Localization.get("daily_avg"), color = Color(0xFF869490), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Localization.get("badge_board"), color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
                Text(Localization.get("view_all"), color = Color(0xFF59DBC7), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1C1F))
                    .border(1.dp, Color(0x1A3C4946), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.badges.isEmpty()) {
                        Text(Localization.get("no_notifications") ?: "No badges yet", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        uiState.badges.take(4).forEach { badge ->
                            BadgeItem(
                                filled = badge.unlocked_at != null,
                                tint = if (badge.unlocked_at != null) Color(0xFF59DBC7) else Color.Gray,
                                bgTint = if (badge.unlocked_at != null) Color(0x1A59DBC7) else Color(0xFF282A2D)
                            )
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        item {
            Text("ACCOUNT & SECURITY", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2023))
                    .border(1.dp, Color(0x333C4946), RoundedCornerShape(12.dp))
            ) {
                SettingRow(
                    title = "Change Password", 
                    value = "", 
                    icon = Icons.Default.Lock,
                    onClick = { showPasswordDialog = true }
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        item {
            Text(Localization.get("app_settings"), color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2023))
                    .border(1.dp, Color(0x333C4946), RoundedCornerShape(12.dp))
            ) {
                SettingRow(
                    title = Localization.get("language"), 
                    value = Localization.currentLanguage.label, 
                    icon = Icons.Default.Language,
                    onClick = {
                        val newLang = if (Localization.currentLanguage == Language.ENGLISH) Language.VIETNAMESE else Language.ENGLISH
                        Localization.currentLanguage = newLang
                        viewModel.updateSettings(mapOf("language" to newLang.code))
                    }
                )
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                SettingToggleRow(
                    title = Localization.get("dark_theme"), 
                    checked = SettingsManager.isDarkTheme, 
                    icon = Icons.Default.Settings, 
                    onCheckedChange = { 
                        SettingsManager.isDarkTheme = it 
                        viewModel.updateSettings(mapOf("theme" to if (it) "DARK" else "LIGHT"))
                    }
                )
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                SettingToggleRow(
                    title = Localization.get("notifications"), 
                    checked = SettingsManager.notificationsEnabled, 
                    icon = Icons.Default.Notifications, 
                    onCheckedChange = { 
                        if (!SettingsManager.notificationsEnabled) {
                            SettingsManager.notificationMessage = Localization.get("tasks_due_today")
                        }
                        SettingsManager.notificationsEnabled = it
                        viewModel.updateSettings(mapOf("notifications_enabled" to it))
                    }
                )
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                SettingToggleRow(
                    title = Localization.get("focus_reminders"), 
                    checked = true, 
                    icon = Icons.Default.AddCircle, 
                    onCheckedChange = { }
                )
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x80FFB4AB), RoundedCornerShape(8.dp))
                    .clickable { 
                        authViewModel.logout()
                        onLogout() 
                    }
                    .padding(vertical = 17.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFFFB4AB), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Localization.get("logout"), color = Color(0xFFFFB4AB), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.7.sp)
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
    
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            backgroundColor = Color(0xFF1E2023),
            title = { Text("Change Password", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current Password", color = Color.Gray) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = Color(0xFF59DBC7),
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password", color = Color.Gray) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = Color(0xFF59DBC7),
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.changePassword(oldPassword, newPassword)
                    showPasswordDialog = false
                    oldPassword = ""
                    newPassword = ""
                }) {
                    Text("Change", color = Color(0xFF59DBC7))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun BadgeItem(filled: Boolean, tint: Color, bgTint: Color) {
    val dashedBorderColor = Color(0xFF3C4946)
    Box(
        modifier = Modifier
            .size(67.dp)
            .drawBehind {
                if (!filled) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawCircle(
                        color = dashedBorderColor,
                        radius = size.minDimension / 2 - 1.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx(), pathEffect = pathEffect)
                    )
                }
            }
            .clip(CircleShape)
            .background(bgTint)
            .then(
                if (filled) Modifier.border(2.dp, tint.copy(alpha = 0.4f), CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (filled) {
            Icon(Icons.Default.Star, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        } else {
            Icon(Icons.Default.Lock, contentDescription = null, tint = dashedBorderColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingRow(title: String, value: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color(0xFFE2E2E6), fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Color(0xFF869490), fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingToggleRow(title: String, checked: Boolean, icon: ImageVector, onCheckedChange: (Boolean) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color(0xFFE2E2E6), fontSize = 16.sp)
        }
        
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 24.dp)
                .clip(RoundedCornerShape(50))
                .background(if (checked) Color(0xFF59DBC7) else Color.Gray)
                .padding(2.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF003731))
            )
        }
    }
}

@Composable
fun InfoDetailRow(title: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF59DBC7), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color(0xFFBBCAC5), fontSize = 14.sp)
        }
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EditProfileScreen(
    user: UserDto?,
    onDismiss: () -> Unit,
    onSave: (fullName: String, bio: String, gender: String, dob: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(user?.full_name ?: "") }
    var bioInput by remember { mutableStateOf(user?.bio ?: "") }
    var genderInput by remember { mutableStateOf(user?.gender ?: "Male") }
    var dobInput by remember { mutableStateOf(user?.dob ?: "") }

    var nameError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                backgroundColor = Color(0xFF111316),
                elevation = 0.dp
            )
        },
        backgroundColor = Color(0xFF111316)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2023))
                        .border(2.dp, Color(0xFF59DBC7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = nameInput,
                onValueChange = { 
                    nameInput = it 
                    nameError = it.isBlank()
                },
                label = { Text("Full Name", color = Color.Gray) },
                isError = nameError,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF59DBC7),
                    unfocusedBorderColor = Color(0xFF282A2D),
                    errorBorderColor = Color.Red
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError) {
                Text("Name cannot be empty", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedTextField(
                value = bioInput,
                onValueChange = { bioInput = it },
                label = { Text("Bio", color = Color.Gray) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF59DBC7),
                    unfocusedBorderColor = Color(0xFF282A2D)
                ),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Gender", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E2023))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Male", "Female", "Other").forEach { gender ->
                    val isSelected = genderInput.equals(gender, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF59DBC7) else Color.Transparent)
                            .clickable { genderInput = gender }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            gender,
                            color = if (isSelected) Color(0xFF003731) else Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedTextField(
                value = dobInput,
                onValueChange = { dobInput = it },
                label = { Text("Date of Birth (DD/MM/YYYY)", color = Color.Gray) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color(0xFF59DBC7),
                    unfocusedBorderColor = Color(0xFF282A2D)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        onSave(nameInput, bioInput, genderInput, dobInput)
                    } else {
                        nameError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF59DBC7)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Changes", color = Color(0xFF003731), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}