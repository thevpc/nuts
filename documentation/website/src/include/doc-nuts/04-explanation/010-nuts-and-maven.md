---
id: nutsAndMaven
title: Nuts and Maven
sidebar_label: Nuts and Maven
---


Nuts is not a build tool. It is a runtime package manager that consumes Maven artifacts.

|                       | Nuts | Maven | SDKMAN         | jbang        | Homebrew             |
| --------------------- | ---- | ----- | -------------- | ------------ | -------------------- |
| Installs apps         | ✅    | ❌     | ⚠️ (JDKs only) | ⚠️ (scripts) | ✅                    |
| Uses Maven metadata   | ✅    | ✅     | ❌              | ✅            | ❌                    |
| Side-by-side versions | ✅    | ❌     | ✅              | ❌            | ❌                    |
| Zero dependencies     | ✅    | ❌     | ❌              | ❌            | ❌                    |
| Cross-platform        | ✅    | ✅     | ⚠️             | ✅            | ❌ (macOS/Linux only) |
| JDK provisioning      | ✅    | ❌     | ✅              | ❌            | ❌                    |

