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
val MATRYOSHKA_PACK = listOf(
    StickerItem("m01", 1, "ЗДРАВ БУЧАЙ"),
    StickerItem("m02", 2, "ДОБРОГО ПУТИ"),
    StickerItem("m03", 3, "ЧЁВО ДАВНОЕ"),
    StickerItem("m04", 4, "НИЗКИЙ ПОКЛОН"),
    StickerItem("m05", 5, "ДОБРОЙ НОЧИ"),
    StickerItem("m06", 6, "ДА/НЕТ"),
    StickerItem("m07", 7, "ТРУНЬ"),
    StickerItem("m08", 8, "ПРИГОРЮНИЛАСЬ"),
    StickerItem("m09", 9, ""),
    StickerItem("m10", 10, "УЛЫБАЙСЯ"),
    StickerItem("m11", 11, "ОТДЫХАЙ"),
    StickerItem("m12", 12, "СПАСИБО"),
    StickerItem("m13", 13, "ПРИВЕТ"),
    StickerItem("m14", 14, "УРА"),
    StickerItem("m15", 15, "ОГОНЬ"),
    StickerItem("m16", 16, "ЗВЕЗДА"),
    StickerItem("m17", 17, "СЕРДЦЕ"),
    StickerItem("m18", 18, "ПОЦЕЛУЙ")
)

/**
 * Категории (табы) — внизу панели.
 */
enum class StickerTab(val label: String) {
    STICKERS("Стикеры"),
    EMOJI("Эмодзи")
}
