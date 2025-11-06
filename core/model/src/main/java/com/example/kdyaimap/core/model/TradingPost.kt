package com.example.kdyaimap.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.*

@Entity(tableName = "trading_posts")
data class TradingPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // 帖子标题
    val description: String, // 详细描述
    val category: TradingCategory, // 交易分类
    val price: Double, // 价格
    val isNegotiable: Boolean = false, // 是否可议价
    val condition: ItemCondition, // 物品状况
    val images: List<String> = emptyList(), // 图片URL列表
    val location: String, // 交易地点
    val latitude: Double, // 纬度
    val longitude: Double, // 经度
    val contactInfo: String, // 联系方式
    val contactType: ContactType, // 联系方式类型
    val authorId: Long, // 发布者ID
    val authorName: String, // 发布者姓名
    val isAnonymous: Boolean = false, // 是否匿名发布
    val viewCount: Int = 0, // 浏览次数
    val likeCount: Int = 0, // 点赞数
    val status: PostStatus = PostStatus.ACTIVE, // 帖子状态
    val tags: List<String> = emptyList(), // 标签
    val createdAt: Long = System.currentTimeMillis(), // 创建时间
    val updatedAt: Long = System.currentTimeMillis(), // 更新时间
    val expiresAt: Long? = null // 过期时间
)

enum class TradingCategory {
    // 科技类
    ELECTRONICS,        // 电子产品
    DIGITAL_GOODS,     // 数码产品
    COMPUTER_ACCESSORIES, // 电脑配件
    GAMING_EQUIPMENT,  // 游戏设备
    
    // 实体物品
    BOOKS,             // 图书教材
    CLOTHING,          // 服装鞋包
    DAILY_NECESSITIES, // 生活用品
    SPORTS_GOODS,      // 运动器材
    TRANSPORTATION,    // 交通工具
    FURNITURE,         // 家具家电
    FOOD_HEALTH,       // 食品保健
    BEAUTY_COSMETICS,  // 美妆护肤
    
    // 虚拟物品
    TICKETS,           // 票券卡证
    DIGITAL_CONTENT,   // 数字内容
    GAME_ACCOUNTS,     // 游戏账号
    MEMBERSHIPS,       // 会员服务
    
    // 服务技能
    SERVICES,          // 服务技能
    TUTORING,          // 家教辅导
    DESIGN_CREATIVE,   // 设计创意
    
    // 其他
    OTHERS             // 其他
}

enum class ItemCondition {
    BRAND_NEW,      // 全新
    LIKE_NEW,       // 九成新
    GOOD,           // 八成新
    FAIR,           // 七成新
    POOR            // 六成新及以下
}

enum class ContactType {
    PHONE,          // 电话
    WECHAT,         // 微信
    QQ,             // QQ
    EMAIL,          // 邮箱
    WEIBO,          // 微博
    DOUYIN,         // 抖音
    KUAISHOU,       // 快手
    XIAOHONGSHU,    // 小红书
    BILIBILI,       // 哔哩哔哩
    ZHIHU,          // 知乎
    GITHUB,         // GitHub
    LINKEDIN,       // LinkedIn
    IN_PERSON,      // 面议
    OTHER           // 其他
}

enum class PostStatus {
    ACTIVE,         // 活跃
    SOLD,           // 已售出
    EXPIRED,        // 已过期
    HIDDEN,         // 已隐藏
    DELETED         // 已删除
}

// 扩展函数，获取分类的中文名称
fun TradingCategory.getDisplayName(): String {
    return when (this) {
        // 科技类
        TradingCategory.ELECTRONICS -> "电子产品"
        TradingCategory.DIGITAL_GOODS -> "数码产品"
        TradingCategory.COMPUTER_ACCESSORIES -> "电脑配件"
        TradingCategory.GAMING_EQUIPMENT -> "游戏设备"
        
        // 实体物品
        TradingCategory.BOOKS -> "图书教材"
        TradingCategory.CLOTHING -> "服装鞋包"
        TradingCategory.DAILY_NECESSITIES -> "生活用品"
        TradingCategory.SPORTS_GOODS -> "运动器材"
        TradingCategory.TRANSPORTATION -> "交通工具"
        TradingCategory.FURNITURE -> "家具家电"
        TradingCategory.FOOD_HEALTH -> "食品保健"
        TradingCategory.BEAUTY_COSMETICS -> "美妆护肤"
        
        // 虚拟物品
        TradingCategory.TICKETS -> "票券卡证"
        TradingCategory.DIGITAL_CONTENT -> "数字内容"
        TradingCategory.GAME_ACCOUNTS -> "游戏账号"
        TradingCategory.MEMBERSHIPS -> "会员服务"
        
        // 服务技能
        TradingCategory.SERVICES -> "服务技能"
        TradingCategory.TUTORING -> "家教辅导"
        TradingCategory.DESIGN_CREATIVE -> "设计创意"
        
        // 其他
        TradingCategory.OTHERS -> "其他"
    }
}

// 扩展函数，获取物品状况的中文名称
fun ItemCondition.getDisplayName(): String {
    return when (this) {
        ItemCondition.BRAND_NEW -> "全新"
        ItemCondition.LIKE_NEW -> "九成新"
        ItemCondition.GOOD -> "八成新"
        ItemCondition.FAIR -> "七成新"
        ItemCondition.POOR -> "六成新及以下"
    }
}

// 扩展函数，获取联系方式的中文名称
fun ContactType.getDisplayName(): String {
    return when (this) {
        ContactType.PHONE -> "电话"
        ContactType.WECHAT -> "微信"
        ContactType.QQ -> "QQ"
        ContactType.EMAIL -> "邮箱"
        ContactType.WEIBO -> "微博"
        ContactType.DOUYIN -> "抖音"
        ContactType.KUAISHOU -> "快手"
        ContactType.XIAOHONGSHU -> "小红书"
        ContactType.BILIBILI -> "哔哩哔哩"
        ContactType.ZHIHU -> "知乎"
        ContactType.GITHUB -> "GitHub"
        ContactType.LINKEDIN -> "LinkedIn"
        ContactType.IN_PERSON -> "面议"
        ContactType.OTHER -> "其他"
    }
}

// 扩展函数，获取分类分组
fun TradingCategory.getCategoryGroup(): String {
    return when (this) {
        // 科技类
        TradingCategory.ELECTRONICS,
        TradingCategory.DIGITAL_GOODS,
        TradingCategory.COMPUTER_ACCESSORIES,
        TradingCategory.GAMING_EQUIPMENT -> "科技"
        
        // 实体物品
        TradingCategory.BOOKS,
        TradingCategory.CLOTHING,
        TradingCategory.DAILY_NECESSITIES,
        TradingCategory.SPORTS_GOODS,
        TradingCategory.TRANSPORTATION,
        TradingCategory.FURNITURE,
        TradingCategory.FOOD_HEALTH,
        TradingCategory.BEAUTY_COSMETICS -> "实体"
        
        // 虚拟物品
        TradingCategory.TICKETS,
        TradingCategory.DIGITAL_CONTENT,
        TradingCategory.GAME_ACCOUNTS,
        TradingCategory.MEMBERSHIPS -> "虚拟"
        
        // 服务技能
        TradingCategory.SERVICES,
        TradingCategory.TUTORING,
        TradingCategory.DESIGN_CREATIVE -> "服务"
        
        // 其他
        TradingCategory.OTHERS -> "其他"
    }
}