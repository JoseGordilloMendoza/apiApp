package com.example.apiapp.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.apiapp.data.repository.CharacterRepository
import com.example.apiapp.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CharacterRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val previousCount = repository.characters.first().size
        return repository.refreshCachedPages().fold(
            onSuccess = { newTotal ->
                if (newTotal > previousCount) {
                    notificationHelper.notifySyncComplete(newItemsFound = true)
                }
                Result.success()
            },
            onFailure = { Result.retry() }
        )
    }
}
