---
title: Design Constraints
---


## Design Constraints

Nuts was built under three hard constraints that shaped every decision:

1. **Zero dependencies:** The bootstrap JAR must install and run itself without any external libraries.
   This is why Nuts includes its own CLI parser, expression engine, and text formatter.

2. **Maven-native:** No new packaging format. Every Maven artifact is already a Nuts package.

3. **Cross-platform:** Linux, macOS, and Windows must have identical behavior, including shell integration.

## Self-Hosting

Nuts installs, updates, and uninstalls itself using its own package manager.
There is no separate installer framework.