package com.example.kdyaimap.core.data.network

import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserRole
import com.google.gson.*
import java.lang.reflect.Type

/**
 * User类的自定义Gson类型适配器
 * 处理网络传输中的User对象序列化和反序列化
 */
class UserTypeAdapter : JsonDeserializer<User>, JsonSerializer<User> {
    
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): User {
        val jsonObject = json.asJsonObject
        
        // 处理可能为null的字段
        val id = jsonObject.get("id")?.asLong ?: 0L
        val username = jsonObject.get("username")?.asString ?: ""
        val passwordHash = jsonObject.get("passwordHash")?.asString ?: ""
        val roleStr = jsonObject.get("role")?.asString ?: UserRole.STUDENT.name
        val avatar = jsonObject.get("avatar")?.asString ?: ""
        val bio = jsonObject.get("bio")?.asString ?: ""
        val email = jsonObject.get("email")?.asString ?: ""
        val phone = jsonObject.get("phone")?.asString ?: ""
        val followersCount = jsonObject.get("followersCount")?.asInt ?: 0
        val followingCount = jsonObject.get("followingCount")?.asInt ?: 0
        val postsCount = jsonObject.get("postsCount")?.asInt ?: 0
        val likesCount = jsonObject.get("likesCount")?.asInt ?: 0
        val isVerified = jsonObject.get("isVerified")?.asBoolean ?: false
        val isAnonymous = jsonObject.get("isAnonymous")?.asBoolean ?: false
        val anonymousId = jsonObject.get("anonymousId")?.asString ?: ""
        val createdAt = jsonObject.get("createdAt")?.asLong ?: System.currentTimeMillis()
        val updatedAt = jsonObject.get("updatedAt")?.asLong ?: System.currentTimeMillis()
        val lastActiveAt = jsonObject.get("lastActiveAt")?.asLong ?: System.currentTimeMillis()
        
        // 将字符串role转换为UserRole枚举
        val role = try {
            UserRole.valueOf(roleStr.uppercase())
        } catch (e: IllegalArgumentException) {
            UserRole.STUDENT
        }
        
        return User(
            id = id,
            username = username,
            passwordHash = passwordHash,
            role = role,
            contactInfo = ByteArray(0), // 网络传输时不包含contactInfo
            iv = ByteArray(0), // 网络传输时不包含iv
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
    }
    
    override fun serialize(src: User, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        
        jsonObject.addProperty("id", src.id)
        jsonObject.addProperty("username", src.username)
        jsonObject.addProperty("passwordHash", src.passwordHash)
        jsonObject.addProperty("role", src.getRoleString())
        jsonObject.addProperty("avatar", src.avatar)
        jsonObject.addProperty("bio", src.bio)
        jsonObject.addProperty("email", src.email)
        jsonObject.addProperty("phone", src.phone)
        jsonObject.addProperty("followersCount", src.followersCount)
        jsonObject.addProperty("followingCount", src.followingCount)
        jsonObject.addProperty("postsCount", src.postsCount)
        jsonObject.addProperty("likesCount", src.likesCount)
        jsonObject.addProperty("isVerified", src.isVerified)
        jsonObject.addProperty("isAnonymous", src.isAnonymous)
        jsonObject.addProperty("anonymousId", src.anonymousId)
        jsonObject.addProperty("createdAt", src.createdAt)
        jsonObject.addProperty("updatedAt", src.updatedAt)
        jsonObject.addProperty("lastActiveAt", src.lastActiveAt)
        
        return jsonObject
    }
}