package com.bfy.schedule_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bfy.schedule_app.ui.theme.*

@Composable
fun AuthHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Text(
            text = "BFY",
            color = PrimaryColor,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1.7).sp,
        )
        Text(
            text = "Begin your disciplined journey.",
            color = TextSecondary,
            fontSize = 16.sp
        )
    }
}

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelection: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(SegmentedBg)
            .padding(4.dp)
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (isSelected) SegmentedSelected else Color.Transparent)
                    .then(
                        if (isSelected) Modifier.border(1.dp, DividerColor, CircleShape)
                        else Modifier
                    )
                    .clickable { onItemSelection(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) PrimaryColor else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = BorderColor) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = SurfaceColor,
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = BorderColor,
                textColor = TextPrimary
            ),
            singleLine = true
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp)
            .height(48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor)
    ) {
        Text(
            text = text,
            color = TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(DividerColor))
        Text(
            text = text,
            color = BorderColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(DividerColor))
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(backgroundColor = SurfaceColor),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Placeholder for Google Icon
            Box(Modifier.size(20.dp).background(Color.White, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
