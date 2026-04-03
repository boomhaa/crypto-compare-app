package com.cryptocompare.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cryptocompare.domain.usecase.pairs.RefreshCatalogUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class RefreshCatalogWorker @AssistedInject constructor(
    @Assisted appParams: WorkerParameters,
    @Assisted context: Context,
    private val refreshCatalogUseCase: RefreshCatalogUseCase
) : CoroutineWorker(context, appParams) {
    override suspend fun doWork(): Result =
        refreshCatalogUseCase().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
}
