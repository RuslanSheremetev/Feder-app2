import sys

P = "app/src/main/java/com/feder/compose/ui/screen/ChatScreen.kt"
lines = open(P, encoding="utf-8").readlines()

print("=" * 70)
print(" 1. СТРУКТУРА MsgItem (какие поля есть для файлов?)")
print("=" * 70)
in_msg_item = False
for i, line in enumerate(lines):
    if "data class MsgItem" in line:
        in_msg_item = True
    if in_msg_item:
        print(f"{i+1:4d}: {line.rstrip()}")
        if line.strip().startswith(")"): # Конец data class
            break

print("\n" + "=" * 70)
print(" 2. ТЕКУЩИЙ FALLBACK (где рендерится текст вместо файла, ~строка 775)")
print("=" * 70)
start = 765
end = 800
for i in range(start, min(len(lines), end)):
    print(f"{i+1:4d}: {lines[i].rstrip()}")

print("\n" + "=" * 70)
print(" 3. ЕСТЬ ЛИ УЖЕ ФУНКЦИЯ ОПРЕДЕЛЕНИЯ ИКОНКИ ФАЙЛА?")
print("=" * 70)
for i, line in enumerate(lines):
    if "fun fileIcon" in line or "fun getFileIcon" in line:
        print(f"Найдено на строке {i+1}: {line.rstrip()}")
        for j in range(i, min(len(lines), i+15)):
            print(f"    {j+1:4d}: {lines[j].rstrip()}")
        break
else:
    print("Функция определения иконки не найдена (добавим свою).")
