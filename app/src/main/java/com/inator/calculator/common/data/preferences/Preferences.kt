package com.inator.calculator.common.data.preferences

import com.inator.calculator.model.Rate

interface Preferences {

    fun putMeasure(measure: String)

    fun putCurrencySpinnerOne(rate: Rate)

    fun putCurrencySpinnerTwo(rate: Rate)

    fun putConverterSpinnerOne(position: Int)

    fun putConverterSpinnerTwo(position: Int)

    fun getMeasure(): String

    fun getCurrencySpinnerOne(): String

    fun getCurrencySpinnerTwo(): String

    fun getConverterSpinnerOne(): Int

    fun getConverterSpinnerTwo(): Int

    fun deleteSpinners()
}
