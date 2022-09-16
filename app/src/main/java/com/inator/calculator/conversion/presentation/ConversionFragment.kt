package com.inator.calculator.conversion.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.inator.calculator.R
import com.inator.calculator.databinding.FragmentConverterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConversionFragment : Fragment(R.layout.fragment_converter) {
    private lateinit var textWatcher1: TextWatcher
    private lateinit var textWatcher2: TextWatcher
    private val converterInputViewModel: ConversionFragmentViewModel by viewModels()
    private var hasSetSavedSpinners = false

    private var _binding: FragmentConverterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConverterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpViews()
    }

    private fun setUpViews() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds[0]) {
                R.id.length -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Length"))
                }
                R.id.mass -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Mass"))
                }
                R.id.area -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Area"))
                }
                R.id.speed -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Speed"))
                }
                R.id.angle -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Angle"))
                }
                R.id.data -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Data"))
                }
                R.id.time -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Time"))
                }
                R.id.volume -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Volume"))
                }
                R.id.temperature -> {
                    converterInputViewModel.onEvent(ConversionEvent.SetMeasure("Temperature"))
                }
            }
            converterInputViewModel.clearSavedSpinners()
        }
        textWatcher1 = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                converterInputViewModel.setInput1(s.toString())
            }
        }
        textWatcher2 = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                converterInputViewModel.setInput2(s.toString())
            }
        }
        binding.editText1.addTextChangedListener(textWatcher1)
        binding.editText2.addTextChangedListener(textWatcher2)

        converterInputViewModel.getOutputDirect().observe(viewLifecycleOwner) {
            binding.editText2.apply {
                removeTextChangedListener(textWatcher2)
                setText(it)
                addTextChangedListener(textWatcher2)
            }
        }
        converterInputViewModel.getOutputReverse().observe(viewLifecycleOwner) {
            binding.editText1.apply {
                removeTextChangedListener(textWatcher1)
                setText(it)
                addTextChangedListener(textWatcher1)
            }
        }
        converterInputViewModel.getMeasure().observe(
            viewLifecycleOwner
        ) {
            setUpSpinnerAdapter(it)
        }
        setUnitListeners()
        subscribeToViewStateUpdates()
    }

    private fun subscribeToViewStateUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                converterInputViewModel.state.collect {
                    updateScreenState(it)
                }
            }
        }
    }

    private fun updateScreenState(newState: ConversionViewState) {
        binding.chipGroup.check(newState.currentMeasure)
        with(newState){
            setupUnitValues(binding.unit1, unitValues.getContentIfNotHandled())
            setupUnitValues(binding.unit2, unitValues.getContentIfNotHandled())
        }


        updateInitialStateViews(inInitialState)
        searchAdapter.submitList(searchResults)


        updateRemoteSearchViews(searchingRemotely)
        updateNoResultsViews(noResultsState)
        handleFailures(failure)
    }

    private fun setupUnitValues(spinner: Spinner, unitValues: List<String>?) {
        if (unitValues == null || unitValues.isEmpty()) return

        spinner.adapter = createUnitAdapter(filterValues)
        filter.setText(GetSearchFilters.NO_FILTER_SELECTED, false)
    }

    private fun createUnitAdapter(adapterValues: List<String>): ArrayAdapter<String> {
            return ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, adapterValues)
    }

    private fun setUnitListeners() {
        with(binding) {
            setupUnitListenerFor(unit1) { item ->
                converterInputViewModel.onEvent(ConversionEvent.UnitOneSelected(item))
            }

            setupUnitListenerFor(unit2) { item ->
                converterInputViewModel.onEvent(ConversionEvent.UnitTwoSelected(item))
            }
        }
    }

    private fun setupUnitListenerFor(unit: Spinner, block: (item: String) -> Unit) {
        unit.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.let {
                    block(it.adapter.getItem(position) as String)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setUpSpinnerAdapter(string: String) {

        val unitArray = when (string) {
            "Length" -> R.array.length_units
            "Area" -> R.array.area_units
            "Mass" -> R.array.mass_units
            "Speed" -> R.array.speed_units
            "Data" -> R.array.storage_units
            "Volume" -> R.array.volume_units
            "Time" -> R.array.time_units
            "Temperature" -> R.array.temperature_units
            "Angle" -> R.array.angle_units
            else -> R.array.length_units
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            unitArray,
            android.R.layout.simple_spinner_item
        ).also { unitAdapter ->
            unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.unit1.adapter = unitAdapter
            binding.unit2.adapter = unitAdapter
            setSavedSpinners()
        }
    }

    private fun setSavedSpinners() {
        if (!hasSetSavedSpinners) {
            binding.unit1.setSelection(converterInputViewModel.getSavedSpinner1())
            binding.unit2.setSelection(converterInputViewModel.getSavedSpinner2())
            hasSetSavedSpinners = true
        } else {
            binding.unit1.setSelection(0)
            binding.unit2.setSelection(1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}