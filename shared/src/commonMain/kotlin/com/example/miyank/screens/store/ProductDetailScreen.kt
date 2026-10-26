package com.example.miyank.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.ShareManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.store.*
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onCartClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val products by StoreManager.products.collectAsState()
    val product = products.firstOrNull { it.id == productId } ?: StoreManager.products.value.firstOrNull()

    var quantity by remember { mutableStateOf(1) }
    var selectedImageIndex by remember { mutableStateOf(0) }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found", color = TextWhite)
        }
        return
    }

    val images = if (product.images.isNotEmpty()) product.images else listOf(product.thumbnailUrl)
    val currentImage = images.getOrElse(selectedImageIndex) { product.getMainImage() }

    val shareText = ShareManager.formatGeneralShareMessage(
        title = product.name,
        description = "${product.description}\n\nमूल्य: ₹${product.price.toInt()}"
    )

    fun handleShare() {
        haptic.playPremiumHaptic()
        mediaHandler.shareSuvichar(
            imageUrlOrPath = currentImage,
            dateStr = "",
            quoteText = product.name,
            author = "श्री जामनीवाला दरबार स्टोर",
            shareText = shareText
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isHindi) "वस्तु विवरण" else "Product Details",
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
                actions = {
                    IconButton(onClick = { handleShare() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = WarmGold)
                    }
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        onCartClick()
                    }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        bottomBar = {
            if (product.isAvailable()) {
                Surface(
                    color = SurfaceDark,
                    tonalElevation = 8.dp,
                    border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptic.playPremiumHaptic()
                                StoreManager.addToCart(product, quantity)
                                onCartClick()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            border = BorderStroke(1.dp, WarmGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = WarmGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "कार्ट में जोड़ें" else "Add to Cart",
                                color = WarmGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                haptic.playPremiumHaptic()
                                StoreManager.addToCart(product, quantity)
                                onBuyNowClick()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isHindi) "अभी खरीदें" else "Buy Now",
                                color = Color(0xFF160C05),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Image Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF1A120B))
            ) {
                AsyncImage(
                    model = currentImage,
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Thumbnail Gallery if multiple
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    images.forEachIndexed { index, img ->
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.5.dp,
                                    if (selectedImageIndex == index) WarmGold else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    selectedImageIndex = index
                                }
                        ) {
                            AsyncImage(
                                model = img,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title & Category
                Column {
                    Text(
                        text = product.categoryName.ifBlank { if (isHindi) "मंदिर वस्तु" else "Temple Item" },
                        fontSize = 12.sp,
                        color = AccentGolden,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) product.name else product.nameEnglish,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                // Price Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.price.toInt()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmGold
                    )
                    if (product.compareAtPrice != null && product.compareAtPrice > product.price) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "₹${product.compareAtPrice.toInt()}",
                            fontSize = 16.sp,
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                    }
                }

                HorizontalDivider(color = TextMuted.copy(alpha = 0.2f))

                // Quantity Selector Row
                if (product.isAvailable()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "मात्रा (Quantity):" else "Quantity:",
                            fontSize = 15.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceDark)
                                .border(0.5.dp, WarmGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        ) {
                            IconButton(onClick = {
                                if (quantity > 1) {
                                    haptic.playPremiumHaptic()
                                    quantity--
                                }
                            }) {
                                Text("-", fontSize = 20.sp, color = WarmGold, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "$quantity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            IconButton(onClick = {
                                if (quantity < product.stockQuantity) {
                                    haptic.playPremiumHaptic()
                                    quantity++
                                }
                            }) {
                                Text("+", fontSize = 20.sp, color = WarmGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Red.copy(alpha = 0.15f))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHindi) "⚠️ यह वस्तु अभी स्टॉक में उपलब्ध नहीं है।" else "⚠️ Currently Out of Stock.",
                            color = Color(0xFFFF8A80),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Specs Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (isHindi) "वस्तु विवरण (Specifications)" else "Specifications", fontWeight = FontWeight.Bold, color = AccentGolden, fontSize = 13.sp)
                        if (product.weightGrams > 0) {
                            Text("• ${if (isHindi) "वजन" else "Weight"}: ${product.weightGrams} ग्राम", color = TextMuted, fontSize = 12.sp)
                        }
                        if (product.size.isNotBlank()) {
                            Text("• ${if (isHindi) "माप / आकार" else "Size"}: ${product.size}", color = TextMuted, fontSize = 12.sp)
                        }
                        if (product.material.isNotBlank()) {
                            Text("• ${if (isHindi) "सामग्री / धातु" else "Material"}: ${product.material}", color = TextMuted, fontSize = 12.sp)
                        }
                        Text("• ${if (isHindi) "डिलीवरी" else "Shipping"}: भारतीय डाक सेवा (Post Office Delivery)", color = TextMuted, fontSize = 12.sp)
                    }
                }

                // Description
                Column {
                    Text(
                        text = if (isHindi) "विवरण" else "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGolden
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isHindi) product.description else product.descriptionEnglish,
                        fontSize = 14.sp,
                        color = TextWhite,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
