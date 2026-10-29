package com.feder.compose.ui.components

data class StickerItem(
    val id: String,
    val drawType: Int = 0,
    val text: String = ""
)

data class StickerPack(
    val name: String,
    val stickers: List<StickerItem>
)

// ─── Паки ───
val DOG_PACK = StickerPack(
    name = "Pets",
    stickers = (1..11).map { StickerItem("dog$it.png") }
)

val CAT_PACK = StickerPack(
    name = "Cats",
    stickers = (1..11).map { StickerItem("cat$it.png") }
)

val ANIMALS_PACK = StickerPack(
    name = "Animals",
    stickers = (1..12).map { StickerItem("animal$it.png") }
)

val ALL_PACKS = listOf(DOG_PACK, CAT_PACK, ANIMALS_PACK)

enum class StickerTab(val label: String) {
    STICKERS("Стикеры"),
    EMOJI("Эмодзи")
}
