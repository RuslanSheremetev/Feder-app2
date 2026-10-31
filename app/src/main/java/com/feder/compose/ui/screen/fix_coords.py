import re, time, shutil

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# Ищем блок внутри onTapWithCoords, где происходит старое присвоение
# (строки 1848 и 1856: selectedMessageOffset = pos / realX, realY)
# Мы заменим весь блок между "onTapWithCoords = { pos ->" и "onClick?.invoke()" на правильный расчет
pattern = r'(onTapWithCoords\s*=\s*\{\s*pos\s*->\s*)([\s\S]*?)(\n\s*onClick\?\.invoke\(\))'

def repl(m):
    prefix = m.group(1)
    suffix = m.group(3)
    return prefix + """
                                        val isMineMsg = msg.from == "demo"
                                        val density = androidx.compose.ui.platform.LocalDensity.current
                                        val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp
                                        val xPos = if (isMineMsg) with(density) { screenWidth.toPx() - 180.dp.toPx() } else with(density) { 16.dp.toPx() }
                                        val yPos = pos.y + with(density) { 100.dp.toPx() }
                                        selectedMessageOffset = androidx.compose.ui.geometry.Offset(xPos, yPos)
                                        menuAbsOffset = selectedMessageOffset""" + suffix

code_new = re.sub(pattern, repl, code, count=1)

if code_new == code:
    print("❌ Паттерн не найден. Проверь строки 1840-1860 вручную.")
else:
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(code_new)
    print(f"✅ Готово. Бэкап: {backup_path}")
