package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteDao {
    @Query("SELECT * FROM websites ORDER BY addedAt DESC")
    fun getAllWebsites(): Flow<List<Website>>

    @Query("SELECT * FROM websites WHERE id = :id")
    fun getWebsiteById(id: Int): Flow<Website?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsite(website: Website): Long

    @Update
    suspend fun updateWebsite(website: Website)

    @Delete
    suspend fun deleteWebsite(website: Website)

    @Query("SELECT * FROM reviews WHERE websiteId = :websiteId ORDER BY timestamp DESC")
    fun getReviewsForWebsite(websiteId: Int): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Update
    suspend fun updateReview(review: Review)

    @Query("SELECT * FROM keyword_trends WHERE websiteId = :websiteId ORDER BY searchVolume DESC")
    fun getKeywordTrends(websiteId: Int): Flow<List<KeywordTrend>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeywordTrend(trend: KeywordTrend)

    @Query("DELETE FROM keyword_trends WHERE websiteId = :websiteId")
    suspend fun clearKeywordTrends(websiteId: Int)

    @Query("SELECT * FROM weekly_reports WHERE websiteId = :websiteId ORDER BY reportDate DESC")
    fun getWeeklyReports(websiteId: Int): Flow<List<WeeklyReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyReport(report: WeeklyReport)
}
