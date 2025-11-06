package com.example.kdyaimap.util

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val backupPath = applicationContext.getExternalFilesDir(null)?.absolutePath + "/campus_nav_db.bak"
            DatabaseBackupHelper.backupDatabase(applicationContext, backupPath)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}