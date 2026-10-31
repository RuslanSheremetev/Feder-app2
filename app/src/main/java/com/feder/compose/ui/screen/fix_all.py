import time, shutil

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)
print(f"📦 Бэкап: {backup_path}")

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# ═══════════════════════════════════════════════════════════════
# ПРАВКА 1: onClick в MessageBubble — правильный X
# ═══════════════════════════════════════════════════════════════
start_marker = "onClick = {\n                                    if (selectionMode) {"
end_marker = "onLongClick = {"

start_idx = code.find(start_marker)
end_idx = code.find(end_marker, start_idx) if start_idx != -1 else -1

if start_idx != -1 and end_idx != -1:
    new_onclick = """onClick = {
                                    if (selectionMode) {
                                        if (selectedMessages.contains(msg.id.toString())) {
                                            selectedMessages = selectedMessages - msg.id.toString()
                                        } else {
                                            selectedMessages = selectedMessages + msg.id.toString()
                                        }
                                    } else {
                                        // Вычисляем координаты для меню
                                        val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.toFloat()
                                        val realX = if (isMine) (screenWidthDp - 200f) else 16f
                                        val visibleItem = listState.layoutInfo.visibleItemsInfo.find { it.key == msg.id }
                                        val listOffset = visibleItem?.offset?.toFloat() ?: msg.posY
                                        selectedMessageOffset = androidx.compose.ui.geometry.Offset(realX, listOffset + 100f)
                                        selectedMessage = msg
                                    }
                                },
                            """
    code = code[:start_idx] + new_onclick + code[end_idx:]
    print("✅ ПРАВКА 1: onClick заменен (X = screenWidth-200 для правых)")
else:
    print("⚠️ ПРАВКА 1: маркеры onClick не найдены")

# ═══════════════════════════════════════════════════════════════
# ПРАВКА 2: убираем offset у Column внутри Popup (двойное смещение)
# ═══════════════════════════════════════════════════════════════
# Ищем Column с offset и заменяем на Column без offset
offset_block = """.offset(
                            x = with(androidx.compose.ui.platform.LocalDensity.current) { (selectedMessageOffset.x).toDp() },
                            y = with(androidx.compose.ui.platform.LocalDensity.current) { (selectedMessageOffset.y - 450f).toDp() }
                        )
                        .padding(end = 16.dp),"""
if offset_block in code:
    code = code.replace(offset_block, ".padding(end = 16.dp),", 1)
    print("✅ ПРАВКА 2: убрано двойное смещение в Column внутри Popup")
else:
    print("⚠️ ПРАВКА 2: offset-блок не найден (возможно, уже убран)")

# ═══════════════════════════════════════════════════════════════
# ПРАВКА 3: убираем повторную установку selectedMessageOffset в onTapWithCoords
# ═══════════════════════════════════════════════════════════════
# Ищем onTapWithCoords = { localOffset -> ... } и оставляем пустым
import re
pattern = r'(onTapWithCoords\s*=\s*\{\s*localOffset\s*->\s*)([\s\S]*?)(\n\s*\},)'
def repl(m):
    return m.group(1) + "\n                                // onClick уже вычислил координаты\n                            " + m.group(3)
code_new = re.sub(pattern, repl, code, count=1)
if code_new != code:
    code = code_new
    print("✅ ПРАВКА 3: onTapWithCoords очищен")
else:
    print("⚠️ ПРАВКА 3: onTapWithCoords не найден или уже пуст")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(code)

print(f"\n💾 Сохранено: {file_path}")
