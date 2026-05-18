package net.thevpc.nuts.io;

import java.time.Instant;
import java.util.Set;

public interface NPathInfo {
    String name();                // original name
    String path();                // original path
    NPathType type();       // What the entry itself is
    NPathType targetType(); // What the resolved target is (or same if not a link)
    String targetPath();        // Raw resolved path string, null if not a link
    long contentLength();
    boolean isSymbolicLink();    // true if path is a symlink
    Instant lastModifiedInstant();
    Instant creationInstant();
    Set<NPathPermission> permissions();
    String owner();
    String group();
    Instant lastAccessInstant();
}
