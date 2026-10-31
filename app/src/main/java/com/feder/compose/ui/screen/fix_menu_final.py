import time, shutil

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# Маркер 1: Начало старого блока onClick
start_marker = "onClick = {\n                                    if (selectionMode) {"
# Маркер 2: Конец старого блока onTapWithCoords (перед onPhotoRectReady)
end_marker = "onPhotoRectReady = { rect ->"

start_idx = code.find(start_marker)
end_idx = code.find(end_marker)

if start_idx == -1 or end_idx == -1:
    print("❌ ОШИБКА: Не найдены маркеры. Откат не требуется, файл не изменен.")
else:
    # Формируем новый блок. Мы объединяем onClick и onTapWithCoords,
    # чтобы координаты считались ОДИН раз и правильно.
    new_block = """onClick = {
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
                                        
                                        // Y берем из позиции элемента в списке, если есть
                                        val visibleItem = listState.layoutInfo.visibleItemsInfo.find { it.key == msg.id }
                                        val listOffset = visibleItem?.offset?.toFloat() ?: msg.posY
                                        
                                        selectedMessageOffset = androidx.compose.ui.geometry.Offset(realX, listOffset + 50f)
                                        menuAbsOffset = selectedMessageOffset
                                        selectedMessage = msg
                                    }
                                },
                            onLongClick = { 
                                selectionMode = true
                                selectedMessages = selectedMessages + msg.id.toString() 
                            },
                            onTapWithCoords = { localOffset ->
                                // Так как onClick уже считает координаты, здесь просто передаем их
                                // (оставляем пустым, чтобы не перезаписать)
                            },
                            """

    # Выполняем замену
    new_code = code[:start_idx] + new_block + code[end_idx:]
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_code)
    
    print(f"✅ Готово. Бэкап: {backup_path}")
    print("Проверь баланс скобок: grep -o '{' ChatScreen.kt | wc -l")
