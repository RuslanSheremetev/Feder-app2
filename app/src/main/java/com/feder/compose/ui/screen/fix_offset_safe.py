import time, shutil, re

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)
print(f"📦 Бэкап: {backup_path}")

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# Находим начало блока
start_marker = "popupPositionProvider = remember {"
start_idx = code.find(start_marker)
if start_idx == -1:
    print("❌ Не найден popupPositionProvider")
    exit(1)

# Ищем конец: первый "}," на той же глубине отступа, что и начало
lines = code.split('\n')
# Найти номер строки с start_marker
start_line_idx = None
for i, line in enumerate(lines):
    if start_marker in line:
        start_line_idx = i
        start_indent = len(line) - len(line.lstrip())
        break

if start_line_idx is None:
    print("❌ Не найден start_line_idx")
    exit(1)

# Идём вниз, ищем строку, которая начинается с того же отступа и содержит "}," 
# (или просто "}," на этой глубине)
end_line_idx = None
for i in range(start_line_idx + 1, len(lines)):
    stripped = lines[i].lstrip()
    indent = len(lines[i]) - len(stripped)
    if stripped.startswith("},") and indent == start_indent:
        end_line_idx = i
        break
    if stripped.startswith(") {") and indent <= start_indent:
        # Конец Popup — тоже подходит
        end_line_idx = i - 1
        break

if end_line_idx is None:
    print("❌ Не найден конец блока")
    exit(1)

print(f"✅ Блок от строки {start_line_idx+1} до {end_line_idx+1}")

# Формируем новый блок offset
new_lines = [
    ' ' * start_indent + 'offset = androidx.compose.ui.unit.IntOffset(',
    ' ' * (start_indent + 4) + 'x = selectedMessageOffset.x.toInt(),',
    ' ' * (start_indent + 4) + 'y = selectedMessageOffset.y.toInt()',
    ' ' * start_indent + '),'
]

# Заменяем строки
new_code_lines = lines[:start_line_idx] + new_lines + lines[end_line_idx + 1:]
code = '\n'.join(new_code_lines)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(code)
print(f"💾 Сохранено: {file_path}")
