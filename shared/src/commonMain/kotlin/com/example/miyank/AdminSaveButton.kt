package com.example.miyank

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.ui.theme.*

@Composable
fun AdminSaveButton(
    text: String = "सेव करें",
    textEnglish: String = "Save Settings",
    isProcessing: Boolean = false,
    isEnabled: Boolean = true,
    isHindi: Boolean = true,
    onSaveClick: () -> Unit,
    onCancelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = SurfaceDark,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onCancelClick != null) {
                OutlinedButton(
                    onClick = onCancelClick,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                ) {
                    Text(
                        text = if (isHindi) "रद्द करें" else "Cancel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onSaveClick,
                enabled = isEnabled && !isProcessing,
                modifier = Modifier
                    .weight(if (onCancelClick != null) 0.6f else 1.0f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGolden,
                    disabledContainerColor = AccentGolden.copy(alpha = 0.3f)
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = BackgroundDark,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "सहेजा जा रहा है..." else "Saving...",
                        color = BackgroundDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) text else textEnglish,
                        color = BackgroundDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
