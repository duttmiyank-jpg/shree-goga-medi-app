package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.miyank.BhajanCategories
import com.example.miyank.BhajanItem
import com.example.miyank.BhajanManager
import com.example.miyank.CentralAudioCoordinator
import com.example.miyank.HapticManager
import com.example.miyank.LanguageManager
import com.example.miyank.MusicManager
import com.example.miyank.rememberAudioPlayer
import com.example.miyank.rememberHapticManager
import com.example.miyank.rememberSuvicharMediaHandler
import com.example.miyank.ui.theme.*
import kotlinx.coroutines.delay

fun formatTimeMs(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BhajansScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val mediaHandler = rememberSuvicharMediaHandler()
    val audioPlayer = rememberAudioPlayer()
    val isHindi = LanguageManager.currentLanguage == com.example.miyank.Language.HINDI

    val activeBhajansState by BhajanManager.bhajans.collectAsState()
    val favoriteIdsState by BhajanManager.favoriteIds.collectAsState()
    val recentIdsState by BhajanManager.recentIds.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Audio Bhajans, 1: Lyrics
    var selectedCategoryFilter by remember { mutableStateOf(BhajanCategories.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedLyricItem by remember { mutableStateOf<BhajanItem?>(null) }
    var showFullScreenPlayer by remember { mutableStateOf(false) }

    val filteredBhajans = remember(activeBhajansState, favoriteIdsState, recentIdsState, searchQuery, selectedCategoryFilter) {
        activeBhajansState.filter { item ->
            val matchesCategory = when (selectedCategoryFilter) {
                BhajanCategories.ALL -> true
                BhajanCategories.FAVORITES -> favoriteIdsState.contains(item.id)
                BhajanCategories.RECENT -> recentIdsState.contains(item.id)
                else -> item.categoryTag.equals(selectedCategoryFilter, ignoreCase = true)
            }

            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.titleEnglish.contains(searchQuery, ignoreCase = true) ||
                    item.lyricsText.contains(searchQuery, ignoreCase = true) ||
                    item.artist.contains(searchQuery, ignoreCase = true)

            item.isActive && matchesCategory && matchesSearch
        }
    }

    var currentBhajan by remember { mutableStateOf<BhajanItem?>(activeBhajansState.firstOrNull()) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosMs by remember { mutableStateOf(0L) }
    var totalDurationMs by remember { mutableStateOf(320000L) }

    var isRepeat by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }

    var lyricsFontSize by remember { mutableStateOf(16.sp) }

    // REAL-TIME AUDIO PLAYBACK & POSITION POLLING LOOP
    LaunchedEffect(isPlaying, currentBhajan) {
        if (isPlaying && currentBhajan != null) {
            CentralAudioCoordinator.playBhajanTrack(currentBhajan!!, filteredBhajans)

            while (isPlaying) {
                currentPosMs = audioPlayer.getCurrentPosition()
                val dur = audioPlayer.getDuration()
                if (dur > 0L) totalDurationMs = dur
                CentralAudioCoordinator.updateProgress(currentPosMs, totalDurationMs)
                delay(200)
            }
        } else {
            audioPlayer.pause()
            MusicManager.isBhajanActivePlaying.value = false
        }
    }

    // IMMEDIATELY PAUSE APP DEFAULT BACKGROUND MUSIC ON BHAJAN SCREEN ENTRY
    DisposableEffect(Unit) {
        audioPlayer.stopDefaultMusic()
        CentralAudioCoordinator.pauseDefaultAppMusic()
        onDispose {
            if (!isPlaying) {
                MusicManager.setOtherMediaPlaying(false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AccentGolden,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "भजन, आरती एवं लिरिक्स" else "Bhajans & Lyrics",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        bottomBar = {
            if (currentBhajan != null) {
                BhajanMiniPlayer(
                    bhajan = currentBhajan!!,
                    isPlaying = isPlaying,
                    onTogglePlay = {
                        haptic.playPremiumHaptic()
                        isPlaying = !isPlaying
                    },
                    onOpenFullScreen = {
                        haptic.playPremiumHaptic()
                        showFullScreenPlayer = true
                    },
                    haptic = haptic
                )
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // DUAL TABS & SEARCH BAR
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.playPremiumHaptic()
                                selectedTab = 0
                            },
                        color = if (selectedTab == 0) AccentGolden else BackgroundDark,
                        border = BorderStroke(1.dp, if (selectedTab == 0) AccentGolden else WarmGold.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isHindi) "🎵 भजन ऑडियो" else "🎵 Audio Bhajans",
                            color = if (selectedTab == 0) BackgroundDark else TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.playPremiumHaptic()
                                selectedTab = 1
                            },
                        color = if (selectedTab == 1) AccentGolden else BackgroundDark,
                        border = BorderStroke(1.dp, if (selectedTab == 1) AccentGolden else WarmGold.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isHindi) "📖 भजन लिरिक्स" else "📖 Bhajan Lyrics",
                            color = if (selectedTab == 1) BackgroundDark else TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (isHindi) "भजन, आरती या लिरिक्स खोजें..." else "Search Bhajans or Lyrics...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGolden,
                        unfocusedBorderColor = WarmGold.copy(alpha = 0.3f),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Compact Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(BhajanCategories.filterList) { catKey ->
                        val isSelected = selectedCategoryFilter == catKey
                        val displayName = BhajanCategories.getDisplayName(catKey, isHindi)

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.playPremiumHaptic()
                                    selectedCategoryFilter = catKey
                                },
                            color = if (isSelected) AccentGolden else BackgroundDark,
                            border = BorderStroke(1.dp, if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = displayName,
                                color = if (isSelected) BackgroundDark else TextWhite,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (filteredBhajans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHindi) "🌸 कोई भजन उपलब्ध नहीं है।" else "🌸 No bhajans found.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBhajans, key = { it.id }) { item ->
                        val isFav = favoriteIdsState.contains(item.id)

                        BhajanListItemCard(
                            item = item,
                            isSelected = currentBhajan?.id == item.id,
                            isFavorite = isFav,
                            isHindi = isHindi,
                            onPlayClick = {
                                haptic.playPremiumHaptic()
                                currentBhajan = item
                                isPlaying = true
                                BhajanManager.addRecentPlayed(item.id)
                            },
                            onLyricsClick = {
                                haptic.playPremiumHaptic()
                                selectedLyricItem = item
                            },
                            onFavoriteClick = {
                                haptic.playPremiumHaptic()
                                BhajanManager.toggleFavorite(item.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // FULL SCREEN AUDIO PLAYER DIALOG WITH WORKING SEEK & TIME UPDATES
    if (showFullScreenPlayer && currentBhajan != null) {
        FullScreenAudioPlayerDialog(
            bhajan = currentBhajan!!,
            isPlaying = isPlaying,
            currentPosMs = currentPosMs,
            totalDurationMs = totalDurationMs,
            isRepeat = isRepeat,
            isShuffle = isShuffle,
            isFavorite = favoriteIdsState.contains(currentBhajan!!.id),
            isHindi = isHindi,
            onDismiss = { showFullScreenPlayer = false },
            onTogglePlay = {
                haptic.playPremiumHaptic()
                isPlaying = !isPlaying
            },
            onSeekToMs = { targetMs ->
                currentPosMs = targetMs
                audioPlayer.seekTo(targetMs)
            },
            onNextTrack = {
                if (activeBhajansState.isNotEmpty() && currentBhajan != null) {
                    val currIdx = activeBhajansState.indexOfFirst { it.id == currentBhajan!!.id }
                    val nextIdx = (currIdx + 1) % activeBhajansState.size
                    currentBhajan = activeBhajansState[nextIdx]
                    isPlaying = true
                }
            },
            onPreviousTrack = {
                if (activeBhajansState.isNotEmpty() && currentBhajan != null) {
                    val currIdx = activeBhajansState.indexOfFirst { it.id == currentBhajan!!.id }
                    val prevIdx = if (currIdx - 1 < 0) activeBhajansState.size - 1 else currIdx - 1
                    currentBhajan = activeBhajansState[prevIdx]
                    isPlaying = true
                }
            },
            onToggleRepeat = {
                haptic.playPremiumHaptic()
                isRepeat = !isRepeat
            },
            onToggleShuffle = {
                haptic.playPremiumHaptic()
                isShuffle = !isShuffle
            },
            onToggleFavorite = {
                haptic.playPremiumHaptic()
                BhajanManager.toggleFavorite(currentBhajan!!.id)
            },
            onOpenLyrics = {
                if (currentBhajan!!.lyricsText.isNotEmpty()) {
                    showFullScreenPlayer = false
                    selectedLyricItem = currentBhajan
                }
            }
        )
    }

    // FULL SCREEN LYRICS READER DIALOG
    selectedLyricItem?.let { lyric ->
        BhajanLyricsReaderDialog(
            item = lyric,
            fontSize = lyricsFontSize,
            isHindi = isHindi,
            onDismiss = { selectedLyricItem = null },
            onFontSizeChange = { lyricsFontSize = it },
            onPlayAudio = {
                selectedLyricItem = null
                currentBhajan = lyric
                isPlaying = true
                showFullScreenPlayer = true
            }
        )
    }
}

@Composable
fun BhajanListItemCard(
    item: BhajanItem,
    isSelected: Boolean,
    isFavorite: Boolean,
    isHindi: Boolean,
    onPlayClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentGolden.copy(alpha = 0.15f) else SurfaceDark
        ),
        border = BorderStroke(1.dp, if (isSelected) AccentGolden else WarmGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = item.thumbnailUrl.ifEmpty { "file:///android_asset/temple_pooja.jpg" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isHindi || item.titleEnglish.isEmpty()) item.title else item.titleEnglish,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isHindi || item.artistEnglish.isEmpty()) item.artist else item.artistEnglish,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavoriteClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (item.lyricsText.isNotEmpty()) {
                    IconButton(onClick = onLyricsClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Lyrics",
                            tint = AccentGolden,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BhajanMiniPlayer(
    bhajan: BhajanItem,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onOpenFullScreen: () -> Unit,
    haptic: HapticManager
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onOpenFullScreen() },
        color = SurfaceDark,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = bhajan.thumbnailUrl.ifEmpty { "file:///android_asset/temple_pooja.jpg" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bhajan.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bhajan.artist,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(38.dp),
                    shape = CircleShape,
                    color = AccentGolden
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = onOpenFullScreen, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Full Screen", tint = AccentGolden, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun FullScreenAudioPlayerDialog(
    bhajan: BhajanItem,
    isPlaying: Boolean,
    currentPosMs: Long,
    totalDurationMs: Long,
    isRepeat: Boolean,
    isShuffle: Boolean,
    isFavorite: Boolean,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeekToMs: (Long) -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenLyrics: () -> Unit
) {
    val mediaHandler = rememberSuvicharMediaHandler()
    var isSeeking by remember { mutableStateOf(false) }
    var tempProgressRatio by remember { mutableStateOf(0f) }

    val activeRatio = if (isSeeking) tempProgressRatio else (if (totalDurationMs > 0L) (currentPosMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎵 " + (if (isHindi) "भजन प्लेयर" else "Music Player"), color = AccentGolden, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Fav",
                        tint = if (isFavorite) Color.Red else TextWhite
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Cover Artwork Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, WarmGold)
                ) {
                    AsyncImage(
                        model = bhajan.thumbnailUrl.ifEmpty { "file:///android_asset/temple_pooja.jpg" },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(bhajan.title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(2.dp))
                Text(bhajan.artist, fontSize = 13.sp, color = AccentGolden, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(16.dp))

                // WORKING SMOOTH SEEK BAR
                Slider(
                    value = activeRatio,
                    onValueChange = { ratio ->
                        isSeeking = true
                        tempProgressRatio = ratio
                    },
                    onValueChangeFinished = {
                        val targetMs = (tempProgressRatio * totalDurationMs).toLong()
                        onSeekToMs(targetMs)
                        isSeeking = false
                    },
                    colors = SliderDefaults.colors(thumbColor = AccentGolden, activeTrackColor = AccentGolden)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTimeMs(if (isSeeking) (tempProgressRatio * totalDurationMs).toLong() else currentPosMs), fontSize = 11.sp, color = AccentGolden, fontWeight = FontWeight.Bold)
                    Text(formatTimeMs(totalDurationMs), fontSize = 11.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Player Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleShuffle) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = if (isShuffle) AccentGolden else TextMuted)
                    }
                    IconButton(onClick = onPreviousTrack) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = AccentGolden, modifier = Modifier.size(32.dp))
                    }
                    Surface(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = AccentGolden
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(28.dp))
                        }
                    }
                    IconButton(onClick = onNextTrack) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = AccentGolden, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = onToggleRepeat) {
                        Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = if (isRepeat) AccentGolden else TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connected Actions: Lyrics & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (bhajan.lyricsText.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onOpenLyrics,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, AccentGolden)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHindi) "लिरिक्स पढ़ें" else "View Lyrics", color = AccentGolden, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val formattedMsg = com.example.miyank.ShareManager.formatGeneralShareMessage(bhajan.title, "गायक: ${bhajan.artist}")
                            mediaHandler.shareSuvichar(
                                imageUrlOrPath = bhajan.thumbnailUrl.ifEmpty { "file:///android_asset/temple_pooja.jpg" },
                                dateStr = "",
                                quoteText = bhajan.title,
                                author = bhajan.artist,
                                shareText = formattedMsg
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "शेयर करें" else "Share", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "बंद करें" else "Close", color = AccentGolden, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun BhajanLyricsReaderDialog(
    item: BhajanItem,
    fontSize: androidx.compose.ui.unit.TextUnit,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onFontSizeChange: (androidx.compose.ui.unit.TextUnit) -> Unit,
    onPlayAudio: () -> Unit
) {
    val mediaHandler = rememberSuvicharMediaHandler()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.title, color = AccentGolden, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.weight(1f))

                // Font Size Controls
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { if (fontSize.value > 12) onFontSizeChange((fontSize.value - 2).sp) }) {
                        Text("A-", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { if (fontSize.value < 26) onFontSizeChange((fontSize.value + 2).sp) }) {
                        Text("A+", color = AccentGolden, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = item.lyricsText,
                    color = TextWhite,
                    fontSize = fontSize,
                    lineHeight = (fontSize.value + 10).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlayAudio,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGolden)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "▶️ सुनें" else "▶️ Listen", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val formattedMsg = com.example.miyank.ShareManager.formatGeneralShareMessage(item.title, item.lyricsText.take(200))
                            mediaHandler.shareSuvichar(
                                imageUrlOrPath = item.thumbnailUrl.ifEmpty { "file:///android_asset/temple_pooja.jpg" },
                                dateStr = "",
                                quoteText = item.title,
                                author = item.artist,
                                shareText = formattedMsg
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, AccentGolden)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = AccentGolden, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "शेयर करें" else "Share", color = AccentGolden, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "बंद करें" else "Close", color = AccentGolden, fontWeight = FontWeight.Bold)
            }
        }
    )
}
