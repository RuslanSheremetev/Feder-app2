import sys

P = "app/src/main/java/com/feder/compose/ui/screen/ChatScreen.kt"
lines = open(P, encoding="utf-8").readlines()

def find_all(kw):
    return [i for i,l in enumerate(lines) if kw in l]

def show(start, count, title):
    print("\n" + "="*70)
    print(f"### {title} (строки {start+1}-{start+count})")
    print("="*70)
    for k in range(start, min(len(lines), start+count)):
        print(f"{k+1:5d}: {lines[k].rstrip()}")

# 0. Проверка: применился ли вообще weight(1f) к Box сообщения
print("### 0. weight(1f) на Box сообщения:")
for i in find_all("Box(modifier = Modifier.weight(1f)"):
    print(f"  строка {i+1}: {lines[i].rstrip()}")
if not find_all("Box(modifier = Modifier.weight(1f)"):
    print("  НЕ НАЙДЕН — значит патч не применился!")

# 1. Сигнатура и начало MessageBubble + все usage selectionMode внутри неё
mb = find_all("fun MessageBubble")
if mb:
    start = mb[0]
    # найти конец функции (следующий fun на том же уровне отступа)
    print("\n### 1. MessageBubble: все упоминания selectionMode/alpha/visible внутри:")
    for i in range(start, min(len(lines), start+400)):
        l = lines[i]
        if any(k in l for k in ["selectionMode", "alpha", "visible", "size(0", "if (", "else"]):
            print(f"{i+1:5d}: {l.rstrip()}")
    show(start, 30, "MessageBubble: сигнатура")
else:
    print("### 1. fun MessageBubble НЕ НАЙДЕН (возможно в другом файле)")

# 2. Полный блок items: от msgIndexMap до конца Row (2183-2300)
idx = find_all("val index = msgIndexMap[msg.id]")
if idx:
    show(idx[0]-10, 130, "items { msg -> ... } полный блок рендера")

# 3. onLongClick в items (что ещё ставится кроме selectionMode)
ol = [i for i in find_all("onLongClick = {") if i > 2200]
if ol:
    show(ol[0]-3, 12, "onLongClick в списке")

# 4. Overlay-блок после LazyColumn (2288)
ov = [i for i in find_all("if (selectionMode) {") if i > 2250]
for i in ov:
    show(i-2, 15, f"selectionMode-блок после строки {i+1}")

# 5. Верхняя панель select (1900)
tp = find_all("if (selectionMode) {")
if tp:
    show(tp[0]-2, 20, "Верхняя панель select (первое вхождение)")

print("\n### ДАМП ГОТОВ")
