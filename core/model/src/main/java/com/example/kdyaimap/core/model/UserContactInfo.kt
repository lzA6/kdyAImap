package com.example.kdyaimap.core.model

data class UserContactInfo(
    val userId: Long,
    val phone: String? = null,
    val wechat: String? = null,
    val qq: String? = null,
    val email: String? = null,
    val weibo: String? = null,
    val douyin: String? = null,
    val kuaishou: String? = null,
    val xiaohongshu: String? = null,
    val bilibili: String? = null,
    val zhihu: String? = null,
    val github: String? = null,
    val linkedin: String? = null,
    val preferredContact: ContactType = ContactType.PHONE,
    val showPhone: Boolean = false,
    val showWechat: Boolean = false,
    val showQq: Boolean = false,
    val showEmail: Boolean = false,
    val showWeibo: Boolean = false,
    val showDouyin: Boolean = false,
    val showKuaishou: Boolean = false,
    val showXiaohongshu: Boolean = false,
    val showBilibili: Boolean = false,
    val showZhihu: Boolean = false,
    val showGithub: Boolean = false,
    val showLinkedin: Boolean = false
)

fun ContactType.getIconName(): String {
    return when (this) {
        ContactType.PHONE -> "phone"
        ContactType.WECHAT -> "wechat"
        ContactType.QQ -> "qq"
        ContactType.EMAIL -> "email"
        ContactType.WEIBO -> "weibo"
        ContactType.DOUYIN -> "douyin"
        ContactType.KUAISHOU -> "kuaishou"
        ContactType.XIAOHONGSHU -> "xiaohongshu"
        ContactType.BILIBILI -> "bilibili"
        ContactType.ZHIHU -> "zhihu"
        ContactType.GITHUB -> "github"
        ContactType.LINKEDIN -> "linkedin"
        ContactType.IN_PERSON -> "person"
        ContactType.OTHER -> "other"
    }
}