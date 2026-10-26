package com.example.miyank.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.store.*
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarbarStoreScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onMyOrdersClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val categories by StoreManager.categories.collectAsState()
    val cartItems by StoreManager.cart.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }

    val filteredProducts = remember(searchQuery, selectedCategoryId) {
        StoreManager.searchProducts(searchQuery, selectedCategoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "दरबार स्टोर" else "Darbar Store",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isHindi) "घर बैठे मंदिर की वस्तुएँ एवं प्रसाद मंगवाएँ" else "Temple items & Prasad at home",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
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
                    // My Orders Button
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        onMyOrdersClick()
                    }) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "My Orders", tint = WarmGold)
                    }

                    // Cart Icon with Badge
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        onCartClick()
                    }) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = WarmGold,
                                        contentColor = Color(0xFF160C05)
                                    ) {
                                        Text("$cartCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = TextWhite)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isHindi) "वस्तु खोजें (e.g. फ्रेम, प्रसाद, चालीसा...)" else "Search items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmGold,
                    unfocusedBorderColor = TextMuted.copy(alpha = 0.5f),
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // "All" chip
                item {
                    FilterChip(
                        selected = selectedCategoryId.isEmpty(),
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedCategoryId = ""
                        },
                        label = { Text(if (isHindi) "सभी" else "All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarmGold,
                            selectedLabelColor = Color(0xFF160C05),
                            containerColor = SurfaceDark,
                            labelColor = TextWhite
                        )
                    )
                }

                items(categories.filter { it.isActive }) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = {
                            haptic.playPremiumHaptic()
                            selectedCategoryId = cat.id
                        },
                        label = {
                            Text("${cat.icon} ${if (isHindi) cat.nameHindi else cat.nameEnglish}")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarmGold,
                            selectedLabelColor = Color(0xFF160C05),
                            containerColor = SurfaceDark,
                            labelColor = TextWhite
                        )
                    )
                }
            }

            // Products Grid
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛍️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isHindi) "कोई वस्तु उपलब्ध नहीं है" else "No products found",
                            fontSize = 16.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        StoreProductCard(
                            product = product,
                            isHindi = isHindi,
                            onClick = { onProductClick(product.id) },
                            onAddToCart = {
                                StoreManager.addToCart(product, 1)
                            },
                            haptic = haptic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreProductCard(
    product: ProductItem,
    isHindi: Boolean,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    haptic: com.example.miyank.HapticManager
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.playPremiumHaptic()
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(0.5.dp, WarmGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Product Image with Badge overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF1A120B))
            ) {
                AsyncImage(
                    model = product.getMainImage(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Badges
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.isNew) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SaffronAccent)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NEW", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                    if (product.isFeatured) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(WarmGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("BEST", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF160C05))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isHindi) product.name else product.nameEnglish,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.price.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmGold
                    )

                    if (product.compareAtPrice != null && product.compareAtPrice > product.price) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${product.compareAtPrice.toInt()}",
                            fontSize = 12.sp,
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (product.isAvailable()) {
                    Button(
                        onClick = {
                            haptic.playPremiumHaptic()
                            onAddToCart()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "कार्ट में जोड़ें" else "Add to Cart",
                            color = Color(0xFF160C05),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isHindi) "आउट ऑफ स्टॉक" else "Out of Stock",
                            fontSize = 11.sp,
                            color = Color(0xFFFF8A80),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
