package com.example.kdyaimap.core.model

enum class EventType {
    TRADE,          // 二手交易
    SOCIAL,         // 社交邀约
    HELP,           // 生活互助
    STUDY,          // 学习交流
    CLUB_ACTIVITY,  // 社团活动
    ACADEMIC,       // 学术交流
    SPORTS,         // 体育运动
    ENTERTAINMENT,  // 娱乐活动
    VOLUNTEER,      // 志愿服务
    OTHER,          // 其他
    // [NEW] 添加与后端匹配的分类
    FOOD,           // 餐饮美食
    SERVICE,        // 便民服务
    DORMITORY       // 生活住宿
}

enum class EventStatus {
    PENDING_REVIEW, // 待审核
    APPROVED,       // 已批准
    REJECTED,       // 已拒绝
    CLOSED,         // 已关闭
    CANCELLED       // 已取消
}

enum class UserRole {
    STUDENT,        // 学生
    CLUB_ADMIN,     // 社团管理员
    SUPER_ADMIN     // 超级管理员
}

enum class HistoryType {
    VIEWED,         // 查看
    PUBLISHED       // 发布
}