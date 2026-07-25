---
title: NPath
---

# NPath

NPath is a powerful, protocol-aware abstraction introduced by Nuts to handle resource locations in a uniform way. Similar to Java's URL or Path, but with extended capabilities, built-in protocol support, and a fluent, intuitive API, NPath bridges the gap between local files, remote URLs, classpath resources, and virtual in-memory locations.

## Key Features

- Unified Abstraction: Seamlessly handle local files, HTTP/SSH URLs, classpath resources, in-memory buffers, and artifact repositories with a single API.
- Protocol-Aware: Natively supports file, http, https, ssh, classpath, mem, and custom Nuts protocols (e.g., htmlfs+https://).
- Rich I/O Operations: First-class support for reading/writing bytes, strings, structured objects, and streaming, with automatic parent directory creation.
- Fluent Metadata & Behavior: Dynamically attach content hints (charset, content type, kind) and behavioral flags (cache, temporary) via fluent builders.
- Advanced Navigation: Robust path manipulation including relativize, stripParent, smart extension parsing (nameParts), and glob/DFS tree walking.
- Lifecycle Management: Built-in support for temporary files, auto-cleanup (deleteOnDispose), and OS-compliant user/system store locations.

---

## Supported Protocols

| Protocol | Example | Description |
|----------|---------|-------------|
| Local File | /path/to/resource, C:\path | Standard filesystem paths (protocol is implicitly ""). |
| File URL | file:/path/to/resource | Explicit file URL scheme. |
| HTTP/HTTPS | https://example.com/data.json | Remote web resources. |
| SSH | ssh://user@server/path/to/resource | Secure remote file access. |
| Classpath | classpath:/com/myapp/config.xml | Resources bundled in JARs or classpath folders. |
| In-Memory | mem://sandbox/data.txt | Virtual in-memory filesystem. Zero disk I/O, ideal for testing or transient data. |
| Nuts Resource | resource://group:artifact#version/path | Nuts-specific artifact resolution. |
| HTML FS | htmlfs+https://archive.apache.org/dist/ | Browses Apache-style HTML directory listings as a virtual filesystem. |

---

## Creating an NPath

### Basic Creation

```java
    // From String (local, URL, classpath, or memory)
    NPath localFile = NPath.of("/path/to/resource.txt");
    NPath remoteFile = NPath.of("https://example.com/data.json");
    NPath memFile = NPath.of("mem://sandbox/temp-data.txt");

    // From standard Java types
    NPath fromUrl = NPath.of(new URL("file:///tmp/test.txt"));
    NPath fromFile = NPath.of(new File("/tmp/test.txt"));
    NPath fromNio = NPath.of(Paths.get("/tmp/test.txt"));

    // From a Nuts Connection String
    NPath fromConn = NPath.of(NConnectionString.of("ssh://user@host/path"));
```

### Classpath & Origins
```java
    // Resolve with a specific ClassLoader
    NPath cpResource = NPath.of("classpath:/config.properties", MyClass.class.getClassLoader());

    // Find where a class was loaded from (e.g., which JAR)
    NOptional<NPath> origin = NPath.ofOrigin(MyClass.class);
    List<NPath> allOrigins = NPath.ofOrigins(MyClass.class);
```

---

## Behavioral Flags & Content Metadata

While structural operations (resolve, normalize) return new path instances, NPath provides fluent methods to adjust its behavior and attach content metadata. This is especially useful for virtual paths (mem://), HTTP responses, or when you need to override inferred file characteristics.

### Behavioral Flags
```java
    NPath tempFile = NPath.ofTempFile("report.pdf");

    // Mark this path to be treated as user cache (affects storage/cleanup policies)
    NPath cachedFile = tempFile.userCache(true);

    // Explicitly mark as temporary (may influence disposal or OS-level temp handling)
    NPath explicitTemp = cachedFile.userTemporary(true);
```

### Content Metadata (NContentMetadata)

The NContentMetadata interface acts as a fluent builder to attach or override metadata without altering the underlying physical resource. This is heavily utilized when NPath acts as an NInputSource or NOutputTarget.

```java
    NPath memResponse = NPath.of("mem://api/response");

    // Build metadata fluently
    NContentMetadata meta = memResponse.metaData()
        .name("user-profile.json")          // Override the logical name
        .contentType("application/json")    // Force content type (bypasses extension guessing)
        .charset("UTF-8")                   // Explicit charset
        .kind("api-response")               // Custom semantic kind
        .message(NMsg.ofInfo("Generated successfully")) // Attach a status/description
        .contentLength(1024L);              // Pre-declare length if known
```

---

## Special Locations: Stores & Temporary Files

### User and System Stores

NPath provides OS-compliant storage locations (aligning with XDG Base Directory Specification on Linux). Use NStoreKey to target a specific application (GAV) and store type.

```java
    NId appId = NId.of("com.mycompany:myapp");
    NStoreKey configKey = new NStoreKey(appId, NStoreType.CONF);
    NPath configFolder = NPath.of(configKey);

    configFolder.mkdirs();
    configFolder.resolve("settings.json").writeString("{\"theme\": \"dark\"}");
```

Supported NStoreTypes:

| StoreType | Purpose | Linux Equivalent |
|-----------|---------|------------------|
| BIN | User-specific executable binaries | $HOME/.local/bin |
| CONF | Configuration files | $XDG_CONFIG_HOME or $HOME/.config |
| VAR | Modifiable data files | $XDG_DATA_HOME or $HOME/.local/share |
| LOG | Runtime logs and audit trails | $XDG_LOG_HOME or $HOME/.local/log |
| TEMP | Temporary files | $TMPDIR or /tmp |
| CACHE | Non-essential cached data | $XDG_CACHE_HOME or $HOME/.cache |
| LIB | Non-executable libraries | $HOME/.local/lib |
| RUN | Runtime files (sockets, PID files) | $XDG_RUNTIME_DIR |

### Temporary Files and Folders

```java
    // Workspace-level temp file/folder
    NPath tempFile = NPath.ofTempFile("buffer.bin");
    NPath tempFolder = NPath.ofTempFolder("project-workspace");

    // Repository-scoped temp file
    NPath repoTemp = NPath.ofTempRepositoryFile("download.tmp", myRepository);

    // ID-scoped temp folder
    NPath idTemp = NPath.ofTempIdFolder("build-output", NId.of("com.example:lib:1.0"));

    // AUTO-CLEANUP: Mark a temp path to be deleted when the JVM exits or session ends
    tempFile.deleteOnDispose(true);
```

---

## Path Manipulation & Navigation

```java
    NPath base = NPath.of("/var/log/myapp");

    // Resolve: standard resolution
    NPath logFile = base.resolve("app.log");

    // ResolveChild: ignores leading slashes in the child (safer for dynamic concatenation)
    NPath safeChild = base.resolveChild("/app.log");

    // ResolveSibling: replaces the last name element
    NPath sibling = logFile.resolveSibling("error.log");

    // Normalize and Absolute
    NPath normalized = NPath.of("/a/b/../c").normalize();
    NPath absolute = NPath.of("relative.txt").toAbsolute();
```

### Advanced Navigation: relativize vs stripParent

```java
    NPath path = NPath.of("/a/b/c");

    // stripParent: Strict prefix removal. Returns empty if not a direct descendant.
    path.stripParent(NPath.of("/a"));      // Optional["b/c"]
    path.stripParent(NPath.of("/x"));      // Optional.empty()

    // relativize: Navigational. Calculates the route from origin to this path.
    path.relativize(NPath.of("/a/b"));     // Optional["c"]
    NPath.of("/a/c").relativize(NPath.of("/a/b")); // Optional["../b"]
```

### Name Parsing (nameParts)

```java
    NPath p = NPath.of("/archive/my.backup.tar.gz");

    // SHORT: splits at the last dot
    p.nameParts(NPathExtensionType.SHORT); 
    // base="my.backup.tar", ext=".gz", fullExt=".gz"

    // LONG: splits at the first dot
    p.nameParts(NPathExtensionType.LONG);  
    // base="my", ext=".backup.tar.gz", fullExt=".backup.tar.gz"

    // SMART: heuristic-based (e.g., knows about .tar.gz)
    p.nameParts(NPathExtensionType.SMART); 
    // base="my.backup", ext=".tar.gz", fullExt=".tar.gz"
```

---

## Content I/O

### Reading

```java
    byte[] data = path.readBytes();
    String text = path.readString(); // Defaults to UTF-8
    String textIso = path.readString(StandardCharsets.ISO_8859_1);

    // Streaming
    try (InputStream is = path.getInputStream()) { /* process */ }
    try (BufferedReader reader = path.getBufferedReader()) {
        reader.lines().forEach(System.out::println);
    }
```

### Writing

```java
    path.writeBytes(new byte[]{1, 2, 3});
    path.writeString("Hello World", StandardCharsets.UTF_8);

    // Write structured objects (uses Nuts formatting/serialization)
    path.writeObject(myPojo);
    path.writeText(NText.of("Formatted text"));
    path.writeMsg(NMsg.ofInfo("Operation completed"));
```

### Copying and Moving

```java
    NPath source = NPath.of("/tmp/data.txt");
    NPath target = NPath.of("/backup/data.txt");

    source.copyTo(target, NPathOption.CREATE_PARENTS);
    source.moveTo(target, NPathOption.REPLACE_EXISTING);

    // Copy from external streams
    try (InputStream is = new URL("https://example.com/file").openStream()) {
        target.copyFromInputStream(is, NPathOption.CREATE_PARENTS);
    }
```

---

## File & Directory Operations

```java
    NPath dir = NPath.of("/tmp/myapp/data");

    // Creation
    dir.mkdirs();                  // Create directory and all missing parents
    dir.mkParentDirs();            // Only create parents of the current path
    dir.ensureEmptyDirectory();    // Creates if missing, or deletes contents if exists

    // Inspection
    boolean exists = dir.exists();
    boolean isDir = dir.isDirectory();
    boolean isRemote = dir.isRemote(); // true for http://, ssh://, mem://, etc.

    // Deletion
    dir.delete();                  // Delete file or empty directory
    dir.deleteTree();              // Recursively delete directory and contents
```

---

## File Tree & Searching

```java
    NPath dir = NPath.of("/var/log");

    // Simple list
    List<NPath> files = dir.list();
    List<NPathInfo> infos = dir.listInfos(); // Includes size, type, etc.

    // Stream with filtering (Lazy evaluation)
    try (NStream<NPath> stream = dir.stream()) {
        List<NPath> txtFiles = stream.filter(p -> p.getName().endsWith(".log")).toList();
    }

    // Glob pattern matching
    dir.walkGlob("**/*.java").forEach(p -> System.out.println("Found: " + p));

    // Digest and Checksums
    List<NPathChildStringDigestInfo> digests = dir.listStringDigestInfo("SHA-256");
```

---

## Metadata & Permissions (Filesystem Level)

```java
    NPath file = NPath.of("/etc/secure/config.yml");

    // Basic File Metadata
    NPathInfo info = file.info();
    long size = file.contentLength();
    Instant modified = file.lastModifiedInstant();

    // Ownership & Permissions (POSIX)
    String owner = file.owner();
    Set<NPathPermission> perms = file.permissions();

    // Modify permissions
    file.addPermissions(NPathPermission.OWNER_READ, NPathPermission.OWNER_WRITE);
    file.removePermissions(NPathPermission.OTHERS_EXECUTE);
```

---

## Conversion & Introspection

Convert NPath back to standard Java types when interoperability is required. All return NOptional to safely handle unsupported conversions.

```java
    NPath path = NPath.of("/tmp/test.txt");

    // Safe conversions
    Optional<Path> nioPath = path.toPath().asOptional();
    Optional<File> javaFile = path.toFile().asOptional();
    Optional<URL> javaUrl = path.toURL().asOptional();

    // Introspection
    String protocol = path.protocol();       // "" for local, "https" for web, "mem" for memory
    String location = path.location();       // The raw string representation
    NPath compressed = path.toCompressedForm(); // Shortened form (e.g., using ~ for home)
```

---

## Advanced Example: Robust Remote Download with Metadata & In-Memory Staging

```java
    public void downloadArtifact(String urlStr, NId artifactId) {
        NPath remote = NPath.of(urlStr);
        
        // 1. Stage the download in memory first (zero disk I/O until verified)
        NPath memStaging = NPath.of("mem://staging/" + artifactId.getName() + ".tmp")
            .userTemporary(true); // Explicitly mark as transient
        
        // 2. Copy with automatic parent creation
        remote.copyTo(memStaging, NPathOption.CREATE_PARENTS);

        // 3. Attach rich metadata before further processing
        NContentMetadata meta = memStaging.metaData()
            .name(artifactId.getName() + ".jar")
            .contentType("application/java-archive")
            .kind("downloaded-artifact");

        // 4. Verify integrity using metadata
        if (meta.contentLength().orElse(0L) == 0) {
            throw new IOException("Downloaded payload is empty");
        }

        // 5. Move to final destination on disk atomically
        NPath finalDest = NPath.ofUserStore(new NStoreKey(artifactId, NStoreType.CACHE))
                                .resolve(artifactId.getName() + ".jar");
        finalDest.mkParentDirs();
        
        memStaging.copyTo(finalDest, NPathOption.REPLACE_EXISTING);
    }
```

---

## Summary

NPath is a unified resource locator that goes beyond simple string manipulation. By abstracting away the differences between local disks, remote servers, in-memory buffers (mem://), and virtual repositories, it allows you to write clean, portable, and resilient I/O code. Its fluent API for behavioral flags and NContentMetadata, combined with Nuts-specific features like NStoreKey and deleteOnDispose, makes it the ideal foundation for any tool requiring flexible, context-aware resource access.
