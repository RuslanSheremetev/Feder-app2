import time, shutil, re

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)
print(f"📦 Бэкап: {backup_path}")

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# ─── ШАГ 1: Удалить строку с LocalConfiguration внутри onClick ───
pattern_del = r'\n\s*val screenWidthDp = androidx\.compose\.ui\.platform\.LocalConfiguration\.current\.screenWidthDp\.toFloat\(\)'
before_len = len(code)
code = re.sub(pattern_del, '', code, count=1)
if len(code) < before_len:
    print("✅ Удалена строка с LocalConfiguration из onClick")
else:
    print("⚠️ Строка с LocalConfiguration не найдена")

# ─── ШАГ 2: Добавить val screenWidthDp в тело MessageBubble ───
marker = "val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current"
if marker in code:
    if "val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.toFloat()" not in code.split(marker)[1].split("\n")[0:5][0] if marker in code else False:
        pass
    # Проверяем, есть ли уже screenWidthDp сразу после haptic (в ближайших 5 строках)
    idx = code.find(marker)
    snippet = code[idx:idx+300]
    if "val screenWidthDp" not in snippet:
        code = code.replace(marker, 
            marker + "\n        val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.toFloat()",
            1)
        print("✅ Добавлена val screenWidthDp после haptic в MessageBubble")
    else:
        print("ℹ️ val screenWidthDp уже есть после haptic")
else:
    print("⚠️ Маркер haptic не найден")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(code)

print(f"💾 Сохранено: {file_path}")
