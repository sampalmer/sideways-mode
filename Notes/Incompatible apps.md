### The problem

Sideways Mode controls the screen orientation by turning off auto-rotate and changing [the default screen orientation](https://developer.android.com/reference/android/provider/Settings.System.html#USER_ROTATION). This works well for most apps, but it doesn't work with some apps that use their own screen orientation preferences. As a workaround, users can tell Sideways Mode to force the screen orientation for certain apps.

### Examples

App | Rotation control
--- | ----------------
YouTube | Programmatically sets activity orientation to portrait while system auto-rotation is disabled
Apps that use YouTube APIs | Programmatically sets activity orientation to portrait while system auto-rotation is disabled
VLC | Programmatically sets activity orientation to sensor rotation
SBS On Demand | Programmatically sets activity orientation to sensor rotation
7plus | Programmatically sets activity orientation to portrait while system auto-rotation is disabled
Spaceflight Simulator | Programmatically sets activity orientation to portrait while in-game setting is off
