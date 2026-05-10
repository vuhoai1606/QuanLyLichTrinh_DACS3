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

@Composable
fun ProfileScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111316))
            .padding(horizontal = 24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) }

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
                        // Avatar Container with gradient border
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
                            Text("Alex Rivers", color = Color(0xFFE2E2E6), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x33AD7BFF))
                                    .border(1.dp, Color(0x4DAD7BFF), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Text("GOLD SCHOLAR", color = Color(0xFFAD7BFF), fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Level and EXP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("LVL 42", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("1,240", color = Color(0xFF59DBC7), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("/ 2000 EXP", color = Color(0xFF869490), fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 3.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x1A59DBC7))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.62f) // 1240/2000
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF59DBC7), Color(0xFFD5BAFF))))
                        )
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        // Section - Focus Stats (Bento Layout)
        item {
            Text("FOCUS STATS", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth().height(156.dp)) {
                // Total Focus Time Card
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
                            Text("142h", color = Color(0xFFE2E2E6), fontSize = 34.sp, fontWeight = FontWeight.Bold)
                            Text("Total Focus Time", color = Color(0xFF869490), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Stacked Cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Day Streak
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
                                Text("14", color = Color(0xFFE2E2E6), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFFFB4AB), modifier = Modifier.size(16.dp))
                            }
                            Text("Day Streak", color = Color(0xFF869490), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Daily Avg
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
                            Text("4.2h", color = Color(0xFFE2E2E6), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                            Text("Daily Avg", color = Color(0xFF869490), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        // Section - Badge Board
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("BADGE BOARD", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)
                Text("View All", color = Color(0xFF59DBC7), fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BadgeItem(filled = true, tint = Color(0xFF59DBC7), bgTint = Color(0x1A59DBC7))
                        BadgeItem(filled = true, tint = Color(0xFFD5BAFF), bgTint = Color(0x1AD5BAFF))
                        BadgeItem(filled = true, tint = Color(0xFFAEC6FF), bgTint = Color(0x1AAEC6FF))
                        BadgeItem(filled = false, tint = Color.Gray, bgTint = Color(0xFF282A2D))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BadgeItem(filled = false, tint = Color.Gray, bgTint = Color(0xFF282A2D))
                        BadgeItem(filled = false, tint = Color.Gray, bgTint = Color(0xFF282A2D))
                        BadgeItem(filled = false, tint = Color.Gray, bgTint = Color(0xFF282A2D))
                        BadgeItem(filled = false, tint = Color.Gray, bgTint = Color(0xFF282A2D))
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        // Section - App Settings
        item {
            Text("APP SETTINGS", color = Color(0xFFBBCAC5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 4.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2023))
                    .border(1.dp, Color(0x333C4946), RoundedCornerShape(12.dp))
            ) {
                SettingRow(title = "Language", value = "English", icon = Icons.Default.Build)
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                SettingToggleRow(title = "Dark Theme", checked = true, icon = Icons.Default.Settings)
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                SettingToggleRow(title = "Notifications", checked = false, icon = Icons.Default.Notifications)
                Divider(color = Color(0x1A3C4946), thickness = 1.dp)
                SettingToggleRow(title = "Focus Reminders", checked = true, icon = Icons.Default.AddCircle)
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        // Logout Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x80FFB4AB), RoundedCornerShape(8.dp))
                    .clickable { /* Logout action */ }
                    .padding(vertical = 17.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFFFB4AB), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOGOUT", color = Color(0xFFFFB4AB), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.7.sp)
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
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
fun SettingRow(title: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* action */ }
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
fun SettingToggleRow(title: String, checked: Boolean, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                .clickable { /* action toggle */ }
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