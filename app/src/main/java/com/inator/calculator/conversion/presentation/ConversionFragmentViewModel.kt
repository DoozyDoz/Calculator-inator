package com.inator.calculator.conversion.presentation

import android.app.Application
import android.util.Log.d
import android.view.SearchEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inator.calculator.R
import com.inator.calculator.common.data.preferences.Preferences
import com.inator.calculator.conversion.domain.usecases.Convert
import com.inator.calculator.conversion.domain.usecases.SearchAnimals
import com.inator.calculator.extensions.createExceptionHandler
import com.inator.calculator.repository.ConverterRepository
import com.inator.calculator.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.logging.Logger

//This class is similar to CurrencyInputViewModel
@HiltViewModel
class ConversionFragmentViewModel(
    application: Application,
    private val preferences: Preferences,
    private val convert: Convert,
    ) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ConversionViewState())
    private val unitOneSubject = BehaviorSubject.create<String>()
    private val inputOneSubject = BehaviorSubject.create<String>()
    private val inputTwoSubject = BehaviorSubject.create<String>()
    private val unitTwoSubject = BehaviorSubject.create<String>()

    val state: StateFlow<ConversionViewState> = _state.asStateFlow()

    fun onEvent(event: ConversionEvent) {
        when (event) {
            is ConversionEvent.PrepareToConvert -> prepareToConvert()
            is ConversionEvent.SetMeasure -> setMeasure(event.measure)
            else -> onConversionParametersUpdate(event)
        }
    }

    private fun prepareToConvert() {
        loadMeasure()
        loadUnitValues()
        setupConversionSubscription()
    }

    private fun setupConversionSubscription() {
        searchAnimals(querySubject, ageSubject, typeSubject)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSearchResults(it) },
                { onFailure(it) }
            )
            .addTo(compositeDisposable)
    }

    private fun loadMeasure() {
        _state.update { oldState ->
            oldState.copy(chipName = preferences.getMeasure())
        }
    }

    private fun setMeasure(measure: String) {
        preferences.putMeasure(measure)
        _state.update { oldState ->
            oldState.copy(chipName = preferences.getMeasure())
        }
    }

    private fun loadUnitValues() {
        val exceptionHandler = createExceptionHandler(message = "Failed to get unit values!")

        viewModelScope.launch(exceptionHandler) {
            val (ages, types) = getSearchFilters()
            updateStateWithFilterValues(ages, types)
        }
    }

    private fun createExceptionHandler(message: String): CoroutineExceptionHandler {
        return viewModelScope.createExceptionHandler(message) {
//            onFailure(it)
        }
    }

    private fun onConversionParametersUpdate(event: ConversionEvent) {
        when (event) {
            is ConversionEvent.UnitOneSelected -> updateUnitOne(event.unit)
            is ConversionEvent.UnitTwoSelected -> updateUnitTwo(event.unit)
            is ConversionEvent.InputOneInput -> updateInputOne(event.input)
            is ConversionEvent.InputTwoInput -> updateInputTwo(event.input)
            else -> Logger.d("Wrong SearchEvent in onSearchParametersUpdate!")
        }
    }

    private fun updateUnitOne(unit: String) {
        unitOneSubject.onNext(unit)
    }

    private fun updateUnitTwo(unit: String) {
        unitTwoSubject.onNext(unit)
    }

    private fun updateInputOne(input: String) {
        inputOneSubject.onNext(input)
    }

    private fun updateInputTwo(input: String) {
        inputTwoSubject.onNext(input)
    }


    private val preferenceRepository = PreferenceRepository.getInstance(application)
    private val converterRepository = ConverterRepository.getInstance(application)

    private val measure: MutableLiveData<String> by lazy {
        MutableLiveData("Length")
    }

    private val outputDirect: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }

    private val outputReverse: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }

    private val currentInput1: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    private val currentInput2: MutableLiveData<String> by lazy {
        MutableLiveData("")
    }
    private val spinnerFrom: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }
    private val spinnerTo: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }


    fun setSpinner1(position: Int) {
        preferenceRepository.saveConverterSpinner1Prefs(position)
        spinnerFrom.value = position
        calculateDirect()
    }

    fun setSpinner2(position: Int) {
        preferenceRepository.saveConverterSpinner2Prefs(position)
        spinnerTo.value = position
        calculateDirect()
    }


    fun setInput1(string: String) {
        currentInput1.value = string
        if (!currentInput1.value.isNullOrEmpty()) {
            calculateDirect()
        }
    }

    fun setInput2(string: String) {
        currentInput2.value = string
        if (!currentInput2.value.isNullOrEmpty()) {
            calculateReverse()
        }
    }

    private fun calculateReverse() {
        outputReverse.value = evaluateUnitConversion(
            currentInput2,
            spinnerTo,
            spinnerFrom,
            measure
        )
    }

    private fun calculateDirect() {
        outputDirect.value = evaluateUnitConversion(
            currentInput1,
            spinnerFrom,
            spinnerTo,
            measure
        )
    }

    fun getOutputDirect(): LiveData<String> = outputDirect

    fun getOutputReverse(): LiveData<String> = outputReverse

    fun getMeasure(): LiveData<String> = measure

    private fun evaluateUnitConversion(
        input: LiveData<String>,
        fromSpinnerPosition: LiveData<Int>,
        toSpinnerPosition: LiveData<Int>,
        measure: LiveData<String>
    ): String {
        if (!input.value.isNullOrEmpty()) {
            return if (measure.value.equals("Temperature")) {
                converterRepository.evaluateTemperature(
                    input,
                    fromSpinnerPosition,
                    toSpinnerPosition
                )
            } else {
                val conversionRates =
                    converterRepository.getConversionValues(measure.value!!)
                val fromRate = conversionRates[fromSpinnerPosition.value!!]
                val toRate = conversionRates[toSpinnerPosition.value!!]
                (input.value?.toDouble()!! * toRate / fromRate).toString()
            }
        }
        return "0"
    }

    fun getSavedMeasure(): Int {
        //We return the chip Id as per the measure
        return when (preferenceRepository.getMeasurePrefs()) {
            "Length" -> R.id.length
            "Area" -> R.id.area
            "Mass" -> R.id.mass
            "Speed" -> R.id.speed
            "Data" -> R.id.data
            "Volume" -> R.id.volume
            "Time" -> R.id.time
            "Temperature" -> R.id.temperature
            "Angle" -> R.id.angle
            else -> R.id.length
        }
    }

    fun getSavedSpinner1(): Int {
        return preferenceRepository.getConverterSpinner1()
    }

    fun getSavedSpinner2(): Int {
        return preferenceRepository.getConverterSpinner2()
    }

    fun clearSavedSpinners() {
        preferenceRepository.clearSpinnerSelections()
    }
}