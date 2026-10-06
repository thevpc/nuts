---
id: quick-reference
title: Quick Reference
sidebar_label: Quick Reference
---

# Quick Reference

| Task | Command |
| --- | --- |
| Install an app | `nuts install group:artifact` |
| Run an app | `nuts artifact` |
| Run a specific version | `nuts artifact#version` |
| List installed apps | `nuts search --installed` |
| Update all apps | `nuts update` |
| Create a workspace | `nuts -w name install app` |
| Provision a JDK | `nuts settings add java --download --version=21` |
| Create an offline bundle | `nuts bundle app#version` |
| Run on a remote host | `nuts exec --target=ssh://host app` |
| Reset workspace | `nuts -Z` |
| Enable secure mode | `nuts settings secure true` |
