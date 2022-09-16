package com.inator.calculator.conversion.domain.usecases

import com.inator.calculator.conversion.domain.model.ConversionParameters
import com.inator.calculator.conversion.domain.model.ConversionResults
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class Convert @Inject constructor(private val animalRepository: AnimalRepository) {

    private val combiningFunction: Function4<String, String, String, String, ConversionParameters>
        get() = Function4 { inputOne, inputTwo, unitOne, unitTwo ->
            ConversionParameters(unitOne, inputOne, unitTwo, inputTwo)
        }

    operator fun invoke(
        unitOneSubject: BehaviorSubject<String>,
        inputOneSubject: BehaviorSubject<String>,
        unitTwoSubject: BehaviorSubject<String>,
        inputTwoSubject: BehaviorSubject<String>
    ): Flowable<ConversionResults> {
        val inputOne = inputOneSubject
            .debounce(250L, TimeUnit.MILLISECONDS)

        val inputTwo = inputTwoSubject
            .debounce(250L, TimeUnit.MILLISECONDS)

        val unitOne = unitOneSubject.replaceUIEmptyValue()
        val unitTwo = unitTwoSubject.replaceUIEmptyValue()

        return Observable.combineLatest(inputOne, inputTwo, unitOne, unitTwo, combiningFunction)
            .toFlowable(BackpressureStrategy.LATEST)
            .switchMap { parameters: ConversionParameters ->
                animalRepository.searchCachedAnimalsBy(parameters)
            }
    }

    private fun BehaviorSubject<String>.replaceUIEmptyValue() = map {
        if (it == GetSearchFilters.NO_FILTER_SELECTED) "" else it
    }
}