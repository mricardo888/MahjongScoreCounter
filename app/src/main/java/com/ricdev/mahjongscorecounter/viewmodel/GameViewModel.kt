package com.ricdev.mahjongscorecounter.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ricdev.mahjongscorecounter.data.GameRepository
import com.ricdev.mahjongscorecounter.data.gameDataStore
import com.ricdev.mahjongscorecounter.logic.ScoreEngine
import com.ricdev.mahjongscorecounter.model.CommittedRound
import com.ricdev.mahjongscorecounter.model.GameState
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ThemeMode
import com.ricdev.mahjongscorecounter.model.WinType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val gameState: StateFlow<GameState> = repository.historyFlow
        .map { history -> GameState(history = history) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameState())

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    val preview: StateFlow<PreviewState> = _formState
        .map { form -> computePreview(form) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreviewState.Empty)

    fun selectWinner(seat: Seat) {
        _formState.update { it.withWinner(seat) }
    }

    fun selectWinType(winType: WinType) {
        _formState.update { it.withWinType(winType) }
    }

    fun selectPayer(seat: Seat?) {
        _formState.update { it.withPayer(seat) }
    }

    fun setAmount(value: Int) {
        _formState.update { it.copy(amount = value) }
    }

    fun commitRound() {
        val currentPreview = preview.value
        if (currentPreview !is PreviewState.Valid) return
        val committed = CommittedRound(
            input = currentPreview.input,
            result = currentPreview.result,
            timestampMillis = System.currentTimeMillis(),
        )
        viewModelScope.launch { repository.appendRound(committed) }
        resetFormAfterCommit()
    }

    private fun resetFormAfterCommit() {
        _formState.update { current ->
            current.copy(
                amount = 0,
                payer = null,
            )
        }
    }

    fun undoLast() {
        viewModelScope.launch { repository.dropLastRound() }
    }

    fun resetGame() {
        viewModelScope.launch { repository.clearHistory() }
        _formState.value = FormState()
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.updateThemeMode(mode) }
    }

    private fun computePreview(form: FormState): PreviewState {
        val winner = form.winner ?: return PreviewState.Empty
        val input = RoundInput(
            winner = winner,
            winType = form.winType,
            payer = form.payer,
            amount = form.amount,
        )
        val error = ScoreEngine.validate(input)
        if (error != null) return PreviewState.Invalid(error)
        return PreviewState.Valid(input, ScoreEngine.calculate(input))
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = GameRepository(context.applicationContext.gameDataStore)
                    return GameViewModel(repository) as T
                }
            }
    }
}
