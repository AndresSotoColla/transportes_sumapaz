package com.example.transportes_sumapaz.ui.viajes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transportes_sumapaz.data.remote.model.ViajeConPasajeros
import com.example.transportes_sumapaz.data.repository.TransporteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ViajesUiState {
    object Loading : ViajesUiState()
    data class Success(val viajes: List<ViajeConPasajeros>) : ViajesUiState()
    data class Error(val message: String) : ViajesUiState()
}

@HiltViewModel
class ViajesViewModel @Inject constructor(
    private val repository: TransporteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ViajesUiState>(ViajesUiState.Loading)
    val uiState: StateFlow<ViajesUiState> = _uiState.asStateFlow()

    init {
        cargarViajes()
    }

    fun cargarViajes() {
        viewModelScope.launch {
            _uiState.value = ViajesUiState.Loading
            val result = repository.obtenerViajesConPasajeros()
            if (result.isSuccess) {
                _uiState.value = ViajesUiState.Success(result.getOrThrow())
            } else {
                _uiState.value = ViajesUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }
}
