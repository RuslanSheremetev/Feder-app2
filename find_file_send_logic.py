import sys

P = "app/src/main/java/com/feder/compose/ui/screen/ChatScreen.kt"
try:
    lines = open(P, encoding="utf-8").readlines()
except Exception as e:
    print(f"ERROR: {e}")
    sys.exit(1)

print("=" * 70)
print(" 1. ПОИСК: где клиент собирает JSON с message_type = file")
print("=" * 70)
found = False
for i, line in enumerate(lines):
    if 'message_type' in line.lower() and ('file' in line.lower() or 'document' in line.lower() or 'upload' in line.lower()):
        found = True
        print(f"\nНайдено на строке {i+1}:")
        start = max(0, i - 15)
        end = min(len(lines), i + 20)
        for j in range(start, end):
            prefix = ">>> " if j == i else "    "
            print(f"{prefix}{j+1:4d}: {lines[j].rstrip()}")
        print("-" * 70)

if not found:
    print("Явного 'message_type' с 'file' не найдено. Ищем сборку JSON для отправки...")

print("\n" + "=" * 70)
print(" 2. ПОИСК: функции отправки (sendMessage / sendFile / upload)")
print("=" * 70)
for i, line in enumerate(lines):
    if 'fun sendMessage' in line or 'fun sendFile' in line or 'fun uploadFile' in line:
        print(f"Найдено на строке {i+1}: {line.rstrip()}")
        for j in range(i, min(len(lines), i + 40)):
            print(f"    {j+1:4d}: {lines[j].rstrip()}")
        print("-" * 70)
        break # Берем первую найденную функцию отправки
