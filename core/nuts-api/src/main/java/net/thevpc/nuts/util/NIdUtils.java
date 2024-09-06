package net.thevpc.nuts.util;

import net.thevpc.nuts.NId;

public final class NIdUtils {
    private NIdUtils() {
    }

    public static String resolveGroupIdPath(String groupId) {
        return groupId.replace('.', '/');
    }

    public static String resolveIdPath(NId id) {
        StringBuilder sb = new StringBuilder();
        sb.append(resolveGroupIdPath(id.getGroupId()));
        if (!NBlankable.isBlank(id.getArtifactId())) {
            sb.append("/");
            sb.append(id.getArtifactId());
            if (!NBlankable.isBlank(id.getVersion())) {
                sb.append("/");
                sb.append(id.getVersion());
            }
        }
        return sb.toString();
    }

    public static String resolveJarPath(NId id) {
        return resolveFilePath(id, "jar");
    }
    public static String resolveDescPath(NId id) {
        return resolveFilePath(id, "nuts");
    }

    public static String resolveNutsDescriptorPath(NId id) {
        return resolveFilePath(id, "nuts");
    }

    public static String resolveFileName(NId id, String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.getArtifactId());
        if (!id.getVersion().isBlank()) {
            sb.append("-").append(id.getVersion());
        }
        if (!NBlankable.isBlank(extension)) {
            sb.append(".").append(extension);
        }
        return sb.toString();
    }

    public static String resolveFilePath(NId id, String extension) {
        String fileName = resolveFileName(id, extension);
        return resolveIdPath(id) + '/' + fileName;
    }
}
