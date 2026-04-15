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
import com.ricdev.mahjongscorecounter.model.MahjongVariant
import com.ricdev.mahjongscorecounter.model.RoundInput
import com.ricdev.mahjongscorecounter.model.ScoreRules
import com.ricdev.mahjongscorecounter.model.Seat
import com.ricdev.mahjongscorecounter.model.ThemeMode
import com.ricdev.mahjongscorecounter.model.WinType
import com.ricdev.mahjongscorecounter.model.variant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val gameState: StateFlow<GameState> = combine(
        repository.rulesFlow,
        repository.historyFlow,
        repository.dealerFlow,
        repository.honbaFlow,
        repository.riichiSticksFlow,
    ) { rules, history, dealer, honba, sticks ->
        GameState(rules, history, dealer, honba, sticks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameState())

    private val _formState = MutableStateFlow<FormState>(FormState.Fan())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.rulesFlow.map { it.variant() }.distinctUntilChanged().collect { variant ->
                _formState.value = FormState.defaultFor(variant)
            }
        }
    }

    val preview: StateFlow<PreviewState> = combine(
        _formState,
        repository.rulesFlow,
        repository.dealerFlow,
        repository.honbaFlow,
        repository.riichiSticksFlow,
    ) { form, rules, dealer, honba, sticks ->
        computePreview(form, rules, dealer, honba, sticks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreviewState.Empty)

    fun selectWinner(seat: Seat) {
        _formState.update { it.withWinner(seat) }
    }

    fun selectWinType(winType: WinType) {
        _formState.update { it.withWinType(winType) }
    }

    fun selectDiscarder(seat: Seat?) {
        _formState.update { it.withDiscarder(seat) }
    }

    fun setFan(value: Int) {
        _formState.update { current ->
            if (current !is FormState.Fan) current
            else current.copy(fanCount = value.coerceIn(MIN_FAN, MAX_FAN))
        }
    }

    fun setFlowerCount(value: Int) {
        _formState.update { current ->
            if (current !is FormState.Fan) current
            else current.copy(flowerCount = value.coerceAtLeast(0))
        }
    }

    fun setAnimalCount(value: Int) {
        _formState.update { current ->
            if (current !is FormState.Fan) current
            else current.copy(animalCount = value.coerceAtLeast(0))
        }
    }

    fun setTai(value: Int) {
        _formState.update { current ->
            if (current !is FormState.Tai) current
            else current.copy(taiCount = value.coerceIn(MIN_TAI, MAX_TAI))
        }
    }

    fun setHan(value: Int) {
        _formState.update { current ->
            if (current !is FormState.Riichi) current
            else current.copy(han = value.coerceIn(MIN_HAN, MAX_HAN))
        }
    }

    fun setFu(value: Int) {
        _formState.update { current ->
            if (current !is FormState.Riichi) current
            else current.copy(fu = value)
        }
    }

    fun commitRound() {
        val currentPreview = preview.value
        if (currentPreview !is PreviewState.Valid) return
        val input = currentPreview.input
        val result = currentPreview.result
        val committed = CommittedRound(
            input = input,
            result = result,
            timestampMillis = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            repository.appendRound(committed)
            applyPostCommitState(input)
        }
        resetFormAfterCommit()
    }

    private suspend fun applyPostCommitState(input: RoundInput) {
        val dealer = repository.dealerFlow.first()
        val honba = repository.honbaFlow.first()
        val winnerIsDealer = input.winner == dealer

        when (input) {
            is RoundInput.Riichi -> {
                repository.updateRiichiSticks(0)
                if (winnerIsDealer) {
                    repository.updateHonba(honba + 1)
                } else {
                    repository.updateHonba(0)
                    repository.updateDealer(dealer.next())
                }
            }
            is RoundInput.Tai -> {
                if (!winnerIsDealer) repository.updateDealer(dealer.next())
            }
            is RoundInput.Fan -> Unit
        }
    }

    private fun resetFormAfterCommit() {
        _formState.update { current ->
            when (current) {
                is FormState.Fan -> current.copy(
                    fanCount = 1,
                    flowerCount = 0,
                    animalCount = 0,
                    discarder = null,
                )
                is FormState.Tai -> current.copy(discarder = null)
                is FormState.Riichi -> current.copy(han = 1, fu = 30, discarder = null)
            }
        }
    }

    fun undoLast() {
        viewModelScope.launch { repository.dropLastRound() }
    }

    fun resetGame() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.updateDealer(Seat.EAST)
        }
        viewModelScope.launch {
            val variant = repository.rulesFlow.first().variant()
            _formState.value = FormState.defaultFor(variant)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.updateThemeMode(mode) }
    }

    fun updateRules(rules: ScoreRules) {
        viewModelScope.launch { repository.updateRules(rules) }
    }

    fun changeVariant(variant: MahjongVariant) {
        viewModelScope.launch {
            val current = repository.rulesFlow.first()
            if (current.variant() == variant) return@launch
            repository.updateRules(ScoreRules.defaultFor(variant))
            repository.clearHistory()
            repository.updateDealer(Seat.EAST)
        }
    }

    fun setDealer(seat: Seat) {
        viewModelScope.launch { repository.updateDealer(seat) }
    }

    fun setHonba(count: Int) {
        viewModelScope.launch { repository.updateHonba(count) }
    }

    fun setRiichiSticks(count: Int) {
        viewModelScope.launch { repository.updateRiichiSticks(count) }
    }

    fun declareRiichi(seat: Seat) {
        viewModelScope.launch {
            val rules = repository.rulesFlow.first()
            if (rules !is ScoreRules.JapaneseRiichi) return@launch
            val current = repository.riichiSticksFlow.first()
            repository.updateRiichiSticks(current + 1)
        }
    }

    suspend fun currentRules(): ScoreRules = repository.rulesFlow.first()

    private fun computePreview(
        form: FormState,
        rules: ScoreRules,
        dealer: Seat,
        honba: Int,
        sticks: Int,
    ): PreviewState {
        val winner = form.winner ?: return PreviewState.Empty
        val input = buildInput(form, winner, dealer, honba, sticks) ?: return PreviewState.Empty
        val error = ScoreEngine.validate(input, rules)
        if (error != null) return PreviewState.Invalid(error)
        return PreviewState.Valid(input, ScoreEngine.calculate(input, rules))
    }

    private fun buildInput(
        form: FormState,
        winner: Seat,
        dealer: Seat,
        honba: Int,
        sticks: Int,
    ): RoundInput? = when (form) {
        is FormState.Fan -> RoundInput.Fan(
            winner = winner,
            winType = form.winType,
            discarder = form.discarder,
            fanCount = form.fanCount,
            flowerCount = form.flowerCount,
            animalCount = form.animalCount,
        )
        is FormState.Tai -> RoundInput.Tai(
            winner = winner,
            winType = form.winType,
            discarder = form.discarder,
            taiCount = form.taiCount,
            dealer = dealer,
        )
        is FormState.Riichi -> RoundInput.Riichi(
            winner = winner,
            winType = form.winType,
            discarder = form.discarder,
            han = form.han,
            fu = form.fu,
            dealer = dealer,
            honbaCount = honba,
            riichiSticks = sticks,
        )
    }

    companion object {
        const val MIN_FAN = 1
        const val MAX_FAN = 20
        const val MIN_TAI = 1
        const val MAX_TAI = 30
        const val MIN_HAN = 1
        const val MAX_HAN = 13
        val FU_OPTIONS = listOf(20, 25, 30, 40, 50, 60, 70, 80, 90, 100, 110)

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

private fun Seat.next(): Seat = when (this) {
    Seat.EAST -> Seat.SOUTH
    Seat.SOUTH -> Seat.WEST
    Seat.WEST -> Seat.NORTH
    Seat.NORTH -> Seat.EAST
}
