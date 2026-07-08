package com.example.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getSeoAnalysis(url: String, name: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is missing or default")
            return@withContext getMockSeoAnalysis(url, name)
        }

        val prompt = """
            You are an expert SEO and Traffic Analyst. Provide a real-time, professional SEO traffic analysis, content gaps, ranking opportunities, and keyword recommendations for the website URL: $url ($name).
            Include:
            1. An executive SEO score summary and simulated traffic health.
            2. Top 3 actionable keyword performance trends with search volume, ranking, and difficulty.
            3. Detailed Content Gaps (what the competitors are covering that this site lacks).
            4. 3 specific actionable recommendations for better visibility across major search engines.
            
            Keep your tone professional, authoritative, and helpful. Format your response cleanly using bullet points and clear section headers. Do not use markdown files or extra fields.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Unsuccessful response from Gemini: ${response.code} ${response.message}")
                    return@withContext getMockSeoAnalysis(url, name)
                }

                val bodyStr = response.body?.string() ?: ""
                val jsonResponse = JSONObject(bodyStr)
                val textResult = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                textResult
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Gemini API", e)
            getMockSeoAnalysis(url, name)
        }
    }

    suspend fun getAutonomousPrioritization(url: String, inputReview: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getMockAutonomousPrioritization(inputReview)
        }

        val prompt = """
            You are an autonomous customer relations and workflow prioritization subagent.
            A customer is writing a negative review for the website URL: $url.
            Review: "$inputReview"
            
            Analyze this negative feedback and provide:
            1. SEVERITY LEVEL: (High, Medium, Low) based on impact.
            2. CATEGORY: (e.g., Load Speed, Usability, Pricing, Customer Service, Security, Other)
            3. GIFT CARD RECOMMENDATION: Recommend a dynamic value (e.g., $10, $20, or $50) and a customized, warm, apologetic message that includes a promo/gift code to convert this negative experience into a positive, loyal user interaction.
            
            Keep the response polite and highly encouraging to retain the user.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext getMockAutonomousPrioritization(inputReview)

                val bodyStr = response.body?.string() ?: ""
                val jsonResponse = JSONObject(bodyStr)
                jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            }
        } catch (e: Exception) {
            getMockAutonomousPrioritization(inputReview)
        }
    }

    suspend fun generateNegativeSocialReview(url: String, name: String): Map<String, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getMockNegativeSocialReview(url, name)
        }

        val prompt = """
            You are a simulator that generates a realistic, simulated negative social media customer review or post about a business website.
            Website URL: $url
            Business/Brand Name: $name

            Choose a random public platform from: [Google, Yelp, Facebook, X].
            Generate a realistic negative review or complaint post that an end-user might submit on that platform about this website or business. Focus on issues like page speed, outdated UI, broken links, frustrating mobile experience, or lack of customer support contact.

            Output strictly as a valid JSON object with the following keys:
            - "reviewerName": a realistic full name or social handle
            - "reviewText": the simulated complaint or post text (1-3 sentences)
            - "rating": a random string "1" or "2" 
            - "platform": the chosen platform name

            Ensure the response is ONLY raw valid JSON. Do not wrap it in ```json blocks or include any extra text.
        """.trimIndent()

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext getMockNegativeSocialReview(url, name)

                val bodyStr = response.body?.string() ?: ""
                var jsonStr = bodyStr
                val jsonResponse = JSONObject(jsonStr)
                val textResult = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                // strip markdown if gemini wrapped it in ```json
                var sanitized = textResult
                if (sanitized.startsWith("```")) {
                    sanitized = sanitized.substringAfter("```").trim()
                    if (sanitized.startsWith("json")) {
                        sanitized = sanitized.substringAfter("json").trim()
                    }
                    if (sanitized.endsWith("```")) {
                        sanitized = sanitized.substringBeforeLast("```").trim()
                    }
                }

                val obj = JSONObject(sanitized)
                mapOf(
                    "reviewerName" to obj.optString("reviewerName", "Anonymous Customer"),
                    "reviewText" to obj.optString("reviewText", "Frustrated with the slow checkout experience on this page."),
                    "rating" to obj.optString("rating", "1"),
                    "platform" to obj.optString("platform", "Google")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating simulated review", e)
            getMockNegativeSocialReview(url, name)
        }
    }

    private fun getMockNegativeSocialReview(url: String, name: String): Map<String, String> {
        val templates = listOf(
            mapOf(
                "reviewerName" to "Mark Reynolds",
                "reviewText" to "Tried checking out on $name ($url) but the mobile responsive layout is completely broken. It took forever and I gave up.",
                "rating" to "1",
                "platform" to "Yelp"
            ),
            mapOf(
                "reviewerName" to "Sophia @techie_soph",
                "reviewText" to "The pages on $url load incredibly slow over cellular connection. It is 2026, $name needs a modern CDN or optimization update!",
                "rating" to "2",
                "platform" to "X"
            ),
            mapOf(
                "reviewerName" to "Jonathan K.",
                "reviewText" to "The contact form on $name throws a server error, and there's no working customer support phone number listed. Extremely frustrating.",
                "rating" to "1",
                "platform" to "Google"
            ),
            mapOf(
                "reviewerName" to "Amanda Vance",
                "reviewText" to "Found broken links on the main product pricing section of $url. I feel hesitant to enter my card details if the site lacks basic QA maintenance.",
                "rating" to "2",
                "platform" to "Facebook"
            )
        )
        return templates.random()
    }

    private fun getMockSeoAnalysis(url: String, name: String): String {
        return """
            ### 📊 Real-Time Traffic & SEO Executive Summary
            * **Domain Analyzed:** $url ($name)
            * **Simulated Monthly Traffic:** 12,450 unique sessions (+14.2% YoY)
            * **SEO Health Index:** 78/100 (Needs Optimization)
            * **Page Load Speed:** 1.8s (Good, mobile-responsive optimized)

            ### 📈 Keyword Performance Trends
            * **"best $name services"** | Search Volume: 8,400/mo | Current Rank: #14 | Competitor Rank: #3 (Opportunity)
            * **"$name reviews"** | Search Volume: 1,200/mo | Current Rank: #8 | Competitor Rank: #1
            * **"affordable $name features"** | Search Volume: 3,600/mo | Current Rank: #25 | Competitor Rank: #5 (High Content Gap)

            ### 🔍 Actionable Content Gaps Identified
            1. **Missing Comparison Guide:** Your main competitors have comprehensive "Alternative to..." directories that capture 25% of commercial search intent.
            2. **Anemic FAQ Schema:** Your service pages lack structured schema data, preventing rich snippet displays on Google and Bing.
            3. **Social Proof Integration:** Competitor sites feature structured trust badges and real-time social platform streams that build organic trust.

            ### ⚡ Recommendations for Better Visibility
            1. **Target Content Gap #1:** Publish a detailed comparison page targeting your top 3 competitors by name.
            2. **Structured Schema Markup:** Implement FAQ and product schemas across all product detail pages immediately.
            3. **Improve Keyword Density:** Optimize meta tags and header hierarchies to emphasize local search keywords with low competition.
        """.trimIndent()
    }

    private fun getMockAutonomousPrioritization(review: String): String {
        val severity = if (review.contains("worst", ignoreCase = true) || review.contains("terrible", ignoreCase = true) || review.contains("scam", ignoreCase = true) || review.contains("bad", ignoreCase = true)) "CRITICAL" else "HIGH"
        val giftValue = if (severity == "CRITICAL") "$25 Amazon Gift Card" else "$15 Amazon Gift Card"
        return """
            ### 🚨 Subagent Autonomous Workflow Prioritization Report
            * **Severity Level:** $severity (Requires Immediate Retention)
            * **Identified Category:** Customer Satisfaction & Service Friction
            * **Risk Assessment:** High risk of public negative review propagation across Yelp, Google, and Twitter.
            
            ### 🎁 Intercepted Gift Offer Details
            * **Recommended Value:** $giftValue
            * **Intercept Status:** CATCH BEFORE SUBMIT (Success)
            
            ### ✉️ Customized Apology & Retention Message:
            "Dear customer, we noticed you had a less-than-stellar experience. Your happiness is our absolute highest priority. We would love the chance to make this right! Here is a **$giftValue** (Code: **SEOSHIELD-LOYAL-2026**) for your next visit. We have paused your review submission and our priority engineering team is on standby to solve this for you immediately."
        """.trimIndent()
    }
}
