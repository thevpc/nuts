package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

/**
 * NPathInfo interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NPathInfo {

    /**
     * Creates a new instance of not found.
     *
     * @param path path
     * @return of not found result
     */
    static NPathInfo ofNotFound(String path) {
        return NIORPI.of().createPathInfoNotFound(path);
    }

    /**
     * Default n path info.
     *
     * @param name name
     * @param path path
     * @param type type
     * @param targetType target type
     * @param targetPath target path
     * @param size size
     * @param symbolicLink symbolic link
     * @param lastModified last modified
     * @param lastAccess last access
     * @param creationTime creation time
     * @param permissions permissions
     * @param owner owner
     * @param group group
     * @return default n path info result
     */
    static NPathInfo of(String name, String path, NPathType type, NPathType targetType, String targetPath, long size, boolean symbolicLink, Instant lastModified, Instant lastAccess, Instant creationTime, Set<NPathPermission> permissions, String owner, String group){
        return NIORPI.of().createPathInfo(name,path,targetType, targetType,targetPath,size,symbolicLink,lastModified,lastAccess, creationTime, permissions,owner,group);
    }


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
