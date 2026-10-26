package com.example.miyank

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.ui.theme.*

enum class GuardActionContext {
    UNSAVED_CHANGES,
    DELETE_ITEM,
    PUBLISH_MAJOR_CHANGE,
    BROADCAST_NOTIFICATION,
    CLEAR_HISTORY,
    RESET_SETTINGS
}

data class GuardDialogState(
    val isVisible: Boolean = false,
    val context: GuardActionContext = GuardActionContext.UNSAVED_CHANGES,
    val title: String = "",
    val message: String = "",
    val itemTitle: String = "",
    val itemCategory: String = "",
    val itemImageUrl: String = "",
    val payloadPreview: String = "",
    val onConfirm: () -> Unit = {},
    val onDiscard: (() -> Unit)? = null,
    val onDismiss: () -> Unit = {}
)

@Composable
fun AdminActionGuardDialog(
    state: GuardDialogState,
    isHindi: Boolean = true
) {
    if (!state.isVisible) return

    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) state.onDismiss()
        },
        containerColor = SurfaceDark,
        titleContentColor = when (state.context) {
            GuardActionContext.DELETE_ITEM,
            GuardActionContext.CLEAR_HISTORY,
            GuardActionContext.RESET_SETTINGS -> Color.Red
            else -> AccentGolden
        },
        textContentColor = TextWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.context == GuardActionContext.DELETE_ITEM ||
                    state.context == GuardActionContext.CLEAR_HISTORY ||
                    state.context == GuardActionContext.RESET_SETTINGS) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = state.title.ifEmpty {
                        when (state.context) {
                            GuardActionContext.UNSAVED_CHANGES -> if (isHindi) "बिना सेव किए बाहर निकलें?" else "Unsaved Changes"
                            GuardActionContext.DELETE_ITEM -> if (isHindi) "हटाने की पुष्टि करें" else "Confirm Deletion"
                            GuardActionContext.PUBLISH_MAJOR_CHANGE -> if (isHindi) "पब्लिश करने की पुष्टि करें" else "Confirm Publication"
                            GuardActionContext.BROADCAST_NOTIFICATION -> if (isHindi) "ब्रॉडकास्ट की पुष्टि करें" else "Confirm Notification Broadcast"
                            GuardActionContext.CLEAR_HISTORY -> if (isHindi) "इतिहास साफ़ करें?" else "Clear History?"
                            GuardActionContext.RESET_SETTINGS -> if (isHindi) "सेटिंग्स रीसेट करें?" else "Reset Settings?"
                        }
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.message.ifEmpty {
                        when (state.context) {
                            GuardActionContext.UNSAVED_CHANGES -> if (isHindi) "आपके बदलाव अभी सेव नहीं हुए हैं। यदि आप बाहर निकलते हैं, तो आपके बदलाव खो जाएंगे।" else "You have unsaved changes. If you leave without saving, your edits will be lost."
                            GuardActionContext.DELETE_ITEM -> if (isHindi) "⚠️ क्या आप वाकई इस सामग्री को स्थायी रूप से हटाना चाहते हैं? यह कार्रवाई वापस नहीं की जा सकती।" else "⚠️ Are you sure you want to permanently delete this item? This action cannot be undone."
                            GuardActionContext.PUBLISH_MAJOR_CHANGE -> if (isHindi) "क्या आप इन बदलावों को सेव और प्रकाशित करना चाहते हैं?" else "Are you sure you want to save and publish these changes?"
                            GuardActionContext.BROADCAST_NOTIFICATION -> if (isHindi) "क्या आप यह Push Notification सभी श्रद्धालुओं को भेजना चाहते हैं?" else "Are you sure you want to broadcast this notification to all users?"
                            GuardActionContext.CLEAR_HISTORY -> if (isHindi) "यह सभी संबंधित रिकॉर्ड स्थायी रूप से हटा देगा।" else "This will permanently remove all related history records."
                            GuardActionContext.RESET_SETTINGS -> if (isHindi) "सभी सेटिंग्स उनके डिफ़ॉल्ट मान पर वापस चली जाएँगी।" else "All settings will be restored to default values."
                        }
                    },
                    color = TextWhite,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                // ITEM PREVIEW (THUMBNAIL / DETAILS)
                if (state.itemTitle.isNotEmpty() || state.itemImageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.itemImageUrl.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    AsyncImage(
                                        model = state.itemImageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                if (state.itemCategory.isNotEmpty()) {
                                    Text(
                                        text = state.itemCategory,
                                        fontSize = 10.sp,
                                        color = AccentGolden,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = state.itemTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                if (state.payloadPreview.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = BackgroundDark,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.payloadPreview,
                            color = TextWhite.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isProcessing) {
                        isProcessing = true
                        state.onConfirm()
                        isProcessing = false
                    }
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (state.context) {
                        GuardActionContext.DELETE_ITEM,
                        GuardActionContext.CLEAR_HISTORY,
                        GuardActionContext.RESET_SETTINGS -> Color.Red
                        else -> AccentGolden
                    }
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundDark)
                } else {
                    Text(
                        text = when (state.context) {
                            GuardActionContext.UNSAVED_CHANGES -> if (isHindi) "सेव करके बाहर जाएँ" else "Save & Leave"
                            GuardActionContext.DELETE_ITEM -> if (isHindi) "हाँ, हटाएँ" else "Yes, Delete"
                            GuardActionContext.PUBLISH_MAJOR_CHANGE -> if (isHindi) "हाँ, सेव करें" else "Yes, Save Settings"
                            GuardActionContext.BROADCAST_NOTIFICATION -> if (isHindi) "सभी को भेजें" else "Broadcast Now"
                            GuardActionContext.CLEAR_HISTORY -> if (isHindi) "हाँ, साफ़ करें" else "Yes, Clear"
                            GuardActionContext.RESET_SETTINGS -> if (isHindi) "हाँ, रीसेट करें" else "Yes, Reset"
                        },
                        color = if (state.context == GuardActionContext.DELETE_ITEM || state.context == GuardActionContext.CLEAR_HISTORY || state.context == GuardActionContext.RESET_SETTINGS) Color.White else BackgroundDark,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        },
        dismissButton = {
            Row {
                if (state.context == GuardActionContext.UNSAVED_CHANGES && state.onDiscard != null) {
                    TextButton(onClick = {
                        state.onDiscard.invoke()
                    }) {
                        Text(
                            text = if (isHindi) "बिना सेव किए जाएँ" else "Discard & Leave",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextButton(onClick = { state.onDismiss() }) {
                    Text(
                        text = when (state.context) {
                            GuardActionContext.UNSAVED_CHANGES -> if (isHindi) "यहीं रहें" else "Stay & Edit"
                            else -> if (isHindi) "रद्द करें" else "Cancel"
                        },
                        color = TextMuted
                    )
                }
            }
        }
    )
}
