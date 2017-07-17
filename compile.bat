@echo off
set "gta3sc=%cd%\tools\gta3sc\gta3sc.exe"
set "sanny=%cd%\tools\SannyBuilder3\sanny.exe"

rem Compile gta3sc scripts
cd gta3
forfiles /s /m *.sc /c ^"cmd /c ^
echo Processing: @path ^&^
\"%gta3sc%\" compile @file --config=gta3 --cs --guesser -fconst ^&^
exit 0"
cd ..

cd gtavc
forfiles /s /m *.sc /c ^"cmd /c ^
echo Processing: @path ^&^
\"%gta3sc%\" compile @file --config=gtavc --cs --guesser -fconst ^&^
exit 0"
cd ..

cd gtasa
forfiles /s /m *.sc /c ^"cmd /c ^
echo Processing: @path ^&^
\"%gta3sc%\" compile @file --config=gtasa --cs --guesser -fconst ^&^
exit 0"
cd ..

rem Compile sannybuilder scripts
cd gta3
forfiles /s /m *.txt /c ^"cmd /c ^
echo Processing: @path ^&^
\"%sanny%\" \nosplash \gta3 \compile @path ^&^
exit 0"
cd ..

cd gtavc
forfiles /s /m *.txt /c ^"cmd /c ^
echo Processing: @path ^&^
\"%sanny%\" \nosplash \vc \compile @path ^&^
exit 0"
cd ..

cd gtasa
forfiles /s /m *.txt /c ^"cmd /c ^
echo Processing: @path ^&^
\"%sanny%\" \nosplash \sa \compile @path ^&^
exit 0"
cd ..