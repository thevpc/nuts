# Nuts Extensions

This directory contains optional extension plugins for Nuts runtime features.

## Included Extensions

| Extension | Description |
| :--- | :--- |
| **`nuts-term`** | Terminal extension based on JLine, providing rich terminal features, ANSI colors, auto-completion, and command history. |
| **`nuts-ssh`** | SSH transport extension enabling remote workspace management and deployment over SSH. |

---

## Building Extensions

Build extensions from repository root:
```bash
mvn clean install -pl extensions
```