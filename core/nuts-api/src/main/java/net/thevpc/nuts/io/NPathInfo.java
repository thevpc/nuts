package net.thevpc.nuts.io;

import java.time.Instant;
import java.util.Set;

public interface NPathInfo {
    String getPath();                // original path
    NPathType getType();       // What the entry itself is
    NPathType getTargetType(); // What the resolved target is (or same if not a link)
    String getTargetPath();        // Raw resolved path string, null if not a link
    long getContentLength();
    boolean isSymbolicLink();    // true if path is a symlink
    Instant getLastModifiedInstant();
    Instant getCreationInstant();
    Set<NPathPermission> getPermissions();
    String getOwner();
    String getGroup();
    Instant getLastAccessInstant();
}
