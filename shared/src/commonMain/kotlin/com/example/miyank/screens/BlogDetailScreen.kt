package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.BlogCategories
import com.example.miyank.BlogManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.formatDisplayDate
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*

@Composable
fun BlogDetailScreen(
    blogId: String,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val strings = LocalStrings.current
    val scrollState = rememberScrollState()

    var isHindiArticle by remember {
        mutableStateOf(LanguageManager.currentLanguage == Language.HINDI)
    }

    val blog = remember(blogId) {
        BlogManager.getBlogById(blogId)
    }

    if (blog == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌸", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "यह लेख उपलब्ध नहीं है।",
                    color = TextMuted,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                ) {
                    Text("वापस जाएँ", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val displayTitle = if (isHindiArticle || blog.titleEnglish.isEmpty()) blog.title else blog.titleEnglish
    val displayContent = if (isHindiArticle || blog.contentEnglish.isEmpty()) blog.content else blog.contentEnglish
    val displayCategory = BlogCategories.getDisplayName(blog.category, isHindiArticle)

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // COVER IMAGE HEADER WITH BACK & SHARE OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = blog.coverImageUrl,
                    contentDescription = displayTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    BackgroundDark
                                )
                            )
                        )
                )

                // Top Floating Actions Bar (Back & Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.playPremiumHaptic()
                            val formattedMsg = com.example.miyank.ShareManager.formatGeneralShareMessage(
                                title = displayTitle,
                                description = blog.shortDescription
                            )
                            mediaHandler.shareSuvichar(
                                imageUrlOrPath = blog.coverImageUrl,
                                dateStr = blog.publishedDate,
                                quoteText = displayTitle,
                                author = blog.author,
                                shareText = formattedMsg
                            )
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = AccentGolden
                        )
                    }
                }

                // Category Tag at Bottom of Cover
                Surface(
                    color = AccentGolden,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = displayCategory,
                        color = BackgroundDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // ARTICLE BODY CONTENT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Title
                Text(
                    text = displayTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Author Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = WarmGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatDisplayDate(blog.publishedDate),
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = "— ${blog.author}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmGold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = SurfaceDark, thickness = 2.dp)

                Spacer(modifier = Modifier.height(16.dp))

                // IN-ARTICLE WRITEUP LANGUAGE SWITCHER (DIRECTLY ABOVE CONTENT WRITEUP BODY)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindiArticle) "लेख की भाषा:" else "Article Language:",
                            fontSize = 12.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    isHindiArticle = true
                                },
                            color = if (isHindiArticle) AccentGolden else BackgroundDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isHindiArticle) AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "हिन्दी",
                                color = if (isHindiArticle) BackgroundDark else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    isHindiArticle = false
                                },
                            color = if (!isHindiArticle) AccentGolden else BackgroundDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (!isHindiArticle) AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "English",
                                color = if (!isHindiArticle) BackgroundDark else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // RICH CONTENT PARSER & RENDERER (TEXT BLOCKS + IN-CONTENT EMBEDDED IMAGES)
                RenderRichArticleContent(
                    rawContent = displayContent,
                    secondaryImageUrl = blog.contentImageUrl
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Devotional Footer Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✨ जय श्री जाहरवीर गोगा जी महाराज ✨",
                            color = AccentGolden,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✨ जय सत् गुरु बाबा सोम दत्त जी महाराज ✨",
                            color = WarmGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🙏 श्री जामनीवाला दरबार 🙏",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

fun parseImageSpec(rawSpec: String): Pair<String, String> {
    val spec = rawSpec.trim()
    if (spec.contains("|size:")) {
        val url = spec.substringBefore("|size:").trim()
        val size = spec.substringAfter("|size:").substringBefore("|").trim().lowercase()
        return Pair(url, size)
    }
    return Pair(spec, "medium")
}

@Composable
fun RenderRichArticleContent(
    rawContent: String,
    secondaryImageUrl: String
) {
    if (rawContent.isEmpty()) return

    val parts = rawContent.split("[IMAGE:")
    var secondaryImageRendered = false

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        parts.forEachIndexed { index, part ->
            if (index == 0) {
                if (part.trim().isNotEmpty()) {
                    Text(
                        text = part.trim(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextWhite,
                        lineHeight = 26.sp
                    )
                }
            } else {
                val rawSpec = part.substringBefore("]").trim()
                val remainingText = part.substringAfter("]").trim()

                if (rawSpec.isNotEmpty()) {
                    val (url, size) = parseImageSpec(rawSpec)
                    InlineArticleImageCard(url, size)
                }

                if (remainingText.isNotEmpty()) {
                    Text(
                        text = remainingText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextWhite,
                        lineHeight = 26.sp
                    )
                }
            }

            if (secondaryImageUrl.isNotEmpty() && !secondaryImageRendered && index == 0) {
                secondaryImageRendered = true
                InlineArticleImageCard(secondaryImageUrl, "medium")
            }
        }

        if (secondaryImageUrl.isNotEmpty() && !secondaryImageRendered) {
            InlineArticleImageCard(secondaryImageUrl, "medium")
        }
    }
}

@Composable
fun InlineArticleImageCard(
    imageUrlOrPath: String,
    size: String = "medium"
) {
    val cardModifier = when (size.lowercase()) {
        "small" -> Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .width(200.dp)
            .height(140.dp)
        "large", "full" -> Modifier
            .fillMaxWidth()
            .height(300.dp)
        else -> Modifier // "medium"
            .fillMaxWidth()
            .height(220.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            AsyncImage(
                model = imageUrlOrPath,
                contentDescription = "Embedded Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
