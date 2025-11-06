package com.example.kdyaimap.core.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * POI数据传输对象
 * 与Cloudflare D1数据库的 PointsOfInterest 表结构完全对应
 */
data class PoiDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("category")
    val category: String, // 后端返回的是字符串，如 "FOOD"
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("createdAt")
    val createdAt: String? // 后端返回的是字符串格式的时间
)

/**
 * Cloudflare Worker返回的Point数据模型
 * 与后端数据库结构完全对应
 */
data class PointDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?
)

/**
 * 扩展函数：将PointDto转换为PoiDto
 */
fun PointDto.toPoiDto(): PoiDto {
    return PoiDto(
        id = this.id,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        category = this.category,
        description = this.description,
        createdAt = this.createdAt
    )
}

/**
 * 扩展函数：将PoiDto转换为PointDto
 */
fun PoiDto.toPointDto(): PointDto {
    return PointDto(
        id = this.id,
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        category = this.category,
        description = this.description,
        createdAt = this.createdAt
    )
}