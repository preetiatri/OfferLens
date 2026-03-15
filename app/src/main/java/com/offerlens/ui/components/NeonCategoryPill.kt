package com.offerlens.ui.components

import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offerlens.ui.theme.NeonCyan
import com.offerlens.ui.theme.CyanTeal
import com.offerlens.ui.theme.ElectricPurple
import com.offerlens.ui.theme.PurpleDeep

@Composable
fun NeonCategoryPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val activeColor = if (isDark) NeonCyan else CyanTeal
    val inactiveColor = if (isDark) Color.Gray else Color.Gray
    val secondaryColor = if (isDark) ElectricPurple else PurpleDeep

    val borderColor = if (isSelected) activeColor else inactiveColor.copy(alpha = 0.3f)
    val backgroundColor = if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent
    
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = if (isSelected) Brush.linearGradient(listOf(activeColor, secondaryColor)) else SolidColor(borderColor),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
