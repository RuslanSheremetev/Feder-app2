import sys

P = "app/src/main/java/com/feder/compose/ui/screen/ChatScreen.kt"
lines = open(P, encoding="utf-8").readlines()

print("=" * 70)
print(" ПОИСК: как рендерятся файлы в MessageBubble")
print("=" * 70)

# 1. Ищем messageType == "file" или проверку на файл
for i, line in enumerate(lines):
    if 'messageType' in line and ('file' in line.lower() or 'document' in line.lower()):
        print(f"\nСтрока {i+1}: {line.rstrip()}")
        # Показываем контекст
        for j in range(max(0, i-5), min(len(lines), i+15)):
            prefix = ">>> " if j == i else "    "
            print(f"{prefix}{j+1:4d}: {lines[j].rstrip()}")
        print("-" * 70)

# 2. Ищем, где проверяется imageUrl/imageUrls (возможно файлы идут туда)
print("\n" + "=" * 70)
print(" ПРОВЕРКА: где обрабатываются imageUrls в MessageBubble")
print("=" * 70)
for i, line in enumerate(lines):
    if 'imageUrls' in line and i > 370 and i < 800:
        if 'isNotEmpty' in line or 'isEmpty' in line:
            print(f"Строка {i+1}: {line.rstrip()[:120]}")

# 3. Ищем, где рендерится "пустой" пузырь (возможно это fallback)
print("\n" + "=" * 70)
print(" ПРОВЕРКА: где может рендериться пустой пузырь")
print("=" * 70)
for i, line in enumerate(lines):
    if 'Box(' in line and i > 480 and i < 520:
        print(f"Строка {i+1}: {line.rstrip()[:120]}")

