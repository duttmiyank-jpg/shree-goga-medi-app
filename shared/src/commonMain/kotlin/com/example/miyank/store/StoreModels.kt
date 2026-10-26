package com.example.miyank.store

import kotlinx.serialization.Serializable

/**
 * Category for Darbar Store products
 */
@Serializable
data class ProductCategory(
    val id: String = "",
    val nameHindi: String = "",
    val nameEnglish: String = "",
    val icon: String = "🛕",
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)

/**
 * Shipping calculation rules for products
 */
@Serializable
enum class ShippingChargeType {
    WEIGHT_BASED,
    FIXED,
    FREE
}

/**
 * Product Item model
 */
@Serializable
data class ProductItem(
    val id: String = "",
    val name: String = "", // Hindi / Main name
    val nameEnglish: String = "",
    val description: String = "",
    val descriptionEnglish: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val price: Double = 0.0,
    val compareAtPrice: Double? = null,
    val images: List<String> = emptyList(),
    val thumbnailUrl: String = "",
    val weightGrams: Int = 200,
    val size: String = "",
    val material: String = "",
    val stockQuantity: Int = 10,
    val isActive: Boolean = true,
    val isFeatured: Boolean = false,
    val isNew: Boolean = false,
    val tags: List<String> = emptyList(),
    val shippingChargeType: ShippingChargeType = ShippingChargeType.WEIGHT_BASED,
    val fixedShippingCharge: Double = 0.0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun getMainImage(): String {
        return images.firstOrNull() ?: thumbnailUrl
    }

    fun isAvailable(): Boolean {
        return isActive && stockQuantity > 0
    }
}

/**
 * Cart Item model
 */
@Serializable
data class CartItem(
    val product: ProductItem,
    val quantity: Int = 1
) {
    val subtotal: Double
        get() = product.price * quantity

    val totalWeightGrams: Int
        get() = product.weightGrams * quantity
}

/**
 * Customer Delivery Address
 */
@Serializable
data class DeliveryAddress(
    val id: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val houseFlat: String = "",
    val streetLocality: String = "",
    val villageArea: String = "",
    val city: String = "",
    val district: String = "",
    val state: String = "",
    val pincode: String = "",
    val landmark: String = ""
) {
    fun getFormattedAddress(): String {
        val parts = listOf(
            houseFlat,
            streetLocality,
            villageArea,
            "$city, $district",
            "$state - $pincode"
        ).filter { it.isNotBlank() }
        return parts.joinToString(", ")
    }

    fun isValid(): Boolean {
        return fullName.isNotBlank() &&
                mobileNumber.length >= 10 &&
                city.isNotBlank() &&
                pincode.length == 6
    }
}

/**
 * Order Item snapshot
 */
@Serializable
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val subtotal: Double = 0.0,
    val weightGrams: Int = 0,
    val imageUrl: String = ""
)

/**
 * Payment Status
 */
@Serializable
enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}

/**
 * Order Progress Status
 */
@Serializable
enum class OrderStatus(
    val hindiTitle: String,
    val englishTitle: String,
    val stepOrder: Int
) {
    PLACED("ऑर्डर दर्ज हुआ", "Order Placed", 1),
    PAYMENT_CONFIRMED("भुगतान सत्यापित", "Payment Confirmed", 2),
    PROCESSING("प्रक्रिया जारी", "Processing", 3),
    PACKED("पैकिंग पूर्ण", "Packed", 4),
    SHIPPED("पोस्ट / कूरियर रवाना", "Shipped", 5),
    OUT_FOR_DELIVERY("वितरण हेतु रवाना", "Out for Delivery", 6),
    DELIVERED("सफलतापूर्वक वितरित", "Delivered", 7),
    CANCELLED("निरस्त", "Cancelled", 0)
}

/**
 * Store Order model
 */
@Serializable
data class StoreOrder(
    val id: String = "",
    val userId: String = "guest_user",
    val customerName: String = "",
    val mobile: String = "",
    val address: DeliveryAddress = DeliveryAddress(),
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shippingCharge: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val orderStatus: OrderStatus = OrderStatus.PLACED,
    val paymentReference: String = "",
    val trackingNumber: String = "",
    val shippingProvider: String = "भारतीय डाक सेवा (India Post)",
    val adminNote: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
