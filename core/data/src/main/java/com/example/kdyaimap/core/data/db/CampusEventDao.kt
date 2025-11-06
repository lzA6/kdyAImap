package com.example.kdyaimap.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.kdyaimap.core.model.CampusEvent
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource

@Dao
interface CampusEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CampusEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CampusEvent>)

    @Update
    suspend fun updateEvent(event: CampusEvent)

    @Query("DELETE FROM campus_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)

    @Query("SELECT * FROM campus_events WHERE status = 'APPROVED' ORDER BY creationTimestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getApprovedEventsPaged(limit: Int, offset: Int): List<CampusEvent>

    @Query("SELECT * FROM campus_events WHERE status = 'APPROVED' ORDER BY creationTimestamp DESC")
    fun getAllApprovedEventsPaging(): PagingSource<Int, CampusEvent>

    @Query("SELECT * FROM campus_events WHERE status = 'APPROVED' ORDER BY creationTimestamp DESC")
    fun getAllApprovedEvents(): Flow<List<CampusEvent>>

    @Query("SELECT * FROM campus_events WHERE eventType = :eventType AND status = 'APPROVED' ORDER BY creationTimestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getApprovedEventsByTypePaged(eventType: String, limit: Int, offset: Int): List<CampusEvent>

    @Query("SELECT * FROM campus_events WHERE eventType = :eventType AND status = 'APPROVED' ORDER BY creationTimestamp DESC")
    fun getApprovedEventsByTypePaging(eventType: String): PagingSource<Int, CampusEvent>

    @Query("SELECT * FROM campus_events WHERE eventType = :eventType AND status = 'APPROVED' ORDER BY creationTimestamp DESC")
    fun getApprovedEventsByType(eventType: String): Flow<List<CampusEvent>>

    @Query("SELECT * FROM campus_events WHERE status = 'PENDING_REVIEW' ORDER BY creationTimestamp ASC")
    fun getPendingEvents(): Flow<List<CampusEvent>>

    @Query("UPDATE campus_events SET status = :newStatus WHERE id = :eventId")
    suspend fun updateEventStatus(eventId: Long, newStatus: String)

    @Query("SELECT * FROM campus_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): CampusEvent?

    @Query("SELECT COUNT(*) FROM campus_events WHERE status = 'APPROVED'")
    suspend fun getApprovedEventsCount(): Int

    @Query("SELECT COUNT(*) FROM campus_events WHERE eventType = :eventType AND status = 'APPROVED'")
    suspend fun getApprovedEventsCountByType(eventType: String): Int

    @RawQuery(observedEntities = [CampusEvent::class])
    fun getFilteredEvents(query: SupportSQLiteQuery): Flow<List<CampusEvent>>

    @RawQuery(observedEntities = [CampusEvent::class])
    suspend fun getFilteredEventsPaged(query: SupportSQLiteQuery): List<CampusEvent>
}