forfiles /s /m *.sc /c ^"cmd /c ^
echo Processing: @path ^&^
\"../gta3sc-0.9.4-x86-win32/gta3sc.exe\" compile @file --config=gtasa --cs --guesser -fconst ^&^
"
exit 0