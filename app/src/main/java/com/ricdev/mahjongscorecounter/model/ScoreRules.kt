package com.ricdev.mahjongscorecounter.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ScoreRules {

    @Serializable
    @SerialName("hk_new")
    data class HongKongNew(
        @SerialName("discard_base") val discardBase: Int = 2,
        @SerialName("self_draw_base") val selfDrawBase: Int = 1,
        @SerialName("max_fan") val maxFan: Int? = null,
    ) : ScoreRules

    @Serializable
    @SerialName("taiwanese")
    data class Taiwanese(
        @SerialName("base") val base: Int = 1,
        @SerialName("per_tai") val perTai: Int = 1,
        @SerialName("min_tai") val minTai: Int = 5,
        @SerialName("self_draw_bonus_tai") val selfDrawBonusTai: Int = 1,
        @SerialName("dealer_multiplier") val dealerMultiplier: Int = 2,
    ) : ScoreRules

    @Serializable
    @SerialName("riichi")
    data class JapaneseRiichi(
        @SerialName("kiriage_mangan") val kiriageMangan: Boolean = false,
        @SerialName("honba_value") val honbaValue: Int = 300,
        @SerialName("riichi_stick_value") val riichiStickValue: Int = 1000,
    ) : ScoreRules

    @Serializable
    @SerialName("hokkien")
    data class Hokkien(
        @SerialName("base") val base: Int = 1,
        @SerialName("per_unit") val perUnit: Int = 1,
        @SerialName("max_units") val maxUnits: Int? = 10,
        @SerialName("dealer_doubles") val dealerDoubles: Boolean = false,
    ) : ScoreRules

    @Serializable
    @SerialName("shanghai")
    data class Shanghai(
        @SerialName("base") val base: Int = 1,
        @SerialName("max_fan") val maxFan: Int = 13,
    ) : ScoreRules

    @Serializable
    @SerialName("sichuan")
    data class Sichuan(
        @SerialName("base") val base: Int = 1,
        @SerialName("max_fan") val maxFan: Int = 5,
    ) : ScoreRules

    @Serializable
    @SerialName("singaporean")
    data class Singaporean(
        @SerialName("discard_base") val discardBase: Int = 2,
        @SerialName("self_draw_base") val selfDrawBase: Int = 1,
        @SerialName("flower_bonus") val flowerBonus: Int = 1,
        @SerialName("animal_bonus") val animalBonus: Int = 2,
    ) : ScoreRules

    companion object {
        fun defaultFor(variant: MahjongVariant): ScoreRules = when (variant) {
            MahjongVariant.HONG_KONG_NEW -> HongKongNew()
            MahjongVariant.TAIWANESE -> Taiwanese()
            MahjongVariant.JAPANESE_RIICHI -> JapaneseRiichi()
            MahjongVariant.HOKKIEN -> Hokkien()
            MahjongVariant.SHANGHAI -> Shanghai()
            MahjongVariant.SICHUAN -> Sichuan()
            MahjongVariant.SINGAPOREAN -> Singaporean()
        }
    }
}

fun ScoreRules.variant(): MahjongVariant = when (this) {
    is ScoreRules.HongKongNew -> MahjongVariant.HONG_KONG_NEW
    is ScoreRules.Taiwanese -> MahjongVariant.TAIWANESE
    is ScoreRules.JapaneseRiichi -> MahjongVariant.JAPANESE_RIICHI
    is ScoreRules.Hokkien -> MahjongVariant.HOKKIEN
    is ScoreRules.Shanghai -> MahjongVariant.SHANGHAI
    is ScoreRules.Sichuan -> MahjongVariant.SICHUAN
    is ScoreRules.Singaporean -> MahjongVariant.SINGAPOREAN
}
