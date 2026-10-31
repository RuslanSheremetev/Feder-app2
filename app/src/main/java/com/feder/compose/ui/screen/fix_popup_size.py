import time, shutil

file_path = 'ChatScreen.kt'
backup_path = f'ChatScreen.kt.bak_{int(time.time())}'
shutil.copy(file_path, backup_path)
print(f"📦 Бэкап: {backup_path}")

with open(file_path, 'r', encoding='utf-8') as f:
    code = f.read()

# Убираем fillMaxSize из Box внутри Popup
old_box = "Box(Modifier.fillMaxSize().clickable { selectedMessage = null; showDeleteSub = false }) {"
new_box = "Box(Modifier.wrapContentSize().clickable { selectedMessage = null; showDeleteSub = false }) {"

if old_box in code:
    code = code.replace(old_box, new_box, 1)
    print("✅ Убран fillMaxSize из Box внутри Popup (заменён на wrapContentSize)")
else:
    print("⚠️ fillMaxSize не найден")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(code)
print(f"💾 Сохранено: {file_path}")
