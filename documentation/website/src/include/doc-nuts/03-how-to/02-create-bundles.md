---
title: Create Bundles
---



## Create an Air-Gapped Bundle

```sh
nuts bundle myapp#1.2.3 --output myapp-bundle.jar
```

The bundle is a self-contained executable JAR.
On the target machine (even offline):

```sh
java -jar myapp-bundle.jar
```

This recreates the workspace, installs artifacts, and runs the app.