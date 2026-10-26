package com.example.miyank.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.AdminActionGuardDialog
import com.example.miyank.AdminSaveButton
import com.example.miyank.GuardActionContext
import com.example.miyank.GuardDialogState
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberFilePickerLauncher
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberImageOptimizer
import com.example.miyank.store.*
import com.example.miyank.ui.theme.*

fun extractDirectImageUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isEmpty()) return ""

    // Extract from Google imgres
    if (trimmed.contains("google.", ignoreCase = true) && trimmed.contains("imgurl=", ignoreCase = true)) {
        val part = trimmed.substringAfter("imgurl=").substringBefore("&")
        val decoded = decodeUrlComponent(part)
        if (decoded.startsWith("http://", ignoreCase = true) || decoded.startsWith("https://", ignoreCase = true)) {
            return decoded
        }
    }
    // Extract from Google url redirect
    if (trimmed.contains("google.", ignoreCase = true) && trimmed.contains("url=", ignoreCase = true)) {
        val part = trimmed.substringAfter("url=").substringBefore("&")
        val decoded = decodeUrlComponent(part)
        if (decoded.startsWith("http://", ignoreCase = true) || decoded.startsWith("https://", ignoreCase = true)) {
            return decoded
        }
    }

    return trimmed
}

private fun decodeUrlComponent(encoded: String): String {
    return encoded
        .replace("%3A", ":", ignoreCase = true)
        .replace("%2F", "/", ignoreCase = true)
        .replace("%3F", "?", ignoreCase = true)
        .replace("%3D", "=", ignoreCase = true)
        .replace("%26", "&", ignoreCase = true)
        .replace("%25", "%", ignoreCase = true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStoreScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val imageOptimizer = rememberImageOptimizer()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val products by StoreManager.products.collectAsState()
    val categories by StoreManager.categories.collectAsState()
    val orders by StoreManager.orders.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Products, 1: Orders
    var showProductFormDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductItem?>(null) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductItem?>(null) }

    var selectedOrderForTracking by remember { mutableStateOf<StoreOrder?>(null) }
    var trackingNumberInput by remember { mutableStateOf("") }
    var orderStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val presetPhotoSamples = listOf(
        Pair("🖼️ गोगा जी फ्रेम", "file:///android_asset/gogaji_photo.jpg"),
        Pair("🍚 भभूत प्रसाद", "file:///android_asset/bhabhoot_prasad.jpg"),
        Pair("📿 मौली धागा", "file:///android_asset/raksha_sutra.jpg"),
        Pair("📖 गोगा चालीसा", "file:///android_asset/goga_chalisa_book.jpg"),
        Pair("🔥 हवन सामग्री", "file:///android_asset/hawan_samagri.jpg"),
        Pair("📅 मंदिर कैलेंडर", "file:///android_asset/mandir_calendar_2026.jpg")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "दरबार स्टोर प्रबंधन" else "Admin Store Management",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        haptic.playPremiumHaptic()
                        editingProduct = ProductItem()
                        showProductFormDialog = true
                    },
                    containerColor = WarmGold,
                    contentColor = Color(0xFF160C05)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Admin Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = WarmGold,
                divider = { HorizontalDivider(color = TextMuted.copy(alpha = 0.2f)) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { haptic.playPremiumHaptic(); selectedTab = 0 },
                    text = { Text(if (isHindi) "वस्तुएँ एवं फोटो (${products.size})" else "Products & Photos", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { haptic.playPremiumHaptic(); selectedTab = 1 },
                    text = { Text(if (isHindi) "ऑर्डर्स (${orders.size})" else "Orders", fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // PRODUCTS TAB
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(products) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Live Product Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1A120B))
                                    ) {
                                        AsyncImage(
                                            model = product.getMainImage(),
                                            contentDescription = product.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            fontSize = 15.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "₹${product.price.toInt()} • स्टॉक: ${product.stockQuantity} • श्रेणी: ${product.categoryName}",
                                            fontSize = 12.sp,
                                            color = WarmGold
                                        )
                                        Text(
                                            text = "स्थिति: ${if (product.isActive) "सक्रिय (Active)" else "निष्क्रिय"} ${if (product.isFeatured) "• Best Seller" else ""}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    IconButton(onClick = {
                                        haptic.playPremiumHaptic()
                                        editingProduct = product
                                        showProductFormDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = WarmGold)
                                    }

                                    IconButton(onClick = {
                                        haptic.playPremiumHaptic()
                                        productToDelete = product
                                        showDeleteConfirmDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373))
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // ORDERS TAB
                    val filteredOrders = if (orderStatusFilter != null) {
                        orders.filter { it.orderStatus == orderStatusFilter }
                    } else orders

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            // Status Filter Chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                FilterChip(
                                    selected = orderStatusFilter == null,
                                    onClick = { orderStatusFilter = null },
                                    label = { Text(if (isHindi) "सभी" else "All") }
                                )
                                FilterChip(
                                    selected = orderStatusFilter == OrderStatus.PAYMENT_CONFIRMED,
                                    onClick = { orderStatusFilter = OrderStatus.PAYMENT_CONFIRMED },
                                    label = { Text("सत्यापित") }
                                )
                                FilterChip(
                                    selected = orderStatusFilter == OrderStatus.SHIPPED,
                                    onClick = { orderStatusFilter = OrderStatus.SHIPPED },
                                    label = { Text("रवाना") }
                                )
                            }
                        }

                        items(filteredOrders) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                                border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#${order.id}", fontWeight = FontWeight.ExtraBold, color = WarmGold, fontSize = 15.sp)
                                        Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                                    }

                                    Text("ग्राहक: ${order.customerName} (${order.mobile})", color = TextWhite, fontSize = 13.sp)
                                    Text("पता: ${order.address.getFormattedAddress()}", color = TextMuted, fontSize = 12.sp)
                                    Text("सामग्री: ${order.items.joinToString(", ") { "${it.productName}×${it.quantity}" }}", color = TextWhite, fontSize = 12.sp)

                                    if (order.trackingNumber.isNotBlank()) {
                                        Text("📍 ट्रैकिंग नंबर: ${order.trackingNumber}", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "स्थिति: ${order.orderStatus.hindiTitle}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGolden
                                        )

                                        Button(
                                            onClick = {
                                                haptic.playPremiumHaptic()
                                                selectedOrderForTracking = order
                                                trackingNumberInput = order.trackingNumber
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (isHindi) "अपडेट करें" else "Update Status", color = Color(0xFF160C05), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog (With Gallery File Picker & Live Preview)
    if (showProductFormDialog && editingProduct != null) {
        val prod = editingProduct!!
        var nameHindi by remember { mutableStateOf(prod.name) }
        var nameEnglish by remember { mutableStateOf(prod.nameEnglish) }
        var descriptionHindi by remember { mutableStateOf(prod.description) }
        var priceStr by remember { mutableStateOf(if (prod.price > 0) prod.price.toInt().toString() else "351") }
        var stockStr by remember { mutableStateOf(prod.stockQuantity.toString()) }
        var weightStr by remember { mutableStateOf(prod.weightGrams.toString()) }
        var imageUrl by remember { mutableStateOf(prod.getMainImage().ifBlank { "file:///android_asset/gogaji_photo.jpg" }) }
        var selectedCatId by remember { mutableStateOf(prod.categoryId.ifBlank { categories.firstOrNull()?.id ?: "cat_1" }) }
        var isFeatured by remember { mutableStateOf(prod.isFeatured) }
        var isActive by remember { mutableStateOf(prod.isActive) }

        // Local Device File Picker for Gallery Photo Upload
        val filePickerLauncher = rememberFilePickerLauncher("image/*") { filePath, _, _ ->
            if (!filePath.isNullOrBlank()) {
                val optResult = imageOptimizer.processAndOptimizeImage(filePath)
                val finalPath = optResult?.optimizedFullImagePath ?: filePath
                imageUrl = finalPath
            }
        }

        AlertDialog(
            onDismissRequest = { showProductFormDialog = false },
            confirmButton = {
                AdminSaveButton(
                    isHindi = isHindi,
                    onSaveClick = {
                        val cleanImage = extractDirectImageUrl(imageUrl)
                        val catObj = categories.firstOrNull { it.id == selectedCatId }
                        val p = prod.copy(
                            name = nameHindi.ifBlank { "मंदिर वस्तु" },
                            nameEnglish = nameEnglish.ifBlank { "Temple Item" },
                            description = descriptionHindi,
                            price = priceStr.toDoubleOrNull() ?: 100.0,
                            stockQuantity = stockStr.toIntOrNull() ?: 10,
                            weightGrams = weightStr.toIntOrNull() ?: 200,
                            categoryId = selectedCatId,
                            categoryName = catObj?.nameHindi ?: "मंदिर वस्तु",
                            images = listOf(cleanImage.ifBlank { imageUrl }),
                            thumbnailUrl = cleanImage.ifBlank { imageUrl },
                            isFeatured = isFeatured,
                            isActive = isActive
                        )
                        StoreManager.saveProduct(p)
                        showProductFormDialog = false
                    },
                    onCancelClick = { showProductFormDialog = false }
                )
            },
            title = {
                Text(if (isHindi) "वस्तु विवरण एवं फोटो सेट करें" else "Product Details & Photo", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // LIVE PHOTO PREVIEW BOX
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A120B)),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = extractDirectImageUrl(imageUrl),
                                    contentDescription = "Photo Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(if (isHindi) "फोटो का लाइव प्रिव्यू" else "Photo Preview", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    // GALLERY PICKER & GOOGLE LINK AUTO-FIX BUTTONS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                haptic.playPremiumHaptic()
                                filePickerLauncher()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "📱 गैलरी / फोन से फोटो चुनें" else "Pick Photo from Phone",
                                color = Color(0xFF160C05),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (imageUrl.contains("google.", ignoreCase = true)) {
                            OutlinedButton(
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    imageUrl = extractDirectImageUrl(imageUrl)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                border = BorderStroke(1.dp, WarmGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isHindi) "⚡ Google लिंक से फोटो ऑटो-निकालें" else "Extract Photo from Google Link", color = WarmGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // PHOTO URL / PATH INPUT
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { raw ->
                            imageUrl = extractDirectImageUrl(raw)
                        },
                        label = { Text(if (isHindi) "इमेज वेब लिंक (Image URL) या पाथ" else "Product Photo URL / Path") },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = AccentGolden) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    // PRESET SAMPLE PHOTOS CHIPS ROW
                    Text(
                        text = if (isHindi) "या मंदिर नमूना फोटो चुनें:" else "Or Pick Sample Photo:",
                        fontSize = 11.sp,
                        color = AccentGolden,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetPhotoSamples) { sample ->
                            FilterChip(
                                selected = imageUrl == sample.second,
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    imageUrl = sample.second
                                },
                                label = { Text(sample.first, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarmGold,
                                    selectedLabelColor = Color(0xFF160C05),
                                    containerColor = BackgroundDark,
                                    labelColor = TextWhite
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                    OutlinedTextField(
                        value = nameHindi,
                        onValueChange = { nameHindi = it },
                        label = { Text(if (isHindi) "वस्तु का नाम (हिंदी)" else "Product Name Hindi") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = nameEnglish,
                        onValueChange = { nameEnglish = it },
                        label = { Text(if (isHindi) "वस्तु का नाम (अंग्रेजी)" else "Product Name English") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text(if (isHindi) "मूल्य (Price ₹)" else "Price (₹)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text(if (isHindi) "स्टॉक संख्या" else "Stock Qty") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text(if (isHindi) "वजन (ग्राम)" else "Weight (g)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )
                    }

                    OutlinedTextField(
                        value = descriptionHindi,
                        onValueChange = { descriptionHindi = it },
                        label = { Text(if (isHindi) "विवरण (Description)" else "Description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it }, colors = CheckboxDefaults.colors(checkedColor = WarmGold))
                        Text(if (isHindi) "Best Seller / Featured" else "Best Seller", color = TextWhite, fontSize = 13.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isActive, onCheckedChange = { isActive = it }, colors = CheckboxDefaults.colors(checkedColor = WarmGold))
                        Text(if (isHindi) "सक्रिय (Active Store)" else "Active in Store", color = TextWhite, fontSize = 13.sp)
                    }
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete Product Guard Dialog
    if (showDeleteConfirmDialog && productToDelete != null) {
        AdminActionGuardDialog(
            state = GuardDialogState(
                isVisible = true,
                context = GuardActionContext.DELETE_ITEM,
                title = if (isHindi) "वस्तु हटाएं?" else "Delete Product?",
                message = if (isHindi) "क्या आप वाकई '${productToDelete?.name}' को स्टोर से हटाना चाहते हैं?" else "Are you sure you want to delete '${productToDelete?.name}'?",
                itemTitle = productToDelete?.name ?: "",
                onConfirm = {
                    StoreManager.deleteProduct(productToDelete!!.id)
                    showDeleteConfirmDialog = false
                },
                onDismiss = { showDeleteConfirmDialog = false }
            ),
            isHindi = isHindi
        )
    }

    // Update Order Status & India Post Tracking Dialog
    if (selectedOrderForTracking != null) {
        val ord = selectedOrderForTracking!!
        var status by remember { mutableStateOf(ord.orderStatus) }

        AlertDialog(
            onDismissRequest = { selectedOrderForTracking = null },
            confirmButton = {
                AdminSaveButton(
                    isHindi = isHindi,
                    onSaveClick = {
                        StoreManager.updateOrderStatus(
                            orderId = ord.id,
                            newStatus = status,
                            trackingNumber = trackingNumberInput
                        )
                        selectedOrderForTracking = null
                    },
                    onCancelClick = { selectedOrderForTracking = null }
                )
            },
            title = {
                Text(if (isHindi) "ऑर्डर #${ord.id} अपडेट करें" else "Update Order #${ord.id}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (isHindi) "पोस्ट ऑफिस ट्रैकिंग नंबर दर्ज करें:" else "Enter India Post Tracking Number:", color = AccentGolden, fontSize = 13.sp)

                    OutlinedTextField(
                        value = trackingNumberInput,
                        onValueChange = { trackingNumberInput = it },
                        placeholder = { Text("e.g. IN100293847IN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                    )

                    Text(if (isHindi) "ऑर्डर स्थिति चुनें:" else "Select Order Status:", color = AccentGolden, fontSize = 13.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            OrderStatus.PAYMENT_CONFIRMED,
                            OrderStatus.PROCESSING,
                            OrderStatus.PACKED,
                            OrderStatus.SHIPPED,
                            OrderStatus.DELIVERED,
                            OrderStatus.CANCELLED
                        ).forEach { st ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { status = st }
                            ) {
                                RadioButton(selected = status == st, onClick = { status = st }, colors = RadioButtonDefaults.colors(selectedColor = WarmGold))
                                Text(st.hindiTitle, color = TextWhite, fontSize = 13.sp)
                            }
                        }
                    }
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
