package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
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
import com.example.miyank.BlogItem
import com.example.miyank.BlogManager
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.LocalStrings
import com.example.miyank.formatDisplayDate
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@Composable
fun BlogListingScreen(
    onBlogClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val strings = LocalStrings.current
    val allBlogs by BlogManager.blogs.collectAsState()

    var isHindiScreen by remember {
        mutableStateOf(LanguageManager.currentLanguage == Language.HINDI)
    }

    var selectedCategoryKey by remember { mutableStateOf(BlogCategories.ALL) }

    val publishedBlogs = remember(allBlogs) {
        allBlogs.filter { it.isPublished }
    }

    val featuredBlog = remember(publishedBlogs) {
        publishedBlogs.firstOrNull { it.isFeatured } ?: publishedBlogs.firstOrNull()
    }

    val filteredBlogs = remember(publishedBlogs, selectedCategoryKey, featuredBlog) {
        val list = if (selectedCategoryKey == BlogCategories.ALL) {
            publishedBlogs
        } else {
            publishedBlogs.filter { BlogCategories.matchesCategory(it.category, selectedCategoryKey) }
        }
        if (selectedCategoryKey == BlogCategories.ALL && featuredBlog != null) {
            list.filter { it.id != featuredBlog.id }
        } else {
            list
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isHindiScreen) "दरबार के विषय में" else "About Darbar",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }

                    // TOP RADIO BUTTON LANGUAGE SWITCHER (HINDI / ENGLISH)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    isHindiScreen = true
                                },
                            color = if (isHindiScreen) AccentGolden else BackgroundDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isHindiScreen) AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "हिन्दी",
                                color = if (isHindiScreen) BackgroundDark else TextWhite,
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
                                    isHindiScreen = false
                                },
                            color = if (!isHindiScreen) AccentGolden else BackgroundDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (!isHindiScreen) AccentGolden else WarmGold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "English",
                                color = if (!isHindiScreen) BackgroundDark else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. FEATURED ARTICLE CARD (TOP HIGHLIGHT)
            if (selectedCategoryKey == BlogCategories.ALL && featuredBlog != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindiScreen) "मुख्य जानकारी" else "Featured Information",
                                color = AccentGolden,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FeaturedBlogCard(
                            blog = featuredBlog,
                            isHindi = isHindiScreen,
                            readArticleText = if (isHindiScreen) "पढ़ें ➔" else "Read Article ➔",
                            onClick = {
                                haptic.playPremiumHaptic()
                                onBlogClick(featuredBlog.id)
                            }
                        )
                    }
                }
            }

            // 2. HORIZONTAL CATEGORY BLOCKS / PILLS (NO EMOJIS, BILINGUAL)
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = if (isHindiScreen) "विषय श्रेणियाँ" else "Categories",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(BlogCategories.list) { catKey ->
                            val isSelected = selectedCategoryKey == catKey
                            val displayName = BlogCategories.getDisplayName(catKey, isHindiScreen)
                            CategoryBlockPill(
                                categoryName = displayName,
                                isSelected = isSelected,
                                onClick = {
                                    haptic.playPremiumHaptic()
                                    selectedCategoryKey = catKey
                                }
                            )
                        }
                    }
                }
            }

            // 3. ARTICLES LIST SECTION
            if (filteredBlogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌸", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isHindiScreen) "इस श्रेणी में अभी कोई जानकारी उपलब्ध नहीं है।" else "No articles available in this category yet.",
                                fontSize = 14.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredBlogs) { blog ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BlogCardItem(
                            blog = blog,
                            isHindi = isHindiScreen,
                            readArticleText = if (isHindiScreen) "पढ़ें ➔" else "Read Article ➔",
                            onClick = {
                                haptic.playPremiumHaptic()
                                onBlogClick(blog.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBlockPill(
    categoryName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) AccentGolden else SurfaceDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = categoryName,
            color = if (isSelected) BackgroundDark else TextWhite,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun FeaturedBlogCard(
    blog: BlogItem,
    isHindi: Boolean,
    readArticleText: String,
    onClick: () -> Unit
) {
    val displayTitle = if (isHindi || blog.titleEnglish.isEmpty()) blog.title else blog.titleEnglish
    val displayDesc = if (isHindi || blog.shortDescriptionEnglish.isEmpty()) blog.shortDescription else blog.shortDescriptionEnglish
    val displayCat = BlogCategories.getDisplayName(blog.category, isHindi)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp,
                    Brush.verticalGradient(listOf(WarmGold, SaffronAccent)),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = blog.coverImageUrl,
                        contentDescription = displayTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )

                    // Category Tag at Top
                    Surface(
                        color = Color.Black.copy(alpha = 0.70f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = displayCat,
                            color = AccentGolden,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = displayTitle,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = displayDesc,
                        fontSize = 13.sp,
                        color = TextMuted,
                        maxLines = 2,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = WarmGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formatDisplayDate(blog.publishedDate),
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Surface(
                            color = AccentGolden,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = readArticleText,
                                    color = BackgroundDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlogCardItem(
    blog: BlogItem,
    isHindi: Boolean,
    readArticleText: String,
    onClick: () -> Unit
) {
    val displayTitle = if (isHindi || blog.titleEnglish.isEmpty()) blog.title else blog.titleEnglish
    val displayCat = BlogCategories.getDisplayName(blog.category, isHindi)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(88.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                AsyncImage(
                    model = blog.coverImageUrl,
                    contentDescription = displayTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = AccentGolden.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = displayCat,
                        color = AccentGolden,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = displayTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 2,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDisplayDate(blog.publishedDate),
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = readArticleText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGolden
                        )
                    }
                }
            }
        }
    }
}
