package com.bfy.schedule_app.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bfy.schedule_app.ui.theme.PrimaryColor
import com.bfy.schedule_app.ui.theme.TextPrimary
import com.bfy.schedule_app.ui.theme.TextSecondary
import com.bfy.schedule_app.ui.viewmodel.LeaderboardViewModel
import com.bfy.schedule_app.utils.Localization

@Composable
fun LeaderboardScreen(
    userId: String,
    onBackClick: () -> Unit,
    viewModel: LeaderboardViewModel = viewModel(key = "leaderboard_$userId") { LeaderboardViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLeaderboard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111316))
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                Localization.get("leaderboard") ?: "Leaderboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = Color.Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.entries) { entry ->
                    LeaderboardItem(entry)
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(entry: com.bfy.schedule_app.data.remote.model.LeaderboardEntryDto) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFF1E2023),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${entry.rank}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (entry.rank <= 3) PrimaryColor else TextSecondary,
                modifier = Modifier.width(40.dp)
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.full_name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(entry.current_rank, color = TextSecondary, fontSize = 12.sp)
            }
            
            Text(
                "${entry.total_exp} EXP",
                color = PrimaryColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
