---
id: downloadManual
title: Manual Download 
---

Download Manual Installer. 

Use one commandline to download and install Nuts package manager with the help of cUrl command. Use 'Portable' version for production and 'Preview' version for all other cases. A valid java 1.8+ runtime is required.

## Stable 0.8.5 Jar

```bash
curl -sOL https://thevpc.net/maven/net/thevpc/nuts/nuts-app/0.8.5/nuts-app-0.8.5.jar -o nuts.jar && java -jar nuts-app.jar -Zy
```

## Stable 0.8.5.1+ Jar

```bash
curl -sOL https://thevpc.net/nuts/nuts-preview.jar -o nuts.jar && java -jar nuts-app.jar -Zy
```


## Older versions

## 0.8.3

```bash
curl -sOL https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/0.8.3/nuts-0.8.3.jar -o nuts.jar && java -jar nuts.jar -Zy
```
