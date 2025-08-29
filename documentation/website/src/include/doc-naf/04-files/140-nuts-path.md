---
title: NPath
---



NPath is a powerful abstraction introduced by nuts to handle paths in a uniform way, similar to Java's URL, but with extended capabilities, built-in protocol support, and a fluent, intuitive API.

# Key Features

- Unified path abstraction for local files, URLs, classpath resources, and artifacts.
- Protocol-aware: supports file, http(s), classpath, and resource URLs.
- Stream access, input/output helpers, and file tree navigation.
- Support for creating temporary files/folders and content manipulation.

## Supported Protocols

- File paths: "/path/to/resource", "C:\\path\\to\\resource"
- File URLs: "file:/path/to/resource", "file:C:/path/to/resource"
- HTTP/HTTPS URLs: "http://...", "https://..."
- Ssh URLs: "ssh://user@server/path/to/resource"
- Classpath: "classpath:/path/to/resource" (requires classloader)
- Resource paths: "resource://group:artifact#version/path/to/resource"

## Creating an NPath

```java
NPath localFile = NPath.of("C:/path/to/resource");
```

## Other creation methods include:


```java
NPath.of(URL url);
NPath.of(File file);
NPath.of(Path path);
NPath.of(String path, ClassLoader cl);
NPath.of(NConnexionString connection);
```

## Special Locations

```java
NPath.ofUserHome();         // User home directory
NPath.ofUserDirectory();    // Current working directory
NPath.ofUserStore(type);    // User store path
NPath.ofSystemStore(type);  // System store path
```

## NPath.ofUserStore(NStoreType storeType)

Returns the path to the user-specific store folder of the given storeType.

This method is used to access various predefined storage locations that conform to standard OS conventions (such as XDG Base Directory Specification on Linux). These locations are used to store user-specific data in structured, OS-compliant folders.

Example:
```java
NPath configFolder = NPath.ofUserStore(NStoreType.CONF);
System.out.println("User config path: " + configFolder);
```

### Supported `NStoreType`s

Each `NStoreType` corresponds to a logical category of user data:

| StoreType | Purpose | Typical Usage | Linux Equivalent |
|-----------|---------|---------------|------------------|
| `BIN`     | Stores user-specific executable binaries (non-modifiable). | Custom installed commands/tools. | `$HOME/.local/bin` |
| `CONF`    | Stores user-specific configuration files. | App or tool settings, preferences. | `$XDG_CONFIG_HOME` or `$HOME/.config` |
| `VAR`     | Stores user-specific modifiable data. | Data files, downloaded content. | `$XDG_DATA_HOME` or `$HOME/.local/share` |
| `LOG`     | Stores log files. | Runtime logs, audit trails. | `$XDG_LOG_HOME` or `$HOME/.local/log` |
| `TEMP`    | Stores temporary files. | Temp input/output files. | `$TMPDIR`, `/tmp`, or equivalent |
| `CACHE`   | Stores non-essential cache files. | Cached packages, resources. | `$XDG_CACHE_HOME` or `$HOME/.cache` |
| `LIB`     | Stores user-specific non-executable binaries. | Local libraries and dependencies. | `$HOME/.local/lib` |
| `RUN`     | Stores runtime file system entries. | Sockets, pipes, PID files. | `$XDG_RUNTIME_DIR` or `/run/user/<uid>` |


### Why Use ofUserStore()?
This method ensures:
- Cross-platform compatibility: Automatically maps to platform-appropriate folders.
- Correct file placement: Keeps your workspace clean and organized.
- Portable behavior: Works reliably across Linux, Windows, and macOS.

### Advanced Example: Write to User Log Folder

```java
NPath logFile = NPath.ofUserStore(NStoreType.LOG).resolve("myapp.log");
logFile.writeString("This is a log entry.\n", StandardCharsets.UTF_8);
```

:::tip
Notes : You can get the resolved directory path as a string via getLocation() or toString().
:::

:::tip
Notes : If you're working in a system context (e.g. root user or shared tools), consider NPath.ofSystemStore(NStoreType).
:::


## Browsing HTML Folders
Supports Apache Tomcat and Apache Httpd directory listings.

```java
NPath httpFolder = NPath.of("htmlfs:https://archive.apache.org/dist/tomcat/");
try (NStream s = httpFolder.stream()) {
    List<NPath> matches = s.filter(x -> x.isDirectory() && x.getName().matches("tomcat-[0-9.]+"))
                            .toList();
}
```

## Working with Temp Files and Folders

```java
NPath tempFile = NPath.ofTempFile("example.txt");
NPath tempFolder = NPath.ofTempFolder("project-workspace");
```

Also supports temp locations for repositories and artifact ids:

```java
NPath.ofTempRepositoryFile("temp.txt", repository);
NPath.ofTempIdFolder(id);
```


## Content I/O
Reading:

```java
byte[] data = path.readBytes();
String content = path.readString();
```

Writing:

```java
path.writeBytes(data);
path.writeString("Hello World");
```

Writing Structured Objects:

```java
path.writeObject(myObject);     // Any serializable
path.writeMsg(NMsg.ofText("Hi"));
```


## Path Operations
- resolve, resolveSibling, normalize, toAbsolute, toRelative
- exists(), isDirectory(), isFile(), delete(), mkdir()
- getName(), getNameCount(), getNames(), getParent()

## File Tree

```java
path.walk();                    // DFS walk
path.walkGlob();               // Glob walk
```

## Streaming and Navigation
```java
try (NStream<NPath> stream = path.stream()) {
    stream.forEach(x -> ...);
}
```

## Permissions & Metadata

```java
Set<NPathPermission> perms = path.getPermissions();
path.setPermissions(...);
```

## Type & Protocol

```java
String protocol = path.getProtocol();
boolean isFile = path.isFile();
boolean isHttp = path.isHttp();
NPathType type = path.type();
```

## Conversion

```java
URL url = path.toURL().orNull();
File file = path.toFile().orNull();
Path nioPath = path.toPath().orNull();
```


## Example: Directory Listing
```java
NPath dir = NPath.of("/my/folder");
List<NPath> files = dir.stream()
    .filter(p -> p.getName().endsWith(".txt"))
    .toList();
```

## Summary
NPath is a versatile and protocol-aware abstraction that unifies file, URL, and resource path handling. Its rich API and Nuts integration make it ideal for building tools that require flexible resource access, remote artifact inspection, or file system utilities.



