package com.pushtofinance.infinapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AvatarPalette = listOf(
    Color(0xFF16A34A), Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFFDB2777),
    Color(0xFFEA580C), Color(0xFF0891B2), Color(0xFF65A30D), Color(0xFFDC2626),
    Color(0xFF9333EA), Color(0xFF0D9488), Color(0xFF4F46E5), Color(0xFFB45309)
)

fun Color.toStoredColor(): Long = this.toArgb().toLong()

fun Long.toEntityColor(): Color =
    if ((this and 0xFFFFFFFFL) == 0L) Color((this ushr 32).toInt())
    else Color(this.toInt())

@Composable
fun IconAvatar(
    letter: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    fontSize: TextUnit = 18.sp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.take(2).uppercase(),
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}