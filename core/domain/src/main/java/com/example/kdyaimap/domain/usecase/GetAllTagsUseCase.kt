package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.MapTag
import com.example.kdyaimap.domain.repository.MapTagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTagsUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    operator fun invoke(): Flow<List<MapTag>> = mapTagRepository.getAllTags()
}

class GetActiveTagsUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    operator fun invoke(): Flow<List<MapTag>> = mapTagRepository.getActiveTags()
}

class CreateTagUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    suspend operator fun invoke(tag: MapTag): Long {
        // 验证标签名称不能为空
        require(tag.name.isNotBlank()) { "标签名称不能为空" }
        
        // 检查标签名称是否已存在
        val existingTag = mapTagRepository.getTagByName(tag.name.trim())
        require(existingTag == null) { "标签名称已存在" }
        
        return mapTagRepository.insertTag(
            tag.copy(name = tag.name.trim())
        )
    }
}

class UpdateTagUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    suspend operator fun invoke(tag: MapTag) {
        require(tag.name.isNotBlank()) { "标签名称不能为空" }
        
        // 检查标签名称是否与其他标签重复
        val existingTag = mapTagRepository.getTagByName(tag.name.trim())
        require(existingTag == null || existingTag.id == tag.id) { "标签名称已存在" }
        
        mapTagRepository.updateTag(
            tag.copy(name = tag.name.trim())
        )
    }
}

class DeleteTagUseCase @Inject constructor(
    private val mapTagRepository: MapTagRepository
) {
    suspend operator fun invoke(tag: MapTag) {
        mapTagRepository.deleteTag(tag)
    }
}