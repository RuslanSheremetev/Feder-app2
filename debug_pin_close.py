import sys

# 1. Клиент: ищем обработчик крестика в ChatScreen.kt
print("=" * 60)
print("КЛИЕНТ: обработчик крестика в ChatScreen.kt")
print("=" * 60)
with open("app/src/main/java/com/feder/compose/ui/screen/ChatScreen.kt", 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Находим IconButton с Close
for i, line in enumerate(lines):
    if 'Icons.Filled.Close' in line and 'unpin' in line:
        print(f"\nСтрока {i+1}: {line.rstrip()}")
        # Показываем контекст: onClick и что внутри
        for j in range(max(0, i-10), min(len(lines), i+15)):
            print(f"{j+1:4d}: {lines[j].rstrip()}")
        break

# 2. Сервер: ищем эндпоинт /api/chat/pin
print("\n" + "=" * 60)
print("СЕРВЕР: эндпоинт /api/chat/pin в simple_server.c")
print("=" * 60)
with open("/root/Feder_server/src/core/simple_server.c", 'r', encoding='utf-8') as f:
    server_lines = f.readlines()

for i, line in enumerate(server_lines):
    if '/api/chat/pin' in line:
        print(f"\nСтрока {i+1}: {line.rstrip()}")
        # Показываем весь блок обработки
        for j in range(max(0, i-2), min(len(server_lines), i+25)):
            print(f"{j+1:4d}: {server_lines[j].rstrip()}")
        break

# 3. Проверяем, есть ли UPDATE в БД
print("\n" + "=" * 60)
print("СЕРВЕР: UPDATE запрос в эндпоинте")
print("=" * 60)
for i, line in enumerate(server_lines):
    if 'UPDATE messages SET is_pinned' in line:
        print(f"Строка {i+1}: {line.rstrip()}")

# 4. Проверяем, как читается is_pinned_val
print("\n" + "=" * 60)
print("СЕРВЕР: чтение is_pinned_val")
print("=" * 60)
for i, line in enumerate(server_lines):
    if 'is_pinned_val' in line and 'PQgetvalue' in line:
        print(f"Строка {i+1}: {line.rstrip()}")

