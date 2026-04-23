package com.bfy.schedule_app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val BfyShapes = Shapes(
    // Bo góc nhẹ cho Text Field, Label nhỏ
    small = RoundedCornerShape(8.dp),
    // Bo góc vừa cho Nút bấm (Button), Card Task
    medium = RoundedCornerShape(12.dp),
    // Bo góc to cho Bottom Sheet, Dialog
    large = RoundedCornerShape(24.dp)
)