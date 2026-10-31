import time, shutil

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# Ищем блок Popup по маркеру "Message action menu"
start_marker = "// Message action menu - Popup near message"
start_idx = code.find(start_marker)

if start_idx == -1:
    print("❌ Маркер Popup не найден. Проверь вручную.")
else:
    # Ищем закрывающую скобку Popup (по отступу)
    # Вставляем новый, правильный Popup с IntOffset
    new_popup = """// Message action menu - Popup near message
        if (selectedMessage != null) {
            Popup(
                offset = androidx.compose.ui.unit.IntOffset(
                    x = selectedMessageOffset.x.toInt(),
                    y = selectedMessageOffset.y.toInt()
                ),
                properties = PopupProperties(focusable = true),
                onDismissRequest = { selectedMessage = null }
            ) {
                // Твоё содержимое меню (Reply, Copy, Edit, Forward, Delete)
                // Оно уже должно быть внутри — не трогаем его.
            }
        }"""
    
    # Находим конец текущего Popup блока (следующие 200 строк до следующего // или пустой строки)
    # Вместо сложной навигации — просто ищем закрывающую скобку "}" после "onDismissRequest"
    end_marker = "onDismissRequest"
    end_idx = code.find(end_marker, start_idx)
    
    if end_idx == -1:
        print("❌ Не найден onDismissRequest. Проверь вручную.")
    else:
        # Находим закрывающую "}" после onDismissRequest
        # Просто ищем следующую пустую строку с отступом
        next_block_start = code.find("\n\n", end_idx)
        if next_block_start == -1:
            next_block_start = end_idx + 500
        
        print(f"✅ Найден Popup на позиции {start_idx}, конец на {next_block_start}")
        print("⚠️ ВНИМАНИЕ: Автоматическая замена Popup может быть опасной.")
        print("Проверь вручную и убедись, что используется IntOffset, а не .toDp()")
        print(f"Бэкап: {backup_path}")

