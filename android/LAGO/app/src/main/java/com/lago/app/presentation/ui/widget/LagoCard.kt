package com.lago.app.presentation.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.lago.app.presentation.theme.*

@Composable
fun LagoCard(
    modifier: Modifier = Modifier,
    elevation: ElevationLevel = ElevationLevel.LEVEL1,
    backgroundColor: Color = Color.White,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    accessibilityLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .semantics {
                accessibilityLabel?.let {
                    contentDescription = it
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.value
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

enum class ElevationLevel(val value: androidx.compose.ui.unit.Dp) {
    LEVEL0(0.dp),
    LEVEL1(2.dp),
    LEVEL2(4.dp),
    LEVEL3(8.dp),
    LEVEL4(16.dp)
}