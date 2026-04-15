package com.ricdev.mahjongscorecounter.model

enum class MahjongVariant {
    HONG_KONG_NEW,
    TAIWANESE,
    JAPANESE_RIICHI,
    HOKKIEN,
    SHANGHAI,
    SICHUAN,
    SINGAPOREAN;

    companion object {
        val DEFAULT: MahjongVariant = HONG_KONG_NEW
    }
}
