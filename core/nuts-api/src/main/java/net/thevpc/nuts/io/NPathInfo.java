package net.thevpc.nuts.io;

import java.time.Instant;
import java.util.Set;

/**
 * NPathInfo interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NPathInfo {
    /**
     * Name.
     *
     * @return name result
     */
    String name();                // original name
    /**
     * Path.
     *
     * @return path result
     */
    String path();                // original path
    /**
     * Type.
     *
     * @return type result
     */
    NPathType type();       // What the entry itself is
    /**
     * Target type.
     *
     * @return target type result
     */
    NPathType targetType(); // What the resolved target is (or same if not a link)
    /**
     * Target path.
     *
     * @return target path result
     */
    String targetPath();        // Raw resolved path string, null if not a link
    /**
     * Content length.
     *
     * @return content length result
     */
    long contentLength();
    /**
     * Checks if is symbolic link.
     *
     * @return is symbolic link result
     */
    boolean isSymbolicLink();    // true if path is a symlink
    /**
     * Last modified instant.
     *
     * @return last modified instant result
     */
    Instant lastModifiedInstant();
    /**
     * Creation instant.
     *
     * @return creation instant result
     */
    Instant creationInstant();
    /**
     * Permissions.
     *
     * @return permissions result
     */
    Set<NPathPermission> permissions();
    /**
     * Owner.
     *
     * @return owner result
     */
    String owner();
    /**
     * Group.
     *
     * @return group result
     */
    String group();
    /**
     * Last access instant.
     *
     * @return last access instant result
     */
    Instant lastAccessInstant();
}
