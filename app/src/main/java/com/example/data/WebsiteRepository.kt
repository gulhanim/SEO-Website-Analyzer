package com.example.data

import kotlinx.coroutines.flow.Flow

class WebsiteRepository(private val websiteDao: WebsiteDao) {
    val allWebsites: Flow<List<Website>> = websiteDao.getAllWebsites()

    fun getWebsiteById(id: Int): Flow<Website?> = websiteDao.getWebsiteById(id)

    suspend fun insertWebsite(website: Website): Long = websiteDao.insertWebsite(website)

    suspend fun updateWebsite(website: Website) = websiteDao.updateWebsite(website)

    suspend fun deleteWebsite(website: Website) = websiteDao.deleteWebsite(website)

    fun getReviewsForWebsite(websiteId: Int): Flow<List<Review>> = websiteDao.getReviewsForWebsite(websiteId)

    suspend fun insertReview(review: Review): Long = websiteDao.insertReview(review)

    suspend fun updateReview(review: Review) = websiteDao.updateReview(review)

    fun getKeywordTrends(websiteId: Int): Flow<List<KeywordTrend>> = websiteDao.getKeywordTrends(websiteId)

    suspend fun insertKeywordTrend(trend: KeywordTrend) = websiteDao.insertKeywordTrend(trend)

    suspend fun clearKeywordTrends(websiteId: Int) = websiteDao.clearKeywordTrends(websiteId)

    fun getWeeklyReports(websiteId: Int): Flow<List<WeeklyReport>> = websiteDao.getWeeklyReports(websiteId)

    suspend fun insertWeeklyReport(report: WeeklyReport) = websiteDao.insertWeeklyReport(report)
}
