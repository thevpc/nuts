---
title: Universal Path
subTitle:  |
  NPath gives you a unified way to work with both local and remote
  paths, letting you read, write, and manipulate files from HTTP, SSH,
  or the local filesystem with the same simple API. It also provides
  convenient access to standard system and user directories, including
  home, working, configuration, and application paths, making
  cross-platform file handling seamless and consistent across
  environments.
contentType: java
---

// manipulate local and remote paths
String content=NPath.of("http://myserver/myfile.txt").readString();
String[] lines=NPath.of("ssh://myserver/myfile.txt").lines().toArray(String[]::new);

// access local standard folders
NPath.ofUserHome();         // User home directory
NPath.ofUserDirectory();    // Current working directory
// User store path (~/.config/nuts on linux, C:\Users\{user}\AppData\Local\nuts on windows)
NPath.ofUserStore(NStoreType.CONFIG);
// User store path (/opt/nuts/ on linux, C:\Program Files\nuts on windows)
NPath.ofSystemStore(NStoreType.BIN);  // System store path
