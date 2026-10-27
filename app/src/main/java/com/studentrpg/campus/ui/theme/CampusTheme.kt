package com.studentrpg.campus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun CampusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Color(0xFF355F50), secondary = Color(0xFF8C613C), tertiary = Color(0xFF6C5824),
        background = Color(0xFFF7F3EA), surface = Color(0xFFF7F3EA)
    ), content = content)
}
