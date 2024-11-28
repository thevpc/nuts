package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NIdBoot;

public final class NIdUtilsBoot {
    private NIdUtilsBoot() {
    }

    public static String resolveGroupIdPath(String groupId) {
        return groupId.replace('.', '/');
    }

    public static String resolveIdPath(NIdBoot id) {
        StringBuilder sb = new StringBuilder();
        sb.append(resolveGroupIdPath(id.getGroupId()));
        if (!NStringUtilsBoot.isBlank(id.getArtifactId())) {
            sb.append("/");
            sb.append(id.getArtifactId());
            if (!NStringUtilsBoot.isBlank(id.getVersion())) {
                sb.append("/");
                sb.append(id.getVersion());
            }
        }
        return sb.toString();
    }

    public static String resolveJarPath(NIdBoot id) {
        return resolveFilePath(id, "jar");
    }
    public static String resolveDescPath(NIdBoot id) {
        return resolveFilePath(id, "nuts");
    }

    public static String resolveNutsDescriptorPath(NIdBoot id) {
        return resolveFilePath(id, "nuts");
    }

    public static String resolveFileName(NIdBoot id, String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.getArtifactId());
        if (!NStringUtilsBoot.isBlank(id.getVersion())) {
            sb.append("-").append(id.getVersion());
        }
        if (!NStringUtilsBoot.isBlank(extension)) {
            sb.append(".").append(extension);
        }
        return sb.toString();
    }

    public static String resolveFilePath(NIdBoot id, String extension) {
        String fileName = resolveFileName(id, extension);
        return resolveIdPath(id) + '/' + fileName;
    }
}
