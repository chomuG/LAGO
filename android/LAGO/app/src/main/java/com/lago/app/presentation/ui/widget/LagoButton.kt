package com.lago.app.presentation.ui.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.lago.app.presentation.theme.*

enum class LagoButtonType {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    TEXT
}

enum class LagoButtonSize {
    LARGE,
    MEDIUM,
    SMALL
}

@Composable
fun LagoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: LagoButtonType = LagoButtonType.PRIMARY,
    size: LagoButtonSize = LagoButtonSize.MEDIUM,
    enabled: Boolean = true,
    accessibilityLabel: String? = null
) {
    val height = when (size) {
        LagoButtonSize.LARGE -> 56.dp
        LagoButtonSize.MEDIUM -> 48.dp
        LagoButtonSize.SMALL -> 36.dp
    }
    
    val fontSize = when (size) {
        LagoButtonSize.LARGE -> 18.sp
        LagoButtonSize.MEDIUM -> 16.sp
        LagoButtonSize.SMALL -> 14.sp
    }
    
    val colors = when (type) {
        LagoButtonType.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = MainBlue,
            contentColor = Color.White,
            disabledContainerColor = Gray300,
            disabledContentColor = Gray600
        )
        LagoButtonType.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = MainPink,
            contentColor = Color.White,
            disabledContainerColor = Gray300,
            disabledContentColor = Gray600
        )
        LagoButtonType.OUTLINE -> ButtonDefaults.outlinedButtonColors(
            contentColor = MainBlue,
            disabledContentColor = Gray600
        )
        LagoButtonType.TEXT -> ButtonDefaults.textButtonColors(
            contentColor = MainBlue,
            disabledContentColor = Gray600
        )
    }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .semantics {
                contentDescription = accessibilityLabel ?: "$text 버튼"
            },
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}