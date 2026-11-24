import sys

P = "app/src/main/java/com/feder/compose/ui/screen/ChatScreen.kt"
try:
    with open(P, 'r', encoding='utf-8') as f:
        lines = f.readlines()
except Exception as e:
    print(f"ERROR: {e}")
    sys.exit(1)

def dump_context(keyword, lines_after=45, lines_before=5):
    print(f"\n{'='*70}")
    print(f" ПОИСК: '{keyword}'")
    print(f"{'='*70}")
    found = False
    for i, line in enumerate(lines):
        if keyword in line:
            found = True
            start = max(0, i - lines_before)
            end = min(len(lines), i + lines_after)
            for k in range(start, end):
                # Подсветка строки с ключевым словом
                prefix = ">>> " if keyword in lines[k] else "    "
                print(f"{prefix}{k+1:4d}: {lines[k].rstrip()}")
            print("-" * 70)
            break # Берем только первое вхождение для чистоты
    if not found:
        print(f"НЕ НАЙДЕНО: '{keyword}'")

print("ЗАПУСК ДИАГНОСТИКИ PIN И SELECT MODE...")

# 1. Объявления состояний (ищем петлю рекомпозиции)
dump_context("var pinnedMessage by")
dump_context("var selectionMode by")
dump_context("var selectedMessages by")

# 2. Логика Pin в меню сообщения (должна быть оптимистичной)
dump_context("val isP = selectedMessage!!.isPinned")

# 3. Логика Unpin в баннере (крестик)
dump_context("val mId = pinnedMessage!!.id")

# 4. Рендер сообщения в режиме выделения (где схлопывается Box)
# Ищем именно Row, который содержит и чекбокс, и MessageBubble
dump_context("if (selectionMode && selectedMessages.isNotEmpty())", lines_after=60)
dump_context("if (selectionMode) {", lines_after=50) # Второй раз для самого чекбокса

# 5. LaunchedEffect, которые могут вызывать бесконечный цикл при изменении messages
dump_context("LaunchedEffect(messages)")

print("\n" + "="*70)
print(" ДИАГНОСТИКА ЗАВЕРШЕНА. Скопируй весь вывод выше.")
print("="*70)
