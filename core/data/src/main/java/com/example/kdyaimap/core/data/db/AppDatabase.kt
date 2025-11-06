package com.example.kdyaimap.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserHistory
import com.example.kdyaimap.core.model.UserRelation
import com.example.kdyaimap.core.model.MapTag
import com.example.kdyaimap.core.model.TaggedLocation
import com.example.kdyaimap.core.model.TagScheme

@Database(
    entities = [
        User::class,
        CampusEvent::class,
        UserHistory::class,
        UserRelation::class,
        MapTag::class,
        TaggedLocation::class,
        TagScheme::class
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(Converters::class, TagSchemeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun campusEventDao(): CampusEventDao
    abstract fun userHistoryDao(): UserHistoryDao
    abstract fun userRelationDao(): UserRelationDao
    abstract fun mapTagDao(): MapTagDao
    abstract fun taggedLocationDao(): TaggedLocationDao
    abstract fun tagSchemeDao(): TagSchemeDao
}