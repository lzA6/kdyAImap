package com.example.kdyaimap.util

import android.content.Context
import com.example.kdyaimap.core.data.db.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object DatabaseBackupHelper {

    private const val DB_NAME = "campus_nav_db"

    fun backupDatabase(context: Context, backupPath: String): Boolean {
        val dbFile = context.getDatabasePath(DB_NAME)
        val backupFile = File(backupPath)

        if (!dbFile.exists()) {
            return false
        }

        return try {
            FileInputStream(dbFile).channel.use { input ->
                FileOutputStream(backupFile).channel.use { output ->
                    output.transferFrom(input, 0, input.size())
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun restoreDatabase(context: Context, backupPath: String): Boolean {
        val dbFile = context.getDatabasePath(DB_NAME)
        val backupFile = File(backupPath)

        if (!backupFile.exists()) {
            return false
        }

        return try {
            // Close the database before restoring
            // This is a simplified example. In a real app, you'd get the DB instance via Hilt
            // and ensure it's properly closed.
            // AppDatabase.getInstance(context).close()

            FileInputStream(backupFile).channel.use { input ->
                FileOutputStream(dbFile).channel.use { output ->
                    output.transferFrom(input, 0, input.size())
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}