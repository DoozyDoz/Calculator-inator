package com.inator.calculator.conversion.domain.usecases

import android.app.appsearch.SearchResults
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class Convert @Inject constructor(private val animalRepository: AnimalRepository) {

    private val combiningFunction: io.reactivex.functions.Function3<String, String, String, SearchParameters>
        get() = Function3 { query, age, type ->
            SearchParameters(query, age, type)
        }

    operator fun invoke(
        unitOneSubject: BehaviorSubject<String>,
        inputOneSubject: BehaviorSubject<String>,
        unitTwoSubject: BehaviorSubject<String>,
        inputTwoSubject: BehaviorSubject<String>
    ): Flowable<SearchResults> {
        val inputOne = inputOneSubject
            .debounce(250L, TimeUnit.MILLISECONDS)

        val inputTwo = inputTwoSubject
            .debounce(250L, TimeUnit.MILLISECONDS)

        val unitOne = unitOneSubject.replaceUIEmptyValue()
        val unitTwo = unitTwoSubject.replaceUIEmptyValue()

        return Observable.combineLatest(query, age, type, combiningFunction)
            .toFlowable(BackpressureStrategy.LATEST)
            .switchMap { parameters: SearchParameters ->
                animalRepository.searchCachedAnimalsBy(parameters)
            }
    }

    private fun BehaviorSubject<String>.replaceUIEmptyValue() = map {
        if (it == GetSearchFilters.NO_FILTER_SELECTED) "" else it
    }
}