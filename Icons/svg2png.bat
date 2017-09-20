@ECHO OFF

MKDIR drawable-mdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=90 --export-png=drawable-mdpi\%~n1.png %1

MKDIR drawable-hdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=135 --export-png=drawable-hdpi\%~n1.png %1

MKDIR drawable-xhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=180 --export-png=drawable-xhdpi\%~n1.png %1

MKDIR drawable-xxhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=270 --export-png=drawable-xxhdpi\%~n1.png %1

MKDIR drawable-xxxhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=360 --export-png=drawable-xxxhdpi\%~n1.png %1

PAUSE
