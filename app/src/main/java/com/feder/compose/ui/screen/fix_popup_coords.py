import time, shutil

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# Маркер: начало PopupPositionProvider (строка ~2103)
start_marker = "object : androidx.compose.ui.window.PopupPositionProvider {"
# Маркер: закрывающая скобка после y = ... .toDp()
# Ищем следующий блок ")" после ".toDp()" в этой секции
start_idx = code.find(start_marker)

if start_idx == -1:
    print("❌ PopupPositionProvider не найден.")
else:
    # Ищем конец блока (закрывающая скобка после ".toDp() }")
    # Ищем ".toDp() }" после start_idx
    end_marker_idx = code.find(".toDp() }", start_idx)
    if end_marker_idx == -1:
        print("❌ Конец блока не найден.")
    else:
        # Находим закрывающую "}" после ".toDp() }"
        end_idx = code.find("}", end_marker_idx)
        if end_idx == -1:
            print("❌ Закрывающая скобка не найдена.")
        else:
            end_idx += 1  # включаем "}"

            # Заменяем только блок PopupPositionProvider на новый с IntOffset
            new_provider = """object : androidx.compose.ui.window.PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: androidx.compose.ui.unit.IntRect,
                        windowSize: androidx.compose.ui.unit.IntSize,
                        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                        popupContentSize: androidx.compose.ui.unit.IntSize
                    ): androidx.compose.ui.unit.IntOffset {
                        return androidx.compose.ui.unit.IntOffset(
                            x = selectedMessageOffset.x.toInt(),
                            y = selectedMessageOffset.y.toInt()
                        )
                    }
                }"""

            new_code = code[:start_idx] + new_provider + code[end_idx:]

            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_code)

            print(f"✅ Готово. Бэкап: {backup_path}")
            print("Проверь: sed -n '2095,2135p' ChatScreen.kt")
