package com.bfy.schedule_app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*
import com.bfy.schedule_app.ui.viewmodel.CalendarViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState


@Composable
fun CalendarYearViewScreen(viewModel: CalendarViewModel = viewModel { CalendarViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate = uiState.selectedDate

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 24.dp)
    ) {
        val months = listOf(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        )
        val chunkedMonths = months.chunked(3)
        chunkedMonths.forEach { rowMonths ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowMonths.forEach { month ->
                        SmallMonthGrid(
                            monthName = month, 
                            selectedDate = selectedDate,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    repeat(3 - rowMonths.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun SmallMonthGrid(monthName: String, selectedDate: kotlinx.datetime.LocalDate, modifier: Modifier = Modifier) {

    Column(modifier = modifier.padding(end = 16.dp)) {
        Text(
            text = monthName,
            color = PrimaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        for (i in 0 until 5) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (j in 0 until 7) {
                    val dayNum = i * 7 + j + 1
                    val isHighlighted = monthName.equals(selectedDate.month.name, ignoreCase = true) && dayNum == selectedDate.dayOfMonth

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .clip(CircleShape)
                            .background(if (isHighlighted) PrimaryColor else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (i * 7 + j < 31) "${(i * 7 + j + 1)}" else "",
                            color = if (isHighlighted) TextDark else TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

