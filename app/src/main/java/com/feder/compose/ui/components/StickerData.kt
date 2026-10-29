package com.feder.compose.ui.components

/**
 * Модель стикера.
 * @param id уникальный ID
 * @param drawType тип рисунка (0..N) — определяет, что рисовать в Canvas
 * @param text текст, который наклеивается на матрёшку (может быть пустым)
 */
data class StickerItem(
    val id: String,
    val drawType: Int,
    val text: String = ""
)

/**
 * Пак стикеров "Матрёшка" — 18 штук.
 * Рисуются в Compose Canvas (без XML-файлов).
 */
val DOG_PACK = listOf(
    StickerItem("dog1.png", 1, ""),
    StickerItem("dog2.png", 1, ""),
    StickerItem("dog3.png", 1, ""),
    StickerItem("dog4.png", 1, ""),
    StickerItem("dog5.png", 1, ""),
    StickerItem("dog6.png", 1, ""),
    StickerItem("dog7.png", 1, ""),
    StickerItem("dog8.png", 1, ""),
    StickerItem("dog9.png", 1, ""),
    StickerItem("dog10.png", 1, ""),
    StickerItem("dog11.png", 1, "")
)

/**
 * Категории (табы) — внизу панели.
 */
enum class StickerTab(val label: String) {
    STICKERS("Стикеры"),
    EMOJI("Эмодзи")
}


