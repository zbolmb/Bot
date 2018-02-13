# MapleBot

## Setting up AutoSuicide Screenshot

Initially, you want to take a screenshot of the maple icon on the maplestory window

You want to retake the images ...

blackheart, cashshop, haku, hypertele, incashshop, item, mapleIcon, mesofarmspot, portalLocation, suicidespot

Things to note:

MAKE SURE when you take a picture of mapleIcon, that you don't include anything outside of the window (like your desktop)

MAKE SURE when taking a screen shot of mesofarmspot, that it is already selected and only take the first half of the entry in the teleport rock. This is because once selected, the cursor will block off some of the text, so just take a screenshot of 'fantasy'

MAKE SURE when taking a screen shot of suicidespot, that you only take a picture of the first half, so the cursor isn't accidently blocking it, just like mesofarmspot.

MAKE SURE to take a screenshot of portalLocation when you are standing in the left of the map. More specifically, the code teleports to fantasy theme park 3 and then immediately checks for a matching image. So to accurately take screenshot, use the tp rock to fantasy theme park and screenshot from there. Also note to make sure it doesn't get clipped by burning map effect notification.

MAKE SURE to take a picture of blackheart and haku and the location of respawn right after death and take it right after it is being cast. In this case, respawn is omega sector. at top of building. If you don't know where this is, just kys once and you will respawn there.

## Debugging

If you get image not found during certain parts of the process, you can force the code to keep kmsing or keep trying to get back to byebye. Just uncomment lines 325, 403.

```
if (curExp > 0.6) {
// if (true) {
```

and

```
while (getExp() > 0) {
// while (true) {
```


### To create cmd shortcut in taskbar that runs the program

Pin cmd to taskbar

Right click it in taskbar, right click command prompt, click properties

Set target field to below

%windir%\system32\cmd.exe /C "C:\path\to\MapleBot\bot.bat"

Rewrite the bot.bat file to do whatever it needs to run your program ie,

```
cd path/to/mapleBot
java maple
```