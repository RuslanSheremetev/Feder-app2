import time, shutil, re

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)
print(f"📦 Бэкап: {backup_path}")

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# Ищем блок popupPositionProvider и заменяем на прямой offset
# Паттерн: popupPositionProvider = remember { object : ... { ... } },
pattern = r'popupPositionProvider\s*=\s*remember\s*\{\s*object\s*:\s*androidx\.compose\.ui\.window\.PopupPositionProvider\s*\{[\s\S]*?\}\s*\},'

new_offset = """offset = androidx.compose.ui.unit.IntOffset(
                    x = selectedMessageOffset.x.toInt(),
                    y = selectedMessageOffset.y.toInt()
                ),"""

new_code = re.sub(pattern, new_offset, code, count=1)

if new_code != code:
    code = new_code
    print("✅ PopupPositionProvider заменен на прямой offset")
else:
    print("⚠️ Паттерн не найден. Пробуем другой подход...")
    # Альтернативный поиск по строкам
    lines = code.split('\n')
    out_lines = []
    skip = 0
    for i, line in enumerate(lines):
        if 'popupPositionProvider = remember' in line:
            # Пропускаем до закрывающей скобки '},'
            indent = len(line) - len(line.lstrip())
            out_lines.append(' ' * indent + 'offset = androidx.compose.ui.unit.IntOffset(')
            out_lines.append(' ' * (indent + 4) + 'x = selectedMessageOffset.x.toInt(),')
            out_lines.append(' ' * (indent + 4) + 'y = selectedMessageOffset.y.toInt()')
            out_lines.append(' ' * indent + '),')
            skip = 1
            continue
        if skip:
            if line.strip() == '},' or line.strip() == '}':
                skip = 0
            continue
        out_lines.append(line)
    code = '\n'.join(out_lines)
    print("✅ Замена выполнена построчно")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(code)
print(f"💾 Сохранено: {file_path}")
