package com.bfy.schedule_app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*

enum class CalendarViewType(val label: String) { 
    DAY("Day"), 
    WEEK("Week"), 
    MONTH("Month"), 
    YEAR("Year") 
}

@Composable
fun CalendarScreen() {
    var viewType by remember { mutableStateOf(CalendarViewType.MONTH) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "October 2024",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                Icon(
                    Icons.Default.KeyboardArrowLeft, 
                    contentDescription = "Previous", 
                    tint = TextSecondary,
                    modifier = Modifier.clickable { /* Prev */ }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    Icons.Default.KeyboardArrowRight, 
                    contentDescription = "Next", 
                    tint = TextSecondary,
                    modifier = Modifier.clickable { /* Next */ }
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
            CalendarViewType.DAY -> CalendarDayViewScreen()
            CalendarViewType.WEEK -> CalendarWeekViewScreen()
            CalendarViewType.MONTH -> CalendarFullMonthViewScreen()
            CalendarViewType.YEAR -> CalendarYearViewScreen()
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
                    text = option.label,
                    color = if (isSelected) Color(0xFF003731) else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


