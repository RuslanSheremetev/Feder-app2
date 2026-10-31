import time, shutil, re

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)
print(f"📦 Бэкап: {backup_path}")

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# 1. Добавляем сохранение реальной позиции в MessageBubble
# Ищем начало Box внутри MessageBubble
marker = "Box(Modifier.onGloballyPositioned { coords -> onPositioned?.invoke(coords.positionInWindow()) }.fillMaxWidth()"
if marker in code:
    new_marker = "Box(Modifier.onGloballyPositioned { coords -> onPositioned?.invoke(coords.positionInWindow()); msg.posY = coords.positionInWindow().y; msg.posX = coords.positionInWindow().x }.fillMaxWidth()"
    code = code.replace(marker, new_marker, 1)
    print("✅ Добавлено сохранение реальных координат в msg.posX/msg.posY")
else:
    print("⚠️ Маркер Box не найден")

# 2. Переписываем onClick, чтобы использовать msg.posX/msg.posY напрямую
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
                                        // Используем координаты, сохраненные из onGloballyPositioned выше
                                        selectedMessageOffset = androidx.compose.ui.geometry.Offset(msg.posX, msg.posY)
                                        selectedMessage = msg
                                    }
                                },
                            """
    code = code[:start_idx] + new_onclick + code[end_idx:]
    print("✅ onClick теперь использует абсолютные координаты")
else:
    print("⚠️ onClick маркеры не найдены")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(code)
print(f"💾 Сохранено: {file_path}")
