import time, shutil

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)
print(f"📦 Бэкап: {backup_path}")

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# 1. Добавляем val screenWidthDp в начало MessageBubble (после haptic = ...)
marker = "val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current"
insert_after = "val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current\n        val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.toFloat()"

if "val screenWidthDp" not in code:
    if marker in code:
        code = code.replace(marker, insert_after, 1)
        print("✅ Добавлена val screenWidthDp в MessageBubble")
    else:
        print("⚠️ Маркер haptic не найден")
else:
    print("ℹ️ val screenWidthDp уже есть — пропускаем")

# 2. Убираем LocalConfiguration.current из onClick
old_line = "val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.toFloat()"
# Заменяем на комментарий/пустую строку ТОЛЬКО если это внутри onClick
# Ищем контекст: onClick = { ... val screenWidthDp = androidx...
# Проще: заменяем все вхождения, кроме первого добавленного
parts = code.split(old_line)
if len(parts) > 1:
    # Склеиваем обратно, но во втором и последующих вхождениях убираем строку
    new_code = parts[0] + old_line  # первое оставляем
    for p in parts[1:]:
        new_code += "\n                                        " + p.lstrip()
    code = new_code
    print(f"✅ Убраны повторные вызовы LocalConfiguration ({len(parts)-1} шт.)")
else:
    print("ℹ️ Вторых вхождений нет")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(code)

print(f"💾 Сохранено: {file_path}")
