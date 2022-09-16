package com.inator.calculator.conversion.domain.usecases

import com.inator.calculator.conversion.domain.model.ConversionParameters
import com.inator.calculator.conversion.domain.model.ConversionResults
import com.inator.calculator.repository.ConverterRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import io.reactivex.functions.Function5

class Convert @Inject constructor(private val converterRepository: ConverterRepository) {

    private val combiningFunction: Function5<String, String, String, String, String, ConversionParameters>
        get() = Function5 { inputOne, inputTwo, unitOne, unitTwo, measure ->
            ConversionParameters(measure, unitOne, inputOne, unitTwo, inputTwo)
        }

    operator fun invoke(
        unitOneSubject: BehaviorSubject<String>,
        inputOneSubject: BehaviorSubject<String>,
        unitTwoSubject: BehaviorSubject<String>,
        inputTwoSubject: BehaviorSubject<String>,
        measureSubject: BehaviorSubject<String>
    ): Flowable<ConversionResults> {
        val inputOne = inputOneSubject
            .debounce(250L, TimeUnit.MILLISECONDS)

        val inputTwo = inputTwoSubject
            .debounce(250L, TimeUnit.MILLISECONDS)

        val unitOne = unitOneSubject
        val unitTwo = unitTwoSubject
        val measure = measureSubject

        return Observable.combineLatest(
            inputOne,
            inputTwo,
            unitOne,
            unitTwo,
            measure,
            combiningFunction
        )
            .toFlowable(BackpressureStrategy.LATEST)
            .switchMap { parameters: ConversionParameters ->
                converterRepository.convertUsing(parameters)
            }
    }

}