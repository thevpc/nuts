---
id: app-xtsunami
title: Google Tsunami
sidebar_label: Google Tsunami
order: 2
---


## T0016- Google Tsunami (Security Scanner)
Google Tsunami is a general purpose network security scanner with an extensible plugin system for detecting high severity vulnerabilities with high confidence.
```
nuts com.google.tsunami:tsunami-main
nuts settings add alias tsunami='--cp=${NUTS_ID_APPS}/your-plugins-folder/*.jar tsunami-main'
# Example of usage
nuts tsunami --ip-v4-target=127.0.0.1
``` 