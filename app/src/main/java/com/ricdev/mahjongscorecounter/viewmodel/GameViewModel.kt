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
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.WinType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
) : ViewModel() {

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    val gameState: StateFlow<GameState> = combine(
        repository.rulesFlow,
        repository.historyFlow,
    ) { rules, history ->
        GameState(rules = rules, history = history)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GameState(),
    )

    val preview: StateFlow<PreviewState> = combine(
        _formState,
        repository.rulesFlow,
    ) { form, rules ->
        computePreview(form, rules)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PreviewState.Empty,
    )

    fun selectWinner(seat: Seat) {
        _formState.update { current ->
            val discarder = if (current.discarder == seat) null else current.discarder
            current.copy(winner = seat, discarder = discarder)
        }
    }

    fun selectWinType(winType: WinType) {
        _formState.update { current ->
            val discarder = if (winType == WinType.SELF_DRAW) null else current.discarder
            current.copy(winType = winType, discarder = discarder)
        }
    }

    fun setFan(fan: Int) {
        val clamped = fan.coerceIn(MIN_FAN, MAX_FAN)
        _formState.update { it.copy(fanCount = clamped) }
    }

    fun selectDiscarder(seat: Seat?) {
        _formState.update { current ->
            if (seat != null && seat == current.winner) current
            else current.copy(discarder = seat)
        }
    }

    fun commitRound() {
        val currentPreview = preview.value
        if (currentPreview !is PreviewState.Valid) return
        val form = _formState.value
        val winner = form.winner ?: return
        val input = RoundInput(
            winner = winner,
            winType = form.winType,
            fanCount = form.fanCount,
            discarder = form.discarder,
        )
        val result = currentPreview.result
        val committed = CommittedRound(
            input = input,
            result = result,
            timestampMillis = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            repository.appendRound(committed)
        }
        // reset fan → 1, keep winner, clear discarder
        _formState.update {
            it.copy(fanCount = 1, discarder = null)
        }
    }

    fun undoLast() {
        viewModelScope.launch {
            repository.dropLastRound()
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            repository.clearHistory()
        }
        _formState.value = FormState()
    }

    fun updateRules(rules: ScoreRules) {
        if (rules.selfDrawBase < 0 || rules.discardWinBase < 0) return
        viewModelScope.launch {
            repository.updateRules(rules)
        }
    }

    /**
     * Reads the latest rules flow synchronously from the cached game state. Convenience for the
     * settings screen when pre-populating fields.
     */
    suspend fun currentRules(): ScoreRules = repository.rulesFlow.first()

    private fun computePreview(form: FormState, rules: ScoreRules): PreviewState {
        val winner = form.winner ?: return PreviewState.Empty
        val input = RoundInput(
            winner = winner,
            winType = form.winType,
            fanCount = form.fanCount,
            discarder = form.discarder,
        )
        val error = ScoreEngine.validate(input, rules)
        if (error != null) return PreviewState.Invalid(error)
        return PreviewState.Valid(ScoreEngine.calculate(input, rules))
    }

    companion object {
        const val MIN_FAN = 1
        const val MAX_FAN = 20

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
