package net.thevpc.nuts.io;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class DefaultNPathInfo implements NPathInfo {
    private String path;                // original path
    private NPathType type;       // What the entry itself is
    private NPathType targetType; // What the resolved target is (or same if not a link)
    private String targetPath;        // Raw resolved path string, null if not a link
    private long size;
    private boolean symbolicLink;    // true if path is a symlink
    private Instant lastModified;
    private Instant creationTime;
    private Instant lastAccess;
    private Set<NPathPermission> permissions;
    private String owner;
    private String group;

    public static DefaultNPathInfo ofNotFound(String path) {
        return new DefaultNPathInfo(path,NPathType.NOT_FOUND,null,null,-1,false,null,null, null,Collections.emptySet(),null,null);
    }

    public DefaultNPathInfo(String path, NPathType type, NPathType targetType, String targetPath, long size, boolean symbolicLink, Instant lastModified, Instant lastAccess, Instant creationTime, Set<NPathPermission> permissions, String owner, String group) {
        this.path = path;
        this.type = type;
        this.targetType = targetType;
        this.targetPath = targetPath;
        this.size = size;
        this.symbolicLink = symbolicLink;
        this.lastModified = lastModified;
        this.creationTime = creationTime;
        this.lastAccess = lastAccess;
        this.permissions = permissions;
        this.owner = owner;
        this.group = group;
    }

    public Instant getLastAccessInstant() {
        return lastAccess;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public NPathType getType() {
        return type;
    }

    @Override
    public NPathType getTargetType() {
        return targetType;
    }

    @Override
    public String getTargetPath() {
        return targetPath;
    }

    @Override
    public long getContentLength() {
        return size;
    }

    @Override
    public boolean isSymbolicLink() {
        return this.symbolicLink;
    }

    @Override
    public Instant getLastModifiedInstant() {
        return lastModified;
    }

    @Override
    public Instant getCreationInstant() {
        return creationTime;
    }

    @Override
    public Set<NPathPermission> getPermissions() {
        return permissions;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNPathInfo that = (DefaultNPathInfo) o;
        return size == that.size && symbolicLink == that.symbolicLink && Objects.equals(path, that.path) && type == that.type && targetType == that.targetType && Objects.equals(targetPath, that.targetPath) && Objects.equals(lastModified, that.lastModified) && Objects.equals(creationTime, that.creationTime) && Objects.equals(permissions, that.permissions) && Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, type, targetType, targetPath, size, symbolicLink, lastModified, creationTime, permissions, owner);
    }

    @Override
    public String toString() {
        return "DefaultNPathInfo{" +
                "path='" + path + '\'' +
                ", type=" + type +
                ", targetType=" + targetType +
                ", targetPath='" + targetPath + '\'' +
                ", size=" + size +
                ", symbolicLink=" + symbolicLink +
                ", lastModified=" + lastModified +
                ", creationTime=" + creationTime +
                ", permissions=" + permissions +
                ", owner='" + owner + '\'' +
                '}';
    }
}
