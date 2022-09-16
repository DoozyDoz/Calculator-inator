package com.inator.calculator.conversion.domain.usecases

import com.inator.calculator.conversion.domain.model.ConversionUnits
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


class GetUnits @Inject constructor(
    private val animalRepository: AnimalRepository,
    private val dispatchersProvider: DispatchersProvider
) {

    suspend operator fun invoke(): ConversionUnits {
        return withContext(dispatchersProvider.io()) {
            val types = listOf(NO_FILTER_SELECTED) + animalRepository.getAnimalTypes()

            val ages = animalRepository.getAnimalAges()
                .map { age ->
                    if (age.name == unknown) {
                        NO_FILTER_SELECTED
                    } else {
                        age.name
                            .uppercase()
                            .replaceFirstChar { firstChar ->
                                if (firstChar.isLowerCase()) {
                                    firstChar.titlecase(Locale.ROOT)
                                } else {
                                    firstChar.toString()
                                }
                            }
                    }
                }

            return@withContext SearchFilters(ages, types)
        }
    }
}