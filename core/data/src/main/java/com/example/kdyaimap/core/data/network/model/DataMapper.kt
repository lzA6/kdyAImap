package com.example.kdyaimap.core.data.network.model

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.EventType

/**
 * 数据映射器
 * 将网络数据模型转换为App内部使用的领域模型
 */

/**
 * 将PoiDto转换为CampusEvent
 */
fun PoiDto.toCampusEvent(): CampusEvent {
    return CampusEvent(
        id = this.id,
        title = this.name,
        description = this.description ?: "",
        latitude = this.latitude,
        longitude = this.longitude,
        locationName = this.name, // 暂时使用name作为locationName
        eventType = this.category, // 直接使用字符串，保持与后端一致
        authorId = 0L, // 后端模型暂无此字段，设为默认值
        creationTimestamp = System.currentTimeMillis(), // 后端模型暂无此字段，设为默认值
        status = EventStatus.APPROVED // 假设从后端获取的都是已批准的
    )
}

/**
 * 将PoiDto列表转换为CampusEvent列表
 */
fun List<PoiDto>.toCampusEvents(): List<CampusEvent> {
    return this.map { it.toCampusEvent() }
}

/**
 * 将CampusEvent转换为PoiDto（用于上传数据到后端）
 */
fun CampusEvent.toPoiDto(): PoiDto {
    return PoiDto(
        id = this.id,
        name = this.title,
        latitude = this.latitude,
        longitude = this.longitude,
        category = this.eventType,
        description = this.description.ifEmpty { null },
        createdAt = null // 上传时不需要此字段
    )
}

/**
 * 将CampusEvent列表转换为PoiDto列表
 */
fun List<CampusEvent>.toPoiDtos(): List<PoiDto> {
    return this.map { it.toPoiDto() }
}