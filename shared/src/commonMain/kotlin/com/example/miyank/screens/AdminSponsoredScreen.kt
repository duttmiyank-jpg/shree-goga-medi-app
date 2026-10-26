package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.SponsoredItem
import com.example.miyank.SponsoredManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSponsoredScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val promotions by SponsoredManager.sponsoredItems.collectAsState()
    var editingItem by remember { mutableStateOf<SponsoredItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "स्पॉन्सर विज्ञापन प्रबंधन" else "Sponsored Business Admin",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "स्थानीय व्यापार एवं प्रचार जोड़ें/संपादित करें" else "Manage local business promos",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.playPremiumHaptic()
                    editingItem = SponsoredItem()
                    showEditDialog = true
                },
                containerColor = WarmGold,
                contentColor = Color(0xFF160C05)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Sponsor")
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (promotions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = WarmGold, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isHindi) "कोई स्पॉन्सर विज्ञापन नहीं है। नीचे '+' पर क्लिक करके जोड़ें।" else "No sponsored ads found. Click '+' to add.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(promotions) { item ->
                        AdminSponsoredCard(
                            item = item,
                            isHindi = isHindi,
                            onEdit = {
                                editingItem = item
                                showEditDialog = true
                            },
                            onToggleActive = {
                                SponsoredManager.toggleActive(item.id)
                            },
                            onDelete = {
                                SponsoredManager.deletePromotion(item.id)
                            }
                        )
                    }
                }
            }
        }

        if (showEditDialog && editingItem != null) {
            SponsoredEditDialog(
                item = editingItem!!,
                isHindi = isHindi,
                onDismiss = { showEditDialog = false },
                onSave = { updated ->
                    SponsoredManager.addOrUpdatePromotion(updated)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
private fun AdminSponsoredCard(
    item: SponsoredItem,
    isHindi: Boolean,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, if (item.isActive) WarmGold else TextMuted.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                    Text(text = item.category + if (item.phone.isNotEmpty()) " • Phone: ${item.phone}" else "", fontSize = 11.sp, color = WarmGold)
                }
                Switch(
                    checked = item.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(checkedThumbColor = WarmGold, checkedTrackColor = WarmGold.copy(alpha = 0.4f))
                )
            }

            if (item.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = item.description, fontSize = 12.sp, color = TextMuted, maxLines = 2)
            }

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
private fun SponsoredEditDialog(
    item: SponsoredItem,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSave: (SponsoredItem) -> Unit
) {
    var titleInput by remember { mutableStateOf(item.title) }
    var categoryInput by remember { mutableStateOf(item.category) }
    var descInput by remember { mutableStateOf(item.description) }
    var imageInput by remember { mutableStateOf(item.imageUrl) }
    var addressInput by remember { mutableStateOf(item.address) }
    var phoneInput by remember { mutableStateOf(item.phone) }
    var waInput by remember { mutableStateOf(item.whatsapp) }
    var webInput by remember { mutableStateOf(item.websiteUrl) }
    var offerInput by remember { mutableStateOf(item.offerText) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item.id.isEmpty()) (if (isHindi) "नया स्पॉन्सर जोड़ें" else "Add New Sponsor") else (if (isHindi) "स्पॉन्सर संपादित करें" else "Edit Sponsor"),
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("व्यापार नाम (Business Title) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("श्रेणी (Category e.g. मिष्ठान, होटल, इलेक्ट्रॉनिक्स)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("विवरण (Description)") },
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
                OutlinedTextField(
                    value = imageInput,
                    onValueChange = { imageInput = it },
                    label = { Text("बैनर/इमेज URL (Image URL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("पता (Location/Address)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("फ़ोन नंबर (Contact Phone)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = waInput,
                    onValueChange = { waInput = it },
                    label = { Text("व्हाट्सएप नंबर (WhatsApp Number)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = webInput,
                    onValueChange = { webInput = it },
                    label = { Text("वेबसाइट / सोशल लिंक (Website URL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = offerInput,
                    onValueChange = { offerInput = it },
                    label = { Text("विशेष ऑफर (Offer Text e.g. 10% OFF)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.trim().isNotEmpty()) {
                        onSave(
                            item.copy(
                                title = titleInput.trim(),
                                category = categoryInput.trim().ifEmpty { "Local Business" },
                                description = descInput.trim(),
                                imageUrl = imageInput.trim(),
                                address = addressInput.trim(),
                                phone = phoneInput.trim(),
                                whatsapp = waInput.trim(),
                                websiteUrl = webInput.trim(),
                                offerText = offerInput.trim()
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
