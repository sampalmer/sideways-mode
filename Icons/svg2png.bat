@ECHO OFF

MKDIR mipmap-mdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=90 --export-png=mipmap-mdpi\%~n1.png %1

MKDIR mipmap-hdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=135 --export-png=mipmap-hdpi\%~n1.png %1

MKDIR mipmap-xhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=180 --export-png=mipmap-xhdpi\%~n1.png %1

MKDIR mipmap-xxhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=270 --export-png=mipmap-xxhdpi\%~n1.png %1

MKDIR mipmap-xxxhdpi 2>NUL
"C:\Program Files\Inkscape\inkscape.com" --without-gui --export-dpi=360 --export-png=mipmap-xxxhdpi\%~n1.png %1

PAUSE
