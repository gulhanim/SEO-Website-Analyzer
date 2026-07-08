package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.KeywordTrend
import com.example.data.Review
import com.example.data.Website

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: WebsiteViewModel) {
    val websites by viewModel.websites.collectAsStateWithLifecycle()
    val selectedWebsite by viewModel.selectedWebsite.collectAsStateWithLifecycle()
    val keywordTrends by viewModel.keywordTrends.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val weeklyReports by viewModel.weeklyReports.collectAsStateWithLifecycle()
    val seoReportState by viewModel.seoReportState.collectAsStateWithLifecycle()
    val interceptedReview by viewModel.interceptedReview.collectAsStateWithLifecycle()
    val subagentAnalysis by viewModel.subagentAnalysis.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notificationMessage by viewModel.notificationMessage.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val lastScanLogs by viewModel.lastScanLogs.collectAsStateWithLifecycle()

    var showAddWebsiteDialog by remember { mutableStateOf(false) }
    var currentSubTab by remember { mutableStateOf(0) } // 0: Analytics, 1: SEO Keywords, 2: Review Shield

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SeoShieldLogo()
                        Column {
                            Text(
                                "SEO & Review Shield",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Real-time Traffic & Integrity",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Theme switcher buttons
                    IconButton(
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        modifier = Modifier.testTag("theme_light_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Light Theme",
                            tint = if (themeMode == ThemeMode.LIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        modifier = Modifier.testTag("theme_dark_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = "Dark Theme",
                            tint = if (themeMode == ThemeMode.DARK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setThemeMode(ThemeMode.NIGHT) },
                        modifier = Modifier.testTag("theme_night_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nightlight,
                            contentDescription = "Night Theme",
                            tint = if (themeMode == ThemeMode.NIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentSubTab == 0,
                    onClick = { currentSubTab = 0 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    modifier = Modifier.testTag("nav_analytics")
                )
                NavigationBarItem(
                    selected = currentSubTab == 1,
                    onClick = { currentSubTab = 1 },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Keywords") },
                    label = { Text("Keywords") },
                    modifier = Modifier.testTag("nav_keywords")
                )
                NavigationBarItem(
                    selected = currentSubTab == 2,
                    onClick = { currentSubTab = 2 },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Shield") },
                    label = { Text("Shield") },
                    modifier = Modifier.testTag("nav_shield")
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWebsiteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_website_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Website URL")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Website Selector Section
            WebsiteSelectorSection(
                websites = websites,
                selectedWebsite = selectedWebsite,
                onSelect = { viewModel.selectWebsite(it) },
                onAddClick = { showAddWebsiteDialog = true }
            )

            if (selectedWebsite == null) {
                EmptyStateView()
            } else {
                val website = selectedWebsite!!
                
                // Dashboard Content based on selected bottom tab
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (currentSubTab) {
                        0 -> AnalyticsTabContent(
                            website = website,
                            seoState = seoReportState,
                            weeklyReports = weeklyReports,
                            onUpgradeClick = { viewModel.upgradeToPremium() },
                            onGenerateWeekly = { viewModel.generateWeeklyReport() },
                            onRefresh = { viewModel.generateSeoReport(website) },
                            onDelete = { viewModel.deleteCurrentWebsite() }
                        )
                        1 -> KeywordsTabContent(
                            website = website,
                            keywordTrends = keywordTrends
                        )
                        2 -> ReviewShieldTabContent(
                            website = website,
                            reviews = reviews,
                            isScanning = isScanning,
                            lastScanLogs = lastScanLogs,
                            onUpgradeClick = { viewModel.upgradeToPremium() },
                            onScanClick = { viewModel.scanSocialMediaForNegativeReviews() }
                        )
                    }
                }
            }
        }
    }

    // Add Website Dialog
    if (showAddWebsiteDialog) {
        AddWebsiteDialog(
            onDismiss = { showAddWebsiteDialog = false },
            onConfirm = { url, name ->
                viewModel.addWebsite(url, name)
                showAddWebsiteDialog = false
            }
        )
    }

    // Intercepted Review Modal / Alert Dialog
    if (interceptedReview != null) {
        InterceptedReviewModal(
            review = interceptedReview!!,
            analysisText = subagentAnalysis ?: "Analyzing negative sentiments autonomously...",
            onAccept = { viewModel.resolveInterceptedReview(true) },
            onDecline = { viewModel.resolveInterceptedReview(false) }
        )
    }
}

// Custom Vector Drawn Logo Composable
@Composable
fun SeoShieldLogo() {
    Canvas(
        modifier = Modifier
            .size(36.dp)
            .testTag("app_logo_canvas")
    ) {
        val width = size.width
        val height = size.height

        // Draw outer shield
        val shieldPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.5f, height * 0.1f)
            lineTo(width * 0.85f, height * 0.2f)
            lineTo(width * 0.85f, height * 0.55f)
            quadraticTo(width * 0.85f, height * 0.85f, width * 0.5f, height * 0.95f)
            quadraticTo(width * 0.15f, height * 0.85f, width * 0.15f, height * 0.55f)
            lineTo(width * 0.15f, height * 0.2f)
            close()
        }
        drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0D9488), Color(0xFF0EA5E9))
            )
        )

        // Draw an inner glowing check/lightning mark
        val checkPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(width * 0.35f, height * 0.5f)
            lineTo(width * 0.48f, height * 0.65f)
            lineTo(width * 0.7f, height * 0.35f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun WebsiteSelectorSection(
    websites: List<Website>,
    selectedWebsite: Website?,
    onSelect: (Website) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monitored Environments",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "${websites.size} Sites",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (websites.isEmpty()) {
                Button(
                    onClick = onAddClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_first_site_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add your first Website URL")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    websites.forEach { website ->
                        val isSelected = selectedWebsite?.id == website.id
                        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

                        Card(
                            onClick = { onSelect(website) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                                .testTag("website_pill_${website.id}"),
                            colors = CardDefaults.cardColors(containerColor = containerColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = website.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (website.isPremium) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Premium Status",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = website.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("empty_state_layout"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Web,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No Websites Active",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add a website URL to begin real-time traffic monitoring, keyword gap analysis, weekly reporting, and negative review protection.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun AnalyticsTabContent(
    website: Website,
    seoState: SeoReportState,
    weeklyReports: List<com.example.data.WeeklyReport>,
    onUpgradeClick: () -> Unit,
    onGenerateWeekly: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tier / Premium Banner
        PremiumTierBanner(website, onUpgradeClick)

        // Real-time Traffic Gauges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrafficGaugeCard(
                title = "SEO Score",
                score = website.seoScore,
                modifier = Modifier.weight(1f)
            )
            TrafficGaugeCard(
                title = "Traffic Volume",
                score = website.trafficCount / 50, // scaled for display
                labelOverride = "${website.trafficCount} /hr",
                modifier = Modifier.weight(1f)
            )
        }

        // Action Buttons Row (Refresh SEO, Delete Domain)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRefresh,
                modifier = Modifier
                    .weight(1f)
                    .testTag("refresh_seo_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sync SEO Analysis")
            }

            Button(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_domain_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete website")
            }
        }

        // Live Gemini SEO Report Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("seo_report_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Autonomous SEO Audit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text("Gemini Powered", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Divider()

                when (seoState) {
                    is SeoReportState.Idle -> {
                        Text("Triggering initial analysis...")
                    }
                    is SeoReportState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is SeoReportState.Success -> {
                        Text(
                            text = seoState.report,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    is SeoReportState.Error -> {
                        Text(
                            text = "Error generating report: ${seoState.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Weekly Reports Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weekly_report_section_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Weekly Performance Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onGenerateWeekly,
                        modifier = Modifier.testTag("generate_weekly_report_btn")
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = "Synthesize Weekly Report")
                    }
                }

                Divider()

                if (weeklyReports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No reports generated yet. Click above to synthesize.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    weeklyReports.forEach { report ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Report synthesized on: ${java.text.DateFormat.getDateInstance().format(report.reportDate)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = report.reportText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 18.sp
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
fun TrafficGaugeCard(
    title: String,
    score: Int,
    labelOverride: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(70.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = outlineColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = 135f,
                        sweepAngle = (270f * (score.coerceIn(0, 100) / 100f)),
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = labelOverride ?: "$score/100",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PremiumTierBanner(website: Website, onUpgradeClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (website.isPremium) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("premium_banner_card")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (website.isPremium) Icons.Default.VerifiedUser else Icons.Default.SecurityUpdateWarning,
                contentDescription = null,
                tint = if (website.isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(36.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (website.isPremium) "Premium Integrity Shield Active" else "Standard Tier Active",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (website.isPremium) "Real-time crawler, subagents, and negative review gift triggers are active." else "Upgrade to unlock social platform checking & bad review gift triggers.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!website.isPremium) {
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.testTag("upgrade_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("Upgrade")
                }
            }
        }
    }
}

@Composable
fun KeywordsTabContent(
    website: Website,
    keywordTrends: List<KeywordTrend>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Keyword Performance Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Compare your page ranking against real-time competitor data to bridge content gaps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        keywordTrends.forEach { trend ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("keyword_card_${trend.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trend.keyword,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (trend.change >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (trend.change >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "${if (trend.change >= 0) "+" else ""}${trend.change}",
                                fontWeight = FontWeight.Bold,
                                color = if (trend.change >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Monthly Vol", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${trend.searchVolume}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Your Rank", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("#${trend.ranking}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Competitor Rank", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("#${trend.competitorRanking}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Content gap highlighting
                    if (trend.competitorRanking < trend.ranking) {
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                                .padding(8.dp)
                        ) {
                            Text(
                                "⚠️ Actionable Content Gap: Competitor ranks higher by ${trend.ranking - trend.competitorRanking} positions. Optimize FAQ schema and include local keywords to reclaim visibility.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewShieldTabContent(
    website: Website,
    reviews: List<Review>,
    isScanning: Boolean,
    lastScanLogs: List<SocialAccountCheckLog>,
    onUpgradeClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Social Media Review Shield",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Our autonomous subagent crawls Google, Yelp, Facebook, and X to intercept and catch negative user feedback mentioning your URL: ${website.url}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // Active Platform Connections Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Autonomous Scanning Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Divider()

                val platforms = listOf(
                    Triple("Google Business Profile", "API Connected", Icons.Default.Business),
                    Triple("Yelp Fusion Search", "API Connected", Icons.Default.Search),
                    Triple("Facebook Pages SDK", "API Connected", Icons.Default.Share),
                    Triple("X (Twitter) Firehose", "API Connected", Icons.Default.DynamicFeed)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    platforms.take(2).forEach { (name, status, icon) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(name, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Text("CRAWLING ACTIVE", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    platforms.drop(2).forEach { (name, status, icon) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(name, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Text("CRAWLING ACTIVE", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Scanning Action Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("crawler_action_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isScanning) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "AI spider subagents are querying social platforms for brand-new reviews...",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Run Autonomous Social Scan",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Trigger real-time spider subagents to crawl Yelp, Google Business, Facebook, and X APIs to detect new negative reviews, customer posts, or disgruntled tweets mentioning your website.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!website.isPremium) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Text(
                                    "WARNING: Shield deactivated. Any negative complaints caught during this scan will publish directly to public feeds. Upgrade to Premium to intercept them!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onScanClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_social_crawler_btn")
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Search & Crawl Public Platforms Now")
                    }
                }
            }
        }

        // Real-Time Crawl & Latency Diagnostics Log Panel
        if (lastScanLogs.isNotEmpty()) {
            val missedCount = lastScanLogs.count { it.isMissed }
            val successCount = lastScanLogs.size - missedCount

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("crawler_latency_diagnostics_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "📡 API Latency & Miss Diagnostics",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (missedCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = if (missedCount > 0) "$missedCount channels missed" else "All channels healthy",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (missedCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "The autonomous subagents queried ${lastScanLogs.size} different social media profiles/API endpoints for mentions of ${website.url}. Outlined below are individual profile latencies and highlights of checks missed because of timeout limits:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Summary Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Scanned Profiles", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${lastScanLogs.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Success Logs", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$successCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF10B981))
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Missed (Latency)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$missedCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (missedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Explanatory alert about latency bypass
                    if (missedCount > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "⚠️ HIGH LATENCY TIMEOUT (>1500ms): These feeds failed to respond in time and were bypassed. Disgruntled customer posts on these channels may slip past the shield until latency recovers.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Divider()

                    // Checklist details
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lastScanLogs.forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (log.isMissed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (log.isMissed) Icons.Default.CloudOff else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (log.isMissed) MaterialTheme.colorScheme.error else Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = log.accountName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = log.platform,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${log.latencyMs} ms",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (log.isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (log.isMissed) "TIMEOUT BYPASS" else "SUCCESS",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.sp,
                                        color = if (log.isMissed) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Reviews feed
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Caught & Intercepted Public Feed Logs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Badge(
                        containerColor = if (website.isPremium) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                    ) {
                        Text(
                            text = if (website.isPremium) "Shield Active" else "Shield Off",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Divider()

                if (reviews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No reviews cached.")
                    }
                } else {
                    reviews.forEach { r ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("feedback_item_${r.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(r.reviewerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(r.platform, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row {
                                        (1..5).forEach { star ->
                                            Icon(
                                                imageVector = if (star <= r.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(r.reviewText, style = MaterialTheme.typography.bodyMedium)

                                // Status tags
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Badge(
                                        containerColor = when (r.status) {
                                            "GiftCardOffered" -> Color(0xFF10B981)
                                            "Intercepted" -> Color(0xFFFBBF24)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    ) {
                                        Text(
                                            text = when (r.status) {
                                                "GiftCardOffered" -> "Experience Converted (5★ Resolved)"
                                                "Intercepted" -> "Intercepted Review"
                                                else -> "Published Publicly"
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = Color.White
                                        )
                                    }

                                    if (r.giftCardCode != null) {
                                        Text(
                                            "Gift Active: ${r.giftCardCode}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddWebsiteDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, name: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_website_dialog_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Monitor New Website",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Brand / Website Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_site_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Website URL (e.g. google.com)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_site_url_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_cancel_btn")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (url.isNotBlank() && name.isNotBlank()) {
                                onConfirm(url, name)
                            }
                        },
                        modifier = Modifier.testTag("dialog_confirm_btn")
                    ) {
                        Text("Add Site")
                    }
                }
            }
        }
    }
}

@Composable
fun InterceptedReviewModal(
    review: Review,
    analysisText: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("intercept_review_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning / Alert symbol
                Icon(
                    imageVector = Icons.Default.OfflineBolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "🚨 NEGATIVE REVIEW SHIELDED!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "A potential bad review from ${review.reviewerName} was intercepted before publication. Here is the autonomous subagent priority report and retention draft:",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )

                Divider()

                // Autonomous subagent prioritization and feedback categorization
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Subagent Analytics & Prioritization",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = analysisText,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Customer original review quote
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Original Customer Review Draft:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "\"${review.reviewText}\" — Rated ${review.rating}★",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Divider()

                Text(
                    "Would you like to offer a Gift Card code to convert this negative experience into a positive 5★ loyalty feedback immediately?",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("intercept_decline_btn")
                    ) {
                        Text("Let Public Submit", fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("intercept_accept_btn")
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Send Gift Code", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
