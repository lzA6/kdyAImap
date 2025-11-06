package com.example.kdyaimap.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val role: UserRole,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val contactInfo: ByteArray,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val iv: ByteArray,
    @SerializedName("avatar")
    val avatar: String = "", // 头像URL
    @SerializedName("bio")
    val bio: String = "", // 个人简介
    @SerializedName("email")
    val email: String = "", // 邮箱
    @SerializedName("phone")
    val phone: String = "", // 电话
    @SerializedName("followersCount")
    val followersCount: Int = 0, // 粉丝数
    @SerializedName("followingCount")
    val followingCount: Int = 0, // 关注数
    @SerializedName("postsCount")
    val postsCount: Int = 0, // 发布数
    @SerializedName("likesCount")
    val likesCount: Int = 0, // 获赞数
    @SerializedName("isVerified")
    val isVerified: Boolean = false, // 是否认证
    @SerializedName("isAnonymous")
    val isAnonymous: Boolean = false, // 是否为匿名用户
    @SerializedName("anonymousId")
    val anonymousId: String = "", // 匿名用户唯一标识
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(), // 创建时间
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis(), // 更新时间
    @SerializedName("lastActiveAt")
    val lastActiveAt: Long = System.currentTimeMillis() // 最后活跃时间
) {
    // 用于网络传输的构造函数，处理字符串类型的role
    constructor(
        id: Long = 0,
        username: String,
        passwordHash: String,
        role: String,
        contactInfo: ByteArray = ByteArray(0),
        iv: ByteArray = ByteArray(0),
        avatar: String = "",
        bio: String = "",
        email: String = "",
        phone: String = "",
        followersCount: Int = 0,
        followingCount: Int = 0,
        postsCount: Int = 0,
        likesCount: Int = 0,
        isVerified: Boolean = false,
        isAnonymous: Boolean = false,
        anonymousId: String = "",
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis(),
        lastActiveAt: Long = System.currentTimeMillis()
    ) : this(
        id = id,
        username = username,
        passwordHash = passwordHash,
        role = UserRole.valueOf(role.uppercase()),
        contactInfo = contactInfo,
        iv = iv,
        avatar = avatar,
        bio = bio,
        email = email,
        phone = phone,
        followersCount = followersCount,
        followingCount = followingCount,
        postsCount = postsCount,
        likesCount = likesCount,
        isVerified = isVerified,
        isAnonymous = isAnonymous,
        anonymousId = anonymousId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastActiveAt = lastActiveAt
    )
    
    // 获取role的字符串形式，用于JSON序列化
    fun getRoleString(): String = role.name
}