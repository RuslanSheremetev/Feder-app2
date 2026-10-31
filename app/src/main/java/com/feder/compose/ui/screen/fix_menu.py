import re, time, shutil, sys

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# 1. Очищаем onPositioned (он даёт кривой X=0 для правых)
code = re.sub(r'onPositioned\s*=\s*\{\s*pos\s*->\s*msg\.posX\s*=\s*pos\.x;\s*msg\.posY\s*=\s*pos\.y;\s*selectedMessageOffset\s*=\s*pos\s*\}', 
              'onPositioned = { }', code)

# 2. Переписываем onTapWithCoords. 
# Вместо того чтобы полагаться на координаты внутри пузыря, мы берем ширину экрана для правых.
pattern_tap = r'onTapWithCoords\s*=\s*\{\s*pos\s*->\s*[^\}]*?(?=\n\s*onClick|\n\s*onLongClick|\n\s*\})'
replacement_tap = '''onTapWithCoords = { pos ->
                                        val isMineMsg = msg.from == "demo" // замени "demo" на свою переменную пользователя, если нужно
                                        val density = androidx.compose.ui.platform.LocalDensity.current
                                        val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                                        val xPos = if (isMineMsg) with(density) { screenWidth.toPx() - 180.dp.toPx() } else with(density) { 16.dp.toPx() }
                                        val yPos = pos.y + with(density) { 100.dp.toPx() }
                                        selectedMessageOffset = androidx.compose.ui.geometry.Offset(xPos, yPos)
                                        menuAbsOffset = selectedMessageOffset'''
code = re.sub(pattern_tap, replacement_tap, code, count=1)

# 3. Исправляем PopupPositionProvider (строка ~2100), чтобы он использовал selectedMessageOffset напрямую
# Находим блок Popup и заменяем координаты
old_popup = r'object\s*:\s*androidx\.compose\.ui\.window\.PopupPositionProvider\s*\{[\s\S]*?x\s*=\s*with\(.*?\)\s*\{\s*\(selectedMessageOffset\.x\)\.toDp\(\)\s*\},[\s\S]*?y\s*=\s*with\(.*?\)\s*\{\s*\(selectedMessageOffset\.y\s*\)\.toDp\(\)\s*\}'
new_popup = '''object : androidx.compose.ui.window.PopupPositionProvider {
                    override fun calculatePosition(anchorBounds: androidx.compose.ui.unit.IntRect, windowSize: androidx.compose.ui.unit.IntSize, layoutDirection: androidx.compose.ui.unit.LayoutDirection, popupContentSize: androidx.compose.ui.unit.IntSize): androidx.compose.ui.unit.IntOffset {
                        return androidx.compose.ui.unit.IntOffset(
                            x = selectedMessageOffset.x.toInt(),
                            y = selectedMessageOffset.y.toInt()
                        )
                    }
                }'''

# Если регулярка не сработала, пробуем заменить просто вызов Popup
if code == open(file_path, 'r', encoding='utf-8').read():
    # Запасной вариант: заменяем offset в Popup
    code = re.sub(r'offset\s*=\s*androidx\.compose\.ui\.unit\.IntOffset\([^\)]+\)', 
                  'offset = androidx.compose.ui.unit.IntOffset(x = selectedMessageOffset.x.toInt(), y = selectedMessageOffset.y.toInt())', code)
else:
    code = re.sub(old_popup, new_popup, code)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(code)

print(f"✅ Готово. Бэкап: {backup_path}")
