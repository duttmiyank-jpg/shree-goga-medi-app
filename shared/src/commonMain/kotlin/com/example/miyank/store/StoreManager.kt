package com.example.miyank.store

import com.example.miyank.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object StoreManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_CATEGORIES_JSON = "store_categories_json_v1"
    private const val KEY_PRODUCTS_JSON = "store_products_json_v1"
    private const val KEY_CART_JSON = "store_cart_json_v1"
    private const val KEY_ADDRESS_JSON = "store_address_json_v1"
    private const val KEY_ORDERS_JSON = "store_orders_json_v1"

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    // State Flows
    private val _categories = MutableStateFlow<List<ProductCategory>>(emptyList())
    val categories: StateFlow<List<ProductCategory>> = _categories

    private val _products = MutableStateFlow<List<ProductItem>>(emptyList())
    val products: StateFlow<List<ProductItem>> = _products

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    private val _savedAddress = MutableStateFlow(DeliveryAddress())
    val savedAddress: StateFlow<DeliveryAddress> = _savedAddress

    private val _orders = MutableStateFlow<List<StoreOrder>>(emptyList())
    val orders: StateFlow<List<StoreOrder>> = _orders

    init {
        _categories.value = loadCategories()
        _products.value = loadProducts()
        _cart.value = loadCart()
        _savedAddress.value = loadAddress()
        _orders.value = loadOrders()
    }

    // --- CATEGORIES MANAGEMENT ---
    private fun loadCategories(): List<ProductCategory> {
        val saved = settingsStorage.getString(KEY_CATEGORIES_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultCategories()
    }

    private fun getDefaultCategories(): List<ProductCategory> {
        return listOf(
            ProductCategory("cat_1", "भगवान जी की तस्वीरें", "Deity Frames & Photos", "🖼️", 1),
            ProductCategory("cat_2", "पूजा सामग्री", "Puja Items", "🙏", 2),
            ProductCategory("cat_3", "हवन सामग्री", "Hawan Samagri", "🔥", 3),
            ProductCategory("cat_4", "धागे / रक्षा सूत्र", "Sacred Threads", "📿", 4),
            ProductCategory("cat_5", "प्रसाद", "Prasad", "🍚", 5),
            ProductCategory("cat_6", "चालीसा एवं स्तोत्र", "Chalisa & Stotra", "📖", 6),
            ProductCategory("cat_7", "धार्मिक पुस्तकें", "Religious Books", "📚", 7),
            ProductCategory("cat_8", "मंदिर कैलेंडर", "Mandir Calendar", "📅", 8),
            ProductCategory("cat_9", "घड़ी एवं अन्य वस्तुएँ", "Clocks & Decor", "⏰", 9),
            ProductCategory("cat_10", "अन्य", "Other Items", "🛍️", 10)
        )
    }

    fun saveCategories(newCategories: List<ProductCategory>) {
        _categories.value = newCategories
        try {
            settingsStorage.setString(KEY_CATEGORIES_JSON, json.encodeToString(newCategories))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- PRODUCTS MANAGEMENT ---
    private fun loadProducts(): List<ProductItem> {
        val saved = settingsStorage.getString(KEY_PRODUCTS_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultProducts()
    }

    private fun getDefaultProducts(): List<ProductItem> {
        val now = 1758000000000L
        return listOf(
            ProductItem(
                id = "prod_1",
                name = "श्री बाबा जाहरवीर गोगा जी महाराज दिव्य फ्रेम",
                nameEnglish = "Shree Baba Goga Ji Maharaj Frame",
                description = "श्री जामनीवाला दरबार से अभिमंत्रित एवं आशीर्वाद प्राप्त सुंदर लकड़ी की फोटो फ्रेम। घर तथा पूजा स्थल हेतु अत्यंत शुभ।",
                descriptionEnglish = "Blessed wooden frame from Shree Jamniwala Darbar. Highly auspicious for home & temple.",
                categoryId = "cat_1",
                categoryName = "भगवान जी की तस्वीरें",
                price = 351.0,
                compareAtPrice = 450.0,
                images = listOf("file:///android_asset/gogaji_photo.jpg"),
                weightGrams = 450,
                size = "8x10 इंच",
                material = "लकड़ी व ग्लास",
                stockQuantity = 25,
                isActive = true,
                isFeatured = true,
                isNew = true,
                tags = listOf("गोगा जी", "फ्रेम", "फोटो", "goga ji", "frame"),
                createdAt = now,
                updatedAt = now
            ),
            ProductItem(
                id = "prod_2",
                name = "दरबार पवित्र बाबूट / भभूत (प्रसाद)",
                nameEnglish = "Holy Bhabhoot / Sacred Ash Prasad",
                description = "श्री जामनीवाला दरबार की अखण्ड ज्योति एवं हवन कुण्ड से प्राप्त पवित्र भभूत प्रसाद।",
                descriptionEnglish = "Holy ash prasad from the eternal akhand jyoti of Shree Jamniwala Darbar.",
                categoryId = "cat_5",
                categoryName = "प्रसाद",
                price = 51.0,
                compareAtPrice = 100.0,
                images = listOf("file:///android_asset/bhabhoot_prasad.jpg"),
                weightGrams = 100,
                size = "100 ग्राम पैकेट",
                material = "पवित्र भस्म",
                stockQuantity = 100,
                isActive = true,
                isFeatured = true,
                isNew = false,
                tags = listOf("भभूत", "बाबूट", "प्रसाद", "prasad", "bhabhoot"),
                createdAt = now,
                updatedAt = now
            ),
            ProductItem(
                id = "prod_3",
                name = "सिद्ध रक्षा सूत्र मौली / धागा",
                nameEnglish = "Siddha Raksha Sutra Sacred Thread",
                description = "दरबार में सिद्ध किया हुआ लाल-पीला पवित्र रक्षा सूत्र। सुरक्षा एवं शुभ ऊर्जा हेतु।",
                descriptionEnglish = "Blessed red-yellow sacred thread for protection and divine energy.",
                categoryId = "cat_4",
                categoryName = "धागे / रक्षा सूत्र",
                price = 31.0,
                images = listOf("file:///android_asset/raksha_sutra.jpg"),
                weightGrams = 50,
                size = "10 मीटर रोल",
                material = "शुद्ध सूती धागा",
                stockQuantity = 200,
                isActive = true,
                isFeatured = true,
                tags = listOf("धागा", "मौली", "रक्षा सूत्र", "mooli", "thread"),
                createdAt = now,
                updatedAt = now
            ),
            ProductItem(
                id = "prod_4",
                name = "श्री गोगा चालीसा एवं आरती पुस्तक",
                nameEnglish = "Shree Goga Chalisa & Aarti Book",
                description = "श्री जाहरवीर गोगा जी महाराज की सम्पूर्ण चालीसा, आरती व स्तुति संग्रह (शुद्ध देवनागरी)।",
                descriptionEnglish = "Complete Chalisa, Aarti, and Stuti collection of Baba Goga Ji Maharaj.",
                categoryId = "cat_6",
                categoryName = "चालीसा एवं स्तोत्र",
                price = 41.0,
                images = listOf("file:///android_asset/goga_chalisa_book.jpg"),
                weightGrams = 150,
                size = "पाकेट साइज",
                material = "कागज",
                stockQuantity = 50,
                isActive = true,
                isFeatured = false,
                tags = listOf("चालीसा", "आरती", "पुस्तक", "chalisa", "book"),
                createdAt = now,
                updatedAt = now
            ),
            ProductItem(
                id = "prod_5",
                name = "विशेष सुगंधित हवन सामग्री एवं धूप",
                nameEnglish = "Special Aromatic Hawan Samagri",
                description = "108 जड़ी-बूटियों एवं शुद्ध गूगल-लोबान से निर्मित विशेष हवन सामग्री।",
                descriptionEnglish = "Aromatic Hawan Samagri crafted with 108 sacred herbs and pure guggul.",
                categoryId = "cat_3",
                categoryName = "हवन सामग्री",
                price = 251.0,
                compareAtPrice = 300.0,
                images = listOf("file:///android_asset/hawan_samagri.jpg"),
                weightGrams = 500,
                size = "500 ग्राम",
                material = "जड़ी-बूटी व गूगल",
                stockQuantity = 40,
                isActive = true,
                isFeatured = true,
                tags = listOf("हवन", "धूप", "सामग्री", "hawan", "samagri"),
                createdAt = now,
                updatedAt = now
            ),
            ProductItem(
                id = "prod_6",
                name = "श्री जामनीवाला दरबार वार्षिक कैलेंडर 2026",
                nameEnglish = "Shree Jamniwala Darbar Annual Calendar 2026",
                description = "मंदिर के सभी प्रमुख त्यौहारों, एकादशी, पूर्णिमा व मेलों की तिथियों सहित सुंदर दीवार कैलेंडर।",
                descriptionEnglish = "Wall calendar featuring all temple festivals, fasts, and auspicious dates.",
                categoryId = "cat_8",
                categoryName = "मंदिर कैलेंडर",
                price = 101.0,
                images = listOf("file:///android_asset/mandir_calendar_2026.jpg"),
                weightGrams = 200,
                size = "12x18 इंच",
                material = "आर्ट पेपर",
                stockQuantity = 60,
                isActive = true,
                isFeatured = true,
                tags = listOf("कैलेंडर", "2026", "calendar"),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    fun saveProduct(product: ProductItem) {
        val current = _products.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == product.id }
        if (existingIndex >= 0) {
            current[existingIndex] = product.copy(updatedAt = 1758000000000L)
        } else {
            val newId = if (product.id.isBlank()) "prod_${1758000000000L}" else product.id
            current.add(0, product.copy(id = newId, createdAt = 1758000000000L, updatedAt = 1758000000000L))
        }
        _products.value = current
        try {
            settingsStorage.setString(KEY_PRODUCTS_JSON, json.encodeToString(current))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteProduct(productId: String) {
        val updated = _products.value.filterNot { it.id == productId }
        _products.value = updated
        try {
            settingsStorage.setString(KEY_PRODUCTS_JSON, json.encodeToString(updated))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun searchProducts(query: String, categoryId: String = ""): List<ProductItem> {
        val activeProducts = _products.value.filter { it.isActive }
        val q = query.trim().lowercase()

        return activeProducts.filter { product ->
            val matchesCategory = categoryId.isBlank() || product.categoryId == categoryId
            val matchesQuery = q.isBlank() ||
                    product.name.lowercase().contains(q) ||
                    product.nameEnglish.lowercase().contains(q) ||
                    product.description.lowercase().contains(q) ||
                    product.descriptionEnglish.lowercase().contains(q) ||
                    product.categoryName.lowercase().contains(q) ||
                    product.tags.any { it.lowercase().contains(q) }

            matchesCategory && matchesQuery
        }
    }

    fun getFeaturedProducts(): List<ProductItem> {
        return _products.value.filter { it.isActive && it.isFeatured }
    }

    // --- CART MANAGEMENT ---
    private fun loadCart(): List<CartItem> {
        val saved = settingsStorage.getString(KEY_CART_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    private fun persistCart(cartList: List<CartItem>) {
        _cart.value = cartList
        try {
            settingsStorage.setString(KEY_CART_JSON, json.encodeToString(cartList))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addToCart(product: ProductItem, quantity: Int = 1) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = current[index]
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stockQuantity)
            current[index] = existing.copy(quantity = newQty)
        } else {
            val qty = quantity.coerceAtMost(product.stockQuantity)
            current.add(CartItem(product = product, quantity = qty))
        }
        persistCart(current)
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = current[index]
            val validQty = quantity.coerceAtMost(existing.product.stockQuantity)
            current[index] = existing.copy(quantity = validQty)
            persistCart(current)
        }
    }

    fun removeFromCart(productId: String) {
        val updated = _cart.value.filterNot { it.product.id == productId }
        persistCart(updated)
    }

    fun clearCart() {
        persistCart(emptyList())
    }

    // --- SHIPPING CALCULATION ENGINE ---
    fun calculateShippingCharge(cartItems: List<CartItem>): Double {
        if (cartItems.isEmpty()) return 0.0

        // Check if any item has fixed shipping or all items are weight-based
        val fixedItems = cartItems.filter { it.product.shippingChargeType == ShippingChargeType.FIXED }
        val fixedTotal = fixedItems.sumOf { it.product.fixedShippingCharge * it.quantity }

        val weightItems = cartItems.filter { it.product.shippingChargeType == ShippingChargeType.WEIGHT_BASED }
        val totalWeightGrams = weightItems.sumOf { it.totalWeightGrams }

        val weightShipping = when {
            totalWeightGrams <= 0 -> 0.0
            totalWeightGrams <= 500 -> 60.0
            totalWeightGrams <= 1000 -> 90.0
            totalWeightGrams <= 2000 -> 130.0
            totalWeightGrams <= 5000 -> 200.0
            else -> 300.0
        }

        return fixedTotal + weightShipping
    }

    // --- ADDRESS MANAGEMENT ---
    private fun loadAddress(): DeliveryAddress {
        val saved = settingsStorage.getString(KEY_ADDRESS_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return DeliveryAddress()
    }

    fun saveAddress(address: DeliveryAddress) {
        _savedAddress.value = address
        try {
            settingsStorage.setString(KEY_ADDRESS_JSON, json.encodeToString(address))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- ORDERS MANAGEMENT ---
    private fun loadOrders(): List<StoreOrder> {
        val saved = settingsStorage.getString(KEY_ORDERS_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    private fun persistOrders(ordersList: List<StoreOrder>) {
        _orders.value = ordersList
        try {
            settingsStorage.setString(KEY_ORDERS_JSON, json.encodeToString(ordersList))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createPendingOrder(
        address: DeliveryAddress,
        cartItems: List<CartItem>
    ): StoreOrder {
        val now = 1758000000000L
        val randomSuffix = (1000..9999).random()
        val orderId = "SJD-$randomSuffix"

        val orderItems = cartItems.map {
            OrderItem(
                productId = it.product.id,
                productName = it.product.name,
                quantity = it.quantity,
                unitPrice = it.product.price,
                subtotal = it.subtotal,
                weightGrams = it.totalWeightGrams,
                imageUrl = it.product.getMainImage()
            )
        }

        val subtotal = cartItems.sumOf { it.subtotal }
        val shipping = calculateShippingCharge(cartItems)
        val totalAmount = subtotal + shipping

        val order = StoreOrder(
            id = orderId,
            userId = "devotee_user",
            customerName = address.fullName,
            mobile = address.mobileNumber,
            address = address,
            items = orderItems,
            subtotal = subtotal,
            shippingCharge = shipping,
            totalAmount = totalAmount,
            paymentStatus = PaymentStatus.PENDING,
            orderStatus = OrderStatus.PLACED,
            shippingProvider = "भारतीय डाक सेवा (India Post)",
            createdAt = now,
            updatedAt = now
        )

        val currentOrders = _orders.value.toMutableList()
        currentOrders.add(0, order)
        persistOrders(currentOrders)
        return order
    }

    fun confirmOrderPayment(orderId: String, paymentReference: String): StoreOrder? {
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            val existing = currentOrders[index]
            val updated = existing.copy(
                paymentStatus = PaymentStatus.SUCCESS,
                orderStatus = OrderStatus.PAYMENT_CONFIRMED,
                paymentReference = paymentReference,
                updatedAt = 1758000000000L
            )
            currentOrders[index] = updated
            persistOrders(currentOrders)

            // Deduct product inventory stock safely
            deductStockForOrder(updated)
            clearCart()
            return updated
        }
        return null
    }

    private fun deductStockForOrder(order: StoreOrder) {
        val currentProducts = _products.value.toMutableList()
        var modified = false
        order.items.forEach { item ->
            val pIndex = currentProducts.indexOfFirst { it.id == item.productId }
            if (pIndex >= 0) {
                val p = currentProducts[pIndex]
                val newStock = (p.stockQuantity - item.quantity).coerceAtLeast(0)
                currentProducts[pIndex] = p.copy(stockQuantity = newStock)
                modified = true
            }
        }
        if (modified) {
            _products.value = currentProducts
            try {
                settingsStorage.setString(KEY_PRODUCTS_JSON, json.encodeToString(currentProducts))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        trackingNumber: String = "",
        provider: String = "",
        adminNote: String = ""
    ) {
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }
        if (index >= 0) {
            val existing = currentOrders[index]
            val updated = existing.copy(
                orderStatus = newStatus,
                trackingNumber = trackingNumber.ifBlank { existing.trackingNumber },
                shippingProvider = provider.ifBlank { existing.shippingProvider },
                adminNote = adminNote.ifBlank { existing.adminNote },
                updatedAt = 1758000000000L
            )
            currentOrders[index] = updated
            persistOrders(currentOrders)
        }
    }

    fun deleteOrder(orderId: String) {
        val currentOrders = _orders.value.filterNot { it.id == orderId }
        persistOrders(currentOrders)
    }
}
