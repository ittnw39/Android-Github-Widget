package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub 저장소 정보를 나타내는 데이터 클래스
 */
data class Repository(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("stargazers_count") val stars: Int,
    @SerializedName("forks_count") val forks: Int,
    @SerializedName("language") val language: String?,
    @SerializedName("updated_at") val updatedAt: String
) 