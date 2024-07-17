package otus.homework.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CatFactViewModel(
    private val catsFactService: CatsService,
    private val catsPicturesService: CatsPicturesService
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result<CatData>>()
    val catsLiveData: LiveData<Result<CatData>> get() = _catsLiveData
    private var fetchJob: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _catsLiveData.value = Result.Error(throwable)
    }

    fun fetchCatData() {
        cancelFetch()

        fetchJob = viewModelScope.launch(exceptionHandler) {
            val factRequest = async { catsFactService.getCatFact() }
            val imageRequest = async { catsPicturesService.getCatPicture() }

            val fact = factRequest.await()
            val picture = imageRequest.await()

            _catsLiveData.value =
                Result.Success(CatData(fact.fact, picture.first().url))
        }
    }

    fun cancelFetch() {
        fetchJob?.cancel()
        fetchJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelFetch()
    }
}