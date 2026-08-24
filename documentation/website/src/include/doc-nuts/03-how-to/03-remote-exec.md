---
title: Remote Execution
---


## Run on a Remote Host


```shell
nuts --at=ssh://user@host myapp
```

Nuts will:
1. Bundle the app locally
2. SCP the bundle to the remote host
3. Self-install on the remote host (no internet required)
4. Execute and stream output back

Requirements: SSH key-based auth, Java on remote host (or use bundle with JRE).
