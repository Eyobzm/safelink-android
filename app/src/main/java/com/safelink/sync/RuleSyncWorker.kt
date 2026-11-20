package com.safelink.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safelink.data.repository.RuleRepository
import com.safelink.data.repository.SyncResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RuleSyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val rules: RuleRepository,
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = when (rules.syncDelta()) {
        SyncResult.APPLIED, SyncResult.UNCHANGED -> Result.success()
        SyncResult.REJECTED -> Result.failure() // keep the previous rule set (FR-SYNC-04)
        SyncResult.RETRY -> Result.retry()
    }
}
