package com.example.kdyaimap.core.model

import com.google.gson.annotations.SerializedName

// 这个数据类精确匹配我们Cloudflare后端返回的JSON结构
data class PoiItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("category")
    val category: String, // 例如 "FOOD", "STUDY"

    @SerializedName("description")
    val description: String?,

    @SerializedName("createdAt")
    val createdAt: String? // 后端返回的是字符串格式的时间
)