package fr.sdis83.remocra.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.sdis83.remocra.mobile.workers.TokenWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val mutableStateFlow = MutableStateFlow(true)
    val isLoading = mutableStateFlow.asStateFlow()
    val goToMainActivity = MutableLiveData(false)

    companion object {
        private const val TAG: String = "SplashViewModel"
    }

    init {
        val tokenWorker = OneTimeWorkRequestBuilder<TokenWorker>().build()

        WorkManager.getInstance(getApplication()).let { workManager ->
            workManager.enqueue(tokenWorker)
            workManager.getWorkInfoByIdLiveData(tokenWorker.id).observeForever {
                if (it.state == WorkInfo.State.SUCCEEDED || it.state == WorkInfo.State.FAILED) {
                    mutableStateFlow.value = false
                }
                if (it.state == WorkInfo.State.SUCCEEDED) {
                    goToMainActivity.value = true
                }
            }
        }
    }
}
