P = "app/src/main/java/com/feder/compose/ui/screen/ChatScreen.kt"
lines = open(P, encoding="utf-8").readlines()

# 1. Где текст "Галерея" — маркер ряда иконок вложений
gal = [i for i,l in enumerate(lines) if "Галерея" in l]
print("### 'Галерея' на строках:", [i+1 for i in gal])
if not gal:
    print("НЕ НАЙДЕНО 'Галерея' — ищем 'Локация'")
    gal = [i for i,l in enumerate(lines) if "Локация" in l]
    print("### 'Локация' на строках:", [i+1 for i in gal])

if gal:
    g = gal[0]
    # идём вверх до ближайшего Row( — это контейнер ряда
    row_start = None
    for i in range(g, max(0, g-60), -1):
        if "Row(" in lines[i] and ("horizontalArrangement" in lines[i] or "verticalAlignment" in lines[i] or "Modifier" in lines[i]):
            row_start = i
            break
    if row_start is None:
        for i in range(g, max(0, g-60), -1):
            if lines[i].strip().startswith("Row("):
                row_start = i; break
    print(f"\n### РЯД ИКОНОК: контейнер Row начинается со строки {row_start+1 if row_start else '?'}")
    if row_start is not None:
        for k in range(row_start, min(len(lines), row_start+55)):
            print(f"{k+1:5d}: {lines[k].rstrip()}")

# 2. Все Row в окне ±40 строк вокруг 'Галерея' + их horizontalArrangement
print("\n### Все Row рядом и их Arrangement:")
for i,l in enumerate(lines):
    if abs(i-g) <= 40 and "Row(" in l:
        print(f"{i+1:5d}: {l.strip()[:120]}")
