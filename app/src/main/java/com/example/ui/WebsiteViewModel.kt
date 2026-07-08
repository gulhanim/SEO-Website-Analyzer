package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SeoReportState {
    object Idle : SeoReportState
    object Loading : SeoReportState
    data class Success(val report: String) : SeoReportState
    data class Error(val message: String) : SeoReportState
}

enum class ThemeMode {
    LIGHT, DARK, NIGHT
}

data class SocialAccountCheckLog(
    val platform: String,
    val accountName: String,
    val latencyMs: Int,
    val isMissed: Boolean,
    val statusMessage: String
)

class WebsiteViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = WebsiteRepository(db.websiteDao())

    // All websites
    val websites: StateFlow<List<Website>> = repository.allWebsites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Website State
    private val _selectedWebsite = MutableStateFlow<Website?>(null)
    val selectedWebsite: StateFlow<Website?> = _selectedWebsite.asStateFlow()

    // Selected Website's Keyword Trends
    val keywordTrends: StateFlow<List<KeywordTrend>> = _selectedWebsite
        .flatMapLatest { website ->
            if (website != null) repository.getKeywordTrends(website.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Website's Reviews
    val reviews: StateFlow<List<Review>> = _selectedWebsite
        .flatMapLatest { website ->
            if (website != null) repository.getReviewsForWebsite(website.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Website's Weekly Reports
    val weeklyReports: StateFlow<List<WeeklyReport>> = _selectedWebsite
        .flatMapLatest { website ->
            if (website != null) repository.getWeeklyReports(website.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // SEO Report State
    private val _seoReportState = MutableStateFlow<SeoReportState>(SeoReportState.Idle)
    val seoReportState: StateFlow<SeoReportState> = _seoReportState.asStateFlow()

    // Intercepted Review state
    private val _interceptedReview = MutableStateFlow<Review?>(null)
    val interceptedReview: StateFlow<Review?> = _interceptedReview.asStateFlow()

    // Subagent Prioritization Result state
    private val _subagentAnalysis = MutableStateFlow<String?>(null)
    val subagentAnalysis: StateFlow<String?> = _subagentAnalysis.asStateFlow()

    // App Theme State
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Error messages/notifications
    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    // Social Media Scan State
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Scanned Social Accounts Latency Logs
    private val _lastScanLogs = MutableStateFlow<List<SocialAccountCheckLog>>(emptyList())
    val lastScanLogs: StateFlow<List<SocialAccountCheckLog>> = _lastScanLogs.asStateFlow()

    init {
        // Automatically select the first website if any exists
        viewModelScope.launch {
            websites.collect { list ->
                if (list.isNotEmpty() && _selectedWebsite.value == null) {
                    selectWebsite(list.first())
                }
            }
        }
    }

    fun selectWebsite(website: Website) {
        _selectedWebsite.value = website
        _seoReportState.value = SeoReportState.Idle
        _interceptedReview.value = null
        _subagentAnalysis.value = null
        // Trigger report generation or load
        generateSeoReport(website)
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun addWebsite(url: String, name: String) {
        viewModelScope.launch {
            val formattedUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            val newWebsite = Website(
                url = formattedUrl,
                name = name,
                trafficCount = (800..5000).random(),
                seoScore = (50..85).random(),
                isPremium = false
            )
            val websiteId = repository.insertWebsite(newWebsite)
            val savedWebsite = newWebsite.copy(id = websiteId.toInt())

            // Seed initial dummy trends
            seedInitialData(savedWebsite)

            // Auto-select the newly added website
            selectWebsite(savedWebsite)
            _notificationMessage.value = "Website '$name' added successfully!"
        }
    }

    private suspend fun seedInitialData(website: Website) {
        // Keyword trends
        val trends = listOf(
            KeywordTrend(websiteId = website.id, keyword = "${website.name} services", searchVolume = 2400, ranking = 15, change = 2, competitorRanking = 4),
            KeywordTrend(websiteId = website.id, keyword = "${website.name} reviews", searchVolume = 850, ranking = 8, change = -1, competitorRanking = 1),
            KeywordTrend(websiteId = website.id, keyword = "best website developer near me", searchVolume = 5400, ranking = 45, change = 8, competitorRanking = 12),
            KeywordTrend(websiteId = website.id, keyword = "affordable SEO checker", searchVolume = 1900, ranking = 32, change = -3, competitorRanking = 7)
        )
        trends.forEach { repository.insertKeywordTrend(it) }

        // Initial Reviews
        val reviewsList = listOf(
            Review(websiteId = website.id, reviewerName = "Sarah J.", reviewText = "Incredibly fast response times. I highly recommend them!", rating = 5, platform = "Google", status = "Active"),
            Review(websiteId = website.id, reviewerName = "Alex M.", reviewText = "Great overall service, though search engine optimization could use minor improvements.", rating = 4, platform = "Yelp", status = "Active"),
            Review(websiteId = website.id, reviewerName = "John K.", reviewText = "A bit slow to load on mobile browsers, but customer service was responsive.", rating = 3, platform = "Facebook", status = "Active")
        )
        reviewsList.forEach { repository.insertReview(it) }
    }

    fun upgradeToPremium() {
        val current = _selectedWebsite.value ?: return
        viewModelScope.launch {
            val updated = current.copy(isPremium = true)
            repository.updateWebsite(updated)
            _selectedWebsite.value = updated
            _notificationMessage.value = "🎉 Website upgraded to PREMIUM tier! Real-time social monitor & review protector unlocked."
        }
    }

    fun generateSeoReport(website: Website) {
        viewModelScope.launch {
            _seoReportState.value = SeoReportState.Loading
            try {
                val report = GeminiClient.getSeoAnalysis(website.url, website.name)
                _seoReportState.value = SeoReportState.Success(report)
            } catch (e: Exception) {
                _seoReportState.value = SeoReportState.Error(e.message ?: "Failed to generate report")
            }
        }
    }

    // Autonomous Social Crawler - checks Google, Yelp, Facebook, X APIs for negative reviews of the URL
    fun scanSocialMediaForNegativeReviews() {
        val current = _selectedWebsite.value ?: return
        viewModelScope.launch {
            _isScanning.value = true
            _notificationMessage.value = "🔍 Crawling Google, Yelp, Facebook, and X APIs for mention of ${current.url}..."
            
            // Wait for 1.8 seconds to simulate active social network API scanning
            kotlinx.coroutines.delay(1800)
            
            try {
                // Call Gemini to generate a realistic user post/complaint about this business on social networks
                val result = GeminiClient.generateNegativeSocialReview(current.url, current.name)
                val reviewer = result["reviewerName"] ?: "Anonymous Customer"
                val text = result["reviewText"] ?: "Frustrated with some performance issues on their checkout."
                val rating = result["rating"]?.toIntOrNull() ?: 1
                val platform = result["platform"] ?: "Google"
                
                // Generate detailed latency logs for 8 different social profiles/feeds
                val cleanBrand = current.name.lowercase().replace(" ", "")
                val checkTemplates = listOf(
                    Pair("Google Business API", "Main Listing Feed"),
                    Pair("Google Business API", "Support Location Hub"),
                    Pair("Yelp Fusion Search", "${current.name} HQ Profile"),
                    Pair("Yelp Fusion Search", "${current.name} West Coast Hub"),
                    Pair("Facebook Pages SDK", "@$cleanBrand Official Feed"),
                    Pair("Facebook Pages SDK", "@${cleanBrand}_community"),
                    Pair("X (Twitter) Firehose", "@$cleanBrand Mention Stream"),
                    Pair("X (Twitter) Firehose", "@${cleanBrand}_support Stream")
                )

                val logs = checkTemplates.map { (plat, account) ->
                    // Make some checks fast, some slow, and some exceed 1500ms threshold
                    val latency = (80..2200).random()
                    val isMissed = latency > 1500
                    val statusMessage = if (isMissed) {
                        "❌ MISSED: API response timed out after ${latency}ms (safety limit 1500ms)"
                    } else {
                        "✅ SUCCESS: Checked in ${latency}ms"
                    }
                    SocialAccountCheckLog(
                        platform = plat,
                        accountName = account,
                        latencyMs = latency,
                        isMissed = isMissed,
                        statusMessage = statusMessage
                    )
                }
                _lastScanLogs.value = logs

                if (current.isPremium) {
                    // PREMIUM SHIELD IS ACTIVE: Intercept & Catch before it's published publicly/permanently
                    val tempReview = Review(
                        websiteId = current.id,
                        reviewerName = reviewer,
                        reviewText = text,
                        rating = rating,
                        platform = platform,
                        status = "Intercepted"
                    )
                    
                    // Trigger autonomous prioritization and analysis report
                    val subagentResult = GeminiClient.getAutonomousPrioritization(current.url, text)
                    _subagentAnalysis.value = subagentResult
                    
                    val promoCode = "LOYAL-${(1000..9999).random()}-REWARD"
                    _interceptedReview.value = tempReview.copy(giftCardCode = promoCode)
                    _notificationMessage.value = "🚨 Caught a new negative post from $reviewer on $platform! Review paused before going viral."
                } else {
                    // Standard tier: just submit it as a live public review because the shield is off!
                    val newReview = Review(
                        websiteId = current.id,
                        reviewerName = reviewer,
                        reviewText = text,
                        rating = rating,
                        platform = platform,
                        status = "Active"
                    )
                    repository.insertReview(newReview)
                    _notificationMessage.value = "⚠️ Alert: Negative post published publicly on $platform! (Upgrade to Premium to shield & auto-offer retention gift cards)"
                }
            } catch (e: Exception) {
                _notificationMessage.value = "Error during social media crawling: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    // Accept and send gift card to turn negative review into positive interaction
    fun resolveInterceptedReview(accepted: Boolean) {
        val current = _selectedWebsite.value ?: return
        val review = _interceptedReview.value ?: return
        viewModelScope.launch {
            if (accepted) {
                // The customer accepted the gift card and we resolved their issue! Let's save a 5-star review instead!
                val resolvedReview = review.copy(
                    reviewText = "Amazing support! The team turned around my issues immediately and offered compensation. Exceeded my expectations!",
                    rating = 5,
                    status = "GiftCardOffered",
                    timestamp = System.currentTimeMillis()
                )
                repository.insertReview(resolvedReview)
                _notificationMessage.value = "🎁 Customer accepted the retention offer! Experience turned positive."
            } else {
                // The customer rejected or skipped. Save original review as active.
                val savedReview = review.copy(status = "Active", timestamp = System.currentTimeMillis())
                repository.insertReview(savedReview)
                _notificationMessage.value = "Review submitted without retention."
            }
            // Clear interception UI state
            _interceptedReview.value = null
            _subagentAnalysis.value = null
        }
    }

    // Generate Weekly performance report
    fun generateWeeklyReport() {
        val current = _selectedWebsite.value ?: return
        viewModelScope.launch {
            _notificationMessage.value = "Generating weekly report..."
            val mockReport = """
                ## 📅 Weekly Performance Report
                **Date:** July 2026
                **SEO Health Trend:** Upward trend (+4.5% keyword rank improvements)
                **Real-time Traffic:** 3,450 sessions (+8.2% vs last week)
                
                ### Key Highlights
                * Negative review shield intercepted 2 experiences and successfully converted both into 5-star Google reviews.
                * Competitor gap optimized: Published "Alternative to competitors" comparison guide.
                * Key organic keywords climbing in rank by +3 average spots.
                
                *This report was automatically synthesized by your Autonomous SEO Analyst Subagent.*
            """.trimIndent()
            
            repository.insertWeeklyReport(WeeklyReport(websiteId = current.id, reportText = mockReport))
            _notificationMessage.value = "Weekly report successfully generated!"
        }
    }

    fun deleteCurrentWebsite() {
        val current = _selectedWebsite.value ?: return
        viewModelScope.launch {
            repository.deleteWebsite(current)
            _selectedWebsite.value = null
            _notificationMessage.value = "Website deleted."
        }
    }
}
