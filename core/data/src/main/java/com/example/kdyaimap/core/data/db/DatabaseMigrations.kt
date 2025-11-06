package com.example.kdyaimap.core.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    
    // 从版本 1 到版本 2 的迁移：添加用户扩展字段
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 添加用户扩展字段
            database.execSQL("ALTER TABLE users ADD COLUMN avatar TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE users ADD COLUMN bio TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE users ADD COLUMN email TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE users ADD COLUMN phone TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE users ADD COLUMN followersCount INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN followingCount INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN postsCount INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN likesCount INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN isVerified INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN isAnonymous INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN anonymousId TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE users ADD COLUMN createdAt INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN updatedAt INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE users ADD COLUMN lastActiveAt INTEGER DEFAULT 0")
            // 注意：iv 字段在原始 schema 中已经存在，所以不需要添加
        }
    }
    
    // 从版本 2 到版本 3 的迁移：添加用户关系表
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建用户关系表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS user_relations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    followerId INTEGER NOT NULL,
                    followingId INTEGER NOT NULL,
                    followTimestamp INTEGER NOT NULL,
                    FOREIGN KEY(followerId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(followingId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_user_relations_followerId ON user_relations (followerId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_user_relations_followingId ON user_relations (followingId)")
        }
    }
    
    // 从版本 3 到版本 4 的迁移：添加地图标签相关表
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建地图标签表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS map_tags (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    color TEXT NOT NULL,
                    icon TEXT NOT NULL,
                    creatorId INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(creatorId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建标签位置表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS tagged_locations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    tagId INTEGER NOT NULL,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    creatorId INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(tagId) REFERENCES map_tags(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(creatorId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_map_tags_creatorId ON map_tags (creatorId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tagged_locations_tagId ON tagged_locations (tagId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tagged_locations_creatorId ON tagged_locations (creatorId)")
        }
    }
    
    // 从版本 4 到版本 5 的迁移：添加标签方案表
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建标签方案表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS tag_schemes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    tags TEXT NOT NULL,
                    creatorId INTEGER NOT NULL,
                    isPublic INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(creatorId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tag_schemes_creatorId ON tag_schemes (creatorId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tag_schemes_isPublic ON tag_schemes (isPublic)")
        }
    }
    
    // 所有迁移的数组
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5
    )
}