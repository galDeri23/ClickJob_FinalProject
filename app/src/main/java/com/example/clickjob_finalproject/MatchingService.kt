package com.example.clickjob_finalproject

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object MatchingService {

    // Server base URL (without a trailing path)
    private const val BASE_URL =
        "https://clickjob-node-backend-493713788422.europe-west1.run.app"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Notifies the server that a new job was created, so it can match it against all candidates.
     * Call this after the job was successfully saved to Firestore.
     */
    fun triggerJobMatching(jobId: String) {
        sendTrigger(url = "$BASE_URL/api/jobs", id = jobId, label = "job/$jobId")
    }

    /**
     * Notifies the server that a candidate was created/updated, so it can match them against all jobs.
     * Call this after the profile was successfully saved to Firestore.
     */
    fun triggerCandidateMatching(candidateId: String) {
        sendTrigger(url = "$BASE_URL/api/candidates", id = candidateId, label = "candidate/$candidateId")
    }

    // Shared logic for both calls — fire-and-forget, does not block the UI
    private fun sendTrigger(url: String, id: String, label: String) {
        val bodyJson = JSONObject().apply {
            put("id", id)
        }.toString()

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        Log.d("MatchingService", "Matching triggered ($label): ${it.code}")
                    } else {
                        Log.e("MatchingService", "Server returned an error ($label): ${it.code}")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("MatchingService", "Failed to call matching server ($label)", e)
            }
        })
    }
}