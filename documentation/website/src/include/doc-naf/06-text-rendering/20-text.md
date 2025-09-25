---
title: Rendering Text
---


Bring your CLI, logs, or console output to life with NTextArt. Render
text as classic ASCII banners, pixel-style visuals, or even
image-like representations, with multiple renderers at your
fingertips — all workspace-aware and fully embeddable.


## Basic Usage


```java
    NTextArt art = NTextArt.of();
NText text = NText.of("hello world");
    NOut.println(art.getTextRenderer("figlet:standard").get().render(text));
```

Output:
```
  _                _    _                                             _        _
 | |              | |  | |                                           | |      | |
 | |__      ___   | |  | |    ___       __      __    ___     _ __   | |    __| |
 | '_ \    / _ \  | |  | |   / _ \      \ \ /\ / /   / _ \   | '__|  | |   / _` |
 | | | |  |  __/  | |  | |  | (_) |      \ V  V /   | (_) |  | |     | |  | (_| |
 |_| |_|   \___|  |_|  |_|   \___/        \_/\_/     \___/   |_|     |_|   \__,_|

```

You can choose from multiple built-in figlet renderers or even use your own.


```java
    NTextArt art = NTextArt.of();
    
    NOut.println(art.getImageRenderer("pixel:standard").get()
            .setFontSize(20) .setOutputColumns(60) .render(text));
```

Output:
```

 █         ░██   ███                             ███      █
 █           █    ▓█                              ░█      █
 █ █▒  ░██   █    ▓█    █▒       █   █  █▓  ██░█░ ░█   ▒█ █
 █▓▓█░░█ █░  █    ▓█  ░█ ▓▒      █ █ █░█ ▓▓ ██▓▓  ░█   █░▒█
 █  █░█████  █    ▓█  ▓█ ▒█      █▓█ ▓▒█ ░█ ██    ░█  ░█  █
 █  █░ █     ██   ▓█   █ █░      ██░█▓ █ █▒ ██    ░█░ ░█░██
 ▓  ▓░ ░▓▓    ▓▓   ▓▓░  ▓░       ░▓ ▓   ▓▒  ▒▒     ░▓▓ ░▓ ▓
```
