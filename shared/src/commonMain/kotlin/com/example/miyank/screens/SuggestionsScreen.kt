package com.example.miyank.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miyank.Language
import com.example.miyank.LanguageManager
import com.example.miyank.SuggestionManager
import com.example.miyank.SuggestionType
import com.example.miyank.UserProfileManager
import com.example.miyank.rememberHapticManager
import com.example.miyank.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    onBackClick: () -> Unit
) {
    val haptic = rememberHapticManager()
    val isHindi = LanguageManager.currentLanguage == Language.HINDI
    val userProfile by UserProfileManager.userProfile.collectAsState()

    var selectedTabType by remember { mutableStateOf(SuggestionType.APP_SUGGESTION) }
    var categoryInput by remember { mutableStateOf("") }
    var subjectInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    var isSubmittedSuccess by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "सुझाव एवं शिकायत" else "Suggestions & Complaints",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = if (isHindi) "आपकी प्रतिक्रिया हमारे लिए बहुमूल्य है" else "Your feedback helps us improve",
                            fontSize = 11.sp,
                            color = WarmGold
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite,
                    navigationIconContentColor = TextWhite
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3 SEGMENTED TAB SELECTOR
                TabRow(
                    selectedTabIndex = SuggestionType.entries.indexOf(selectedTabType).coerceAtLeast(0),
                    containerColor = SurfaceDark,
                    contentColor = WarmGold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, WarmGold.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    indicator = {},
                    divider = {}
                ) {
                    SuggestionType.entries.forEach { tabEnum ->
                        val isSelected = selectedTabType == tabEnum
                        Tab(
                            selected = isSelected,
                            onClick = {
                                haptic.playPremiumHaptic()
                                selectedTabType = tabEnum
                                isSubmittedSuccess = false
                                formError = ""
                            },
                            modifier = Modifier
                                .background(if (isSelected) WarmGold else Color.Transparent)
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = tabEnum.getDisplayName(isHindi),
                                color = if (isSelected) Color(0xFF160C05) else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (isSubmittedSuccess) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHindi) "आपकी प्रविष्टि सफलतापूर्वक प्राप्त हो गई है! धन्यवाद। 🙏" else "Your submission has been received successfully! Thank you. 🙏",
                                color = Color(0xFF81C784),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // FORM CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (selectedTabType) {
                                    SuggestionType.APP_SUGGESTION -> Icons.Default.RateReview
                                    SuggestionType.DARBAR_SUGGESTION -> Icons.Default.Feedback
                                    SuggestionType.COMPLAINT -> Icons.Default.ReportProblem
                                },
                                contentDescription = null,
                                tint = WarmGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (selectedTabType) {
                                    SuggestionType.APP_SUGGESTION -> if (isHindi) "ऐप सुधार हेतु सुझाव" else "App Feature Suggestion"
                                    SuggestionType.DARBAR_SUGGESTION -> if (isHindi) "दरबार व्यवस्था हेतु सुझाव" else "Darbar Facility Suggestion"
                                    SuggestionType.COMPLAINT -> if (isHindi) "अपनी शिकायत दर्ज करें" else "File a Complaint"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                        }

                        if (formError.isNotEmpty()) {
                            Text(formError, color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text(if (isHindi) "श्रेणी (e.g. UI, पूजा, सुविधा)" else "Category (e.g. UI, Facilities)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        OutlinedTextField(
                            value = subjectInput,
                            onValueChange = { subjectInput = it },
                            label = { Text(if (isHindi) "विषय *" else "Subject *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text(if (isHindi) "विस्तृत विवरण लिखें... *" else "Detailed Description... *") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WarmGold, unfocusedBorderColor = TextMuted, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
                        )

                        Button(
                            onClick = {
                                if (subjectInput.trim().isEmpty() || descInput.trim().isEmpty()) {
                                    formError = if (isHindi) "कृपया विषय एवं विवरण दर्ज करें!" else "Please enter subject and description!"
                                    return@Button
                                }
                                haptic.playPremiumHaptic()
                                SuggestionManager.submitSuggestion(
                                    userId = userProfile.mobileNumber.ifEmpty { "guest_user" },
                                    userName = userProfile.fullName.ifEmpty { "श्रद्धालु" },
                                    userMobile = userProfile.mobileNumber,
                                    type = selectedTabType,
                                    category = categoryInput,
                                    subject = subjectInput,
                                    description = descInput
                                )
                                formError = ""
                                subjectInput = ""
                                descInput = ""
                                categoryInput = ""
                                isSubmittedSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF160C05), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (selectedTabType) {
                                    SuggestionType.COMPLAINT -> if (isHindi) "शिकायत दर्ज करें" else "Submit Complaint"
                                    else -> if (isHindi) "सुझाव भेजें" else "Submit Suggestion"
                                },
                                color = Color(0xFF160C05),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
