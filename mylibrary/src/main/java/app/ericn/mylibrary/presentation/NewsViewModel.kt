package app.ericn.ericsweather.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.ericn.android_common.StringProvider
import app.ericn.ericsweather.core.Article
import app.ericn.ericsweather.core.NewsInteractor
import app.ericn.mylibrary.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class NewsViewModel(
    private val interactor: NewsInteractor,
    private val stringProvider: StringProvider,
    private val searchSubject: Observable<String>
) : ViewModel() {

    private val viewState = MutableLiveData<ViewState>()
    val viewStateReadOnly: LiveData<ViewState> = viewState
    private val disposables = CompositeDisposable()

    init {
        searchSubject.subscribeOn(Schedulers.io()).flatMapSingle { cityName ->
            interactor(cityName)
        }.observeOn(AndroidSchedulers.mainThread()).subscribe({ articles ->
            viewState.value = ViewState.DataLoaded(articles)
        }, { t ->
            viewState.value =
                ViewState.Error(t.message ?: stringProvider.getString(R.string.error_generic))
        }).addTo(disposables)
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

    sealed class ViewState {
        data class DataLoaded(val articles: List<Article>) : ViewState()

        object Loading : ViewState()
        data class Error(val message: String) : ViewState()
    }
}
