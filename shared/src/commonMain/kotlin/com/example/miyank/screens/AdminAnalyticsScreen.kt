package com.example.miyank.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminAnalyticsScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF34495E))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "App Analytics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        containerColor = Color(0xFFF4F7F6)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "Performance Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Text(
                text = "Last updated: 17 Aug, 2026",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AnalyticsStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Users",
                    value = "1,240",
                    icon = Icons.Default.Group,
                    color = Color(0xFF3498DB),
                    trend = "+12% this week"
                )
                AnalyticsStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Daily Active",
                    value = "342",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = Color(0xFF2ECC71),
                    trend = "+5% vs yesterday"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnalyticsStatCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Total Donations (Mock)",
                value = "₹45,800",
                icon = Icons.Default.Payments,
                color = Color(0xFFF1C40F),
                trend = "Growth: 8% monthly"
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Feature Popularity",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mock Chart / Progress bars for feature usage
            FeatureUsageRow("Live Darshan", 0.85f, Color(0xFFE74C3C))
            FeatureUsageRow("Bhajans", 0.65f, Color(0xFF9B59B6))
            FeatureUsageRow("Virtual Puja", 0.45f, Color(0xFFF39C12))
            FeatureUsageRow("Yatra Bookings", 0.30f, Color(0xFF1ABC9C))

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AnalyticsStatCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    trend: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF2C3E50))
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(text = trend, fontSize = 11.sp, color = if (trend.contains("+")) Color(0xFF2ECC71) else Color.Red)
        }
    }
}

@Composable
fun FeatureUsageRow(name: String, percentage: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
            Text(text = "${(percentage * 100).toInt()}%", fontSize = 13.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}
