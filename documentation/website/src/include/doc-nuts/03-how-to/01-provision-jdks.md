---
title: Provision JDK
---



## Provision a JDK

### Auto-detect local JDKs

```sh
nuts settings add java --search
```

### Download a specific JDK

```sh
nuts settings add java --download --jdk --version=21
```

### Download only JRE (smaller)

```sh
nuts settings add java --download --jre --version=21
```

### Select vendor (Temurin default, SPI-extensible)

```sh
nuts settings add java --download --vendor=temurin --version=21
```
