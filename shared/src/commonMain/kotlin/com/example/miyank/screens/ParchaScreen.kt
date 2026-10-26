package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.HapticManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.ParchaItem
import com.example.miyank.ParchaManager
import com.example.miyank.ParchaRecipientType
import com.example.miyank.UserProfileManager
import com.example.miyank.formatDisplayDate
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParchaScreen(
    onParchaClick: (String) -> Unit,
    onRequestOtpVerification: () -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val isHindi = LanguageManager.currentLanguage == Language.HINDI

    val isVerifiedSession by ParchaManager.isVerifiedSession.collectAsState()
    val verifiedMobileNumber by ParchaManager.verifiedMobileNumber.collectAsState()

    val userProfile by UserProfileManager.userProfile.collectAsState()
    val userId = verifiedMobileNumber.ifEmpty { userProfile.mobileNumber.ifEmpty { "guest_user" } }

    val allParchas by ParchaManager.parchas.collectAsState()

    // Redirect to OTP Verification if unverified
    LaunchedEffect(isVerifiedSession) {
        if (!isVerifiedSession) {
            onRequestOtpVerification()
        }
    }

    val userParchas = remember(allParchas, userId) {
        ParchaManager.getParchasForUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.parcha,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = strings.yourDarbarGuidance,
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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.playPremiumHaptic()
                        ParchaManager.clearOtpSession()
                        onRequestOtpVerification()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Re-verify OTP",
                            tint = AccentGolden
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (!isVerifiedSession) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WarmGold)
            }
        } else if (userParchas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = SurfaceDark,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f)),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = WarmGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = strings.noParchaAvailable,
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHindi) 
                            "दरबार से प्राप्त आपका निजी मार्गदर्शन यहाँ उपलब्ध होगा।" 
                        else 
                            "Your personal Darbar guidance will appear here once issued.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(userParchas, key = { it.id }) { parcha ->
                    val isRead = ParchaManager.isParchaReadByUser(parcha.id, userId)
                    ParchaCardItem(
                        parcha = parcha,
                        isRead = isRead,
                        isHindi = isHindi,
                        onClick = {
                            haptic.playPremiumHaptic()
                            ParchaManager.markAsReadByUser(parcha.id, userId)
                            onParchaClick(parcha.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParchaCardItem(
    parcha: ParchaItem,
    isRead: Boolean,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    // Content is STRICTLY Hindi
    val title = parcha.title.ifBlank { "दरबार पर्चा" }
    val guidance = parcha.guidance
    val problem = parcha.problem

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            1.dp,
            if (!isRead) WarmGold else WarmGold.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Recipient Badge & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isRead) {
                        Surface(
                            color = Color.Red,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Surface(
                        color = if (parcha.recipientType == ParchaRecipientType.ALL_USERS) 
                            AccentGolden.copy(alpha = 0.15f) 
                        else 
                            WarmGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (parcha.recipientType == ParchaRecipientType.ALL_USERS) {
                                if (isHindi) "सामान्य मार्गदर्शन" else "General Guidance"
                            } else {
                                if (isHindi) "निजी पर्चा" else "Personal Parcha"
                            },
                            color = AccentGolden,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = formatDisplayDate(com.example.miyank.formatEpochToDateString(parcha.createdAt)),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title in Hindi
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!problem.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (isHindi) "समस्या" else "Concern"}: $problem",
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Guidance Preview in Hindi
            Text(
                text = guidance,
                fontSize = 13.sp,
                color = TextWhite.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Attachment & Read Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (parcha.imageUrl != null || parcha.attachmentUrl != null) {
                        Icon(
                            imageVector = Icons.Default.Attachment,
                            contentDescription = null,
                            tint = AccentGolden,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindi) "संलग्नक" else "Attachment",
                            fontSize = 11.sp,
                            color = AccentGolden
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Text(
                        text = if (isRead) {
                            if (isHindi) "✓ पढ़ा हुआ" else "✓ Read"
                        } else {
                            if (isHindi) "🔴 नया पर्चा" else "🔴 Unread"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRead) TextMuted else WarmGold
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = WarmGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
