package com.bfy.schedule_app.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bfy.schedule_app.ui.model.MainTab

data class BottomNavItem(
    val tab: MainTab,
    val label: String
)

@Composable
fun BfyBottomNav(
    items: List<BottomNavItem>,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.tab,
                onClick = { onTabSelected(item.tab) },
                icon = { Text(if (selectedTab == item.tab) "●" else "○") },
                label = { Text(item.label) }
            )
        }
    }
}
