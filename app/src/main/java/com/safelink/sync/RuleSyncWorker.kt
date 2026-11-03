package com.safelink.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safelink.data.repository.RuleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RuleSyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val rules: RuleRepository,
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result =
        if (rules.syncDelta()) Result.success() else Result.retry()  // old rules stay on failure (FR-SYNC-04)
}
