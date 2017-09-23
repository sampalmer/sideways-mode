@ECHO OFF

MKDIR drawable-mdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=96 --export-png=drawable-mdpi\%~n1.png %1

MKDIR drawable-hdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=144 --export-png=drawable-hdpi\%~n1.png %1

MKDIR drawable-xhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=192 --export-png=drawable-xhdpi\%~n1.png %1

MKDIR drawable-xxhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=288 --export-png=drawable-xxhdpi\%~n1.png %1

MKDIR drawable-xxxhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=384 --export-png=drawable-xxxhdpi\%~n1.png %1

PAUSE
