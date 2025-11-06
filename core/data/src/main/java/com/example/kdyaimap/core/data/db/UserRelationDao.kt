package com.example.kdyaimap.core.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserRelation

@Dao
interface UserRelationDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followUser(relation: UserRelation)
    
    @Delete
    suspend fun unfollowUser(relation: UserRelation)
    
    @Query("SELECT * FROM user_relations WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun getRelation(followerId: Long, followingId: Long): UserRelation?
    
    @Query("SELECT COUNT(*) FROM user_relations WHERE followingId = :userId")
    fun getFollowersCount(userId: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM user_relations WHERE followerId = :userId")
    fun getFollowingCount(userId: Long): Flow<Int>
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN user_relations ur ON u.id = ur.followingId
        WHERE ur.followerId = :followerId
        ORDER BY ur.followTimestamp DESC
    """)
    fun getFollowingUsers(followerId: Long): Flow<List<User>>
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN user_relations ur ON u.id = ur.followerId
        WHERE ur.followingId = :followingId
        ORDER BY ur.followTimestamp DESC
    """)
    fun getFollowers(followingId: Long): Flow<List<User>>
    
    @Query("""
        SELECT u.* FROM users u
        WHERE u.id IN (
            SELECT ur.followingId FROM user_relations ur
            WHERE ur.followerId = :userId
        ) AND u.id IN (
            SELECT ur.followerId FROM user_relations ur
            WHERE ur.followingId = :userId
        )
    """)
    fun getMutualFollows(userId: Long): Flow<List<User>>
    
    @Query("DELETE FROM user_relations WHERE followerId = :userId OR followingId = :userId")
    suspend fun deleteUserRelations(userId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM user_relations WHERE followerId = :followerId AND followingId = :followingId)")
    suspend fun isFollowing(followerId: Long, followingId: Long): Boolean
}