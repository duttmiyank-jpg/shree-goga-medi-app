package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.SuggestionItem
import com.example.miyank.SuggestionManager
import com.example.miyank.SuggestionStatus
import com.example.miyank.SuggestionType
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSuggestionsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val suggestions by SuggestionManager.suggestions.collectAsState()
    var selectedFilterType by remember { mutableStateOf<SuggestionType?>(null) } // null = ALL

    val filteredList = remember(suggestions, selectedFilterType) {
        if (selectedFilterType == null) suggestions else suggestions.filter { it.type == selectedFilterType }
    }

    var selectedItemForStatus by remember { mutableStateOf<SuggestionItem?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isHindi) "सुझाव व शिकायत प्रबंधन" else "Suggestions & Complaints Admin",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "श्रद्धालुओं की प्रविष्टियाँ देखें व उत्तर दें" else "View user feedback & complaints",
                                fontSize = 11.sp,
                                color = WarmGold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark, titleContentColor = TextWhite, navigationIconContentColor = TextWhite)
                )

                ScrollableTabRow(
                    selectedTabIndex = if (selectedFilterType == null) 0 else SuggestionType.entries.indexOf(selectedFilterType) + 1,
                    containerColor = SurfaceDark,
                    contentColor = WarmGold,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedFilterType == null,
                        onClick = { haptic.playPremiumHaptic(); selectedFilterType = null },
                        text = { Text(if (isHindi) "सभी (${suggestions.size})" else "All", color = if (selectedFilterType == null) WarmGold else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    SuggestionType.entries.forEach { st ->
                        val count = suggestions.count { it.type == st }
                        Tab(
                            selected = selectedFilterType == st,
                            onClick = { haptic.playPremiumHaptic(); selectedFilterType = st },
                            text = { Text("${st.getDisplayName(isHindi)} ($count)", color = if (selectedFilterType == st) WarmGold else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.RateReview, contentDescription = null, tint = WarmGold, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isHindi) "कोई सुझाव या शिकायत प्राप्त नहीं हुई है।" else "No suggestions or complaints found.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredList) { item ->
                        AdminSuggestionCard(
                            item = item,
                            isHindi = isHindi,
                            onUpdateStatus = {
                                selectedItemForStatus = item
                            },
                            onDelete = {
                                SuggestionManager.deleteSuggestion(item.id)
                            }
                        )
                    }
                }
            }
        }

        if (selectedItemForStatus != null) {
            AdminSuggestionStatusDialog(
                item = selectedItemForStatus!!,
                isHindi = isHindi,
                onDismiss = { selectedItemForStatus = null },
                onSaveStatus = { newStatus, response ->
                    SuggestionManager.updateStatus(selectedItemForStatus!!.id, newStatus, response)
                    selectedItemForStatus = null
                }
            )
        }
    }
}

@Composable
private fun AdminSuggestionCard(
    item: SuggestionItem,
    isHindi: Boolean,
    onUpdateStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = WarmGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.type.getDisplayName(isHindi) + " • " + item.category,
                        color = WarmGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = when (item.status) {
                        SuggestionStatus.RESOLVED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        SuggestionStatus.IN_PROGRESS -> WarmGold.copy(alpha = 0.2f)
                        else -> TextMuted.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.status.getDisplayName(isHindi),
                        color = when (item.status) {
                            SuggestionStatus.RESOLVED -> Color(0xFF81C784)
                            SuggestionStatus.IN_PROGRESS -> WarmGold
                            else -> TextWhite
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = item.subject, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.description, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "प्रेषक: ${item.userName} (${item.userMobile})", fontSize = 11.sp, color = WarmGold.copy(alpha = 0.8f))

            if (item.adminResponse.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = BackgroundDark,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "प्रशासन उत्तर: ${item.adminResponse}",
                        color = Color(0xFF81C784),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUpdateStatus,
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isHindi) "स्थिति/उत्तर बदलें" else "Update Status", color = Color(0xFF160C05), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A80))
                }
            }
        }
    }
}

@Composable
private fun AdminSuggestionStatusDialog(
    item: SuggestionItem,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSaveStatus: (SuggestionStatus, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(item.status) }
    var responseInput by remember { mutableStateOf(item.adminResponse) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isHindi) "स्थिति एवं उत्तर अपडेट करें" else "Update Status & Reply", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "विषय: ${item.subject}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmGold)

                Text(text = if (isHindi) "नई स्थिति चुनें:" else "Select New Status:", fontSize = 12.sp, color = TextWhite)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionStatus.entries.forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                colors = RadioButtonDefaults.colors(selectedColor = WarmGold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = st.getDisplayName(isHindi), color = TextWhite, fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = responseInput,
                    onValueChange = { responseInput = it },
                    label = { Text(if (isHindi) "प्रशासकीय टिप्पणी / उत्तर" else "Admin Reply Note") },
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveStatus(selectedStatus, responseInput) },
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
            ) {
                Text(if (isHindi) "अपडेट करें" else "Update", color = Color(0xFF160C05), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel", color = TextMuted)
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp)
    )
}
