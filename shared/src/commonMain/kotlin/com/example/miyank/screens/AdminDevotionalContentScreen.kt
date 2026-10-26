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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.DevotionalCategories
import com.example.miyank.DevotionalContentItem
import com.example.miyank.DevotionalContentManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDevotionalContentScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val itemsList by DevotionalContentManager.items.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) } // null = ALL

    val filteredList = remember(itemsList, selectedCategory) {
        if (selectedCategory == null) itemsList else itemsList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    var editingItem by remember { mutableStateOf<DevotionalContentItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isHindi) "चालीसा व कथा साहित्य प्रबंधन" else "Devotional Library Admin",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = if (isHindi) "चालीसा, स्तोत्रम एवं कथाएं जोड़ें/संपादित करें" else "Manage chalisa, stotram & katha library",
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
                    selectedTabIndex = if (selectedCategory == null) 0 else DevotionalCategories.list.indexOf(selectedCategory) + 1,
                    containerColor = SurfaceDark,
                    contentColor = WarmGold,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedCategory == null,
                        onClick = { haptic.playPremiumHaptic(); selectedCategory = null },
                        text = { Text(if (isHindi) "सभी (${itemsList.size})" else "All", color = if (selectedCategory == null) WarmGold else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    DevotionalCategories.list.forEach { catKey ->
                        val count = itemsList.count { it.category.equals(catKey, ignoreCase = true) }
                        Tab(
                            selected = selectedCategory == catKey,
                            onClick = { haptic.playPremiumHaptic(); selectedCategory = catKey },
                            text = { Text("${DevotionalCategories.getDisplayName(catKey, isHindi)} ($count)", color = if (selectedCategory == catKey) WarmGold else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.playPremiumHaptic()
                    editingItem = DevotionalContentItem()
                    showEditDialog = true
                },
                containerColor = WarmGold,
                contentColor = Color(0xFF160C05)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
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
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = WarmGold, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isHindi) "कोई साहित्य सामग्री उपलब्ध नहीं है। नीचे '+' पर क्लिक करके जोड़ें।" else "No devotional content found. Click '+' to add.",
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
                        AdminDevotionalCard(
                            item = item,
                            isHindi = isHindi,
                            onEdit = {
                                editingItem = item
                                showEditDialog = true
                            },
                            onDelete = {
                                DevotionalContentManager.deleteItem(item.id)
                            }
                        )
                    }
                }
            }
        }

        if (showEditDialog && editingItem != null) {
            DevotionalContentEditDialog(
                item = editingItem!!,
                isHindi = isHindi,
                onDismiss = { showEditDialog = false },
                onSave = { updated ->
                    DevotionalContentManager.addOrUpdateItem(updated)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
private fun AdminDevotionalCard(
    item: DevotionalContentItem,
    isHindi: Boolean,
    onEdit: () -> Unit,
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
                Text(text = item.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite, modifier = Modifier.weight(1f))
                Surface(color = WarmGold.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                    Text(text = DevotionalCategories.getDisplayName(item.category, isHindi), color = WarmGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.contentHindi, fontSize = 12.sp, color = TextMuted, maxLines = 3)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WarmGold)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A80))
                }
            }
        }
    }
}

@Composable
private fun DevotionalContentEditDialog(
    item: DevotionalContentItem,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSave: (DevotionalContentItem) -> Unit
) {
    var titleInput by remember { mutableStateOf(item.title) }
    var selectedCat by remember { mutableStateOf(item.category) }
    var contentHindiInput by remember { mutableStateOf(item.contentHindi) }
    var contentEnglishInput by remember { mutableStateOf(item.contentEnglish) }
    var authorInput by remember { mutableStateOf(item.author) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item.id.isEmpty()) (if (isHindi) "नया साहित्य जोड़ें" else "Add Devotional Content") else (if (isHindi) "साहित्य संपादित करें" else "Edit Content"), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("शीर्षक (Title) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(if (isHindi) "श्रेणी चुनें:" else "Category:", fontSize = 12.sp, color = TextWhite)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DevotionalCategories.list.forEach { cat ->
                        FilterChip(
                            selected = selectedCat == cat,
                            onClick = { selectedCat = cat },
                            label = { Text(DevotionalCategories.getDisplayName(cat, isHindi), fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = contentHindiInput,
                    onValueChange = { contentHindiInput = it },
                    label = { Text("हिंदी पाठ (Hindi Content) *") },
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )

                OutlinedTextField(
                    value = contentEnglishInput,
                    onValueChange = { contentEnglishInput = it },
                    label = { Text("अंग्रेजी अनुवाद (English Text - Optional)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                OutlinedTextField(
                    value = authorInput,
                    onValueChange = { authorInput = it },
                    label = { Text("लेखक/स्रोत (Author/Source)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.trim().isNotEmpty() && contentHindiInput.trim().isNotEmpty()) {
                        onSave(
                            item.copy(
                                title = titleInput.trim(),
                                category = selectedCat,
                                contentHindi = contentHindiInput.trim(),
                                contentEnglish = contentEnglishInput.trim(),
                                author = authorInput.trim().ifEmpty { "श्री जामनीवाला दरबार" }
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold)
            ) {
                Text(if (isHindi) "सेव करें" else "Save", color = Color(0xFF160C05), fontWeight = FontWeight.Bold)
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
