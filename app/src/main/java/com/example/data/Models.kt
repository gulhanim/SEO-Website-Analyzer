package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "websites")
data class Website(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val name: String,
    val addedAt: Long = System.currentTimeMillis(),
    val trafficCount: Int = 1250,
    val seoScore: Int = 75,
    val isPremium: Boolean = false
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val websiteId: Int,
    val reviewerName: String,
    val reviewText: String,
    val rating: Int, // 1 to 5
    val timestamp: Long = System.currentTimeMillis(),
    val platform: String, // Google, Yelp, Facebook, X
    val status: String, // "Active", "Intercepted", "GiftCardOffered"
    val giftCardCode: String? = null
)

@Entity(tableName = "keyword_trends")
data class KeywordTrend(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val websiteId: Int,
    val keyword: String,
    val searchVolume: Int,
    val ranking: Int,
    val change: Int, // e.g. +3, -1
    val competitorRanking: Int
)

@Entity(tableName = "weekly_reports")
data class WeeklyReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val websiteId: Int,
    val reportDate: Long = System.currentTimeMillis(),
    val reportText: String
)
