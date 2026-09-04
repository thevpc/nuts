package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.artifact.NClasspathEntry;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NRemoteExecutionUtils {
    private static final NLog LOG = NLog.of(NRemoteExecutionUtils.class);

    public static String ensureRemoteJava(NConnectionString connectionString, String requiredVersion) {
        NExec e = NExec.ofSystem();
        if (connectionString != null && !isLocalhost(connectionString)) {
            e.at(connectionString);
        }
        e.command("java", "-version");
        try {
            e.failFast(true).grabbedAll();
            LOG.info(NMsg.ofC("Using system 'java' on remote host: %s", connectionString));
            return "java";
        } catch (Exception ex) {
            LOG.debug(NMsg.ofC("System java check failed on remote host: %s", ex.getMessage()));
        }
        return "java";
    }

    public static List<String> transferClasspaths(NConnectionString connectionString,
                                                 List<NClasspathEntry> localCP,
                                                 String remoteCacheDir) {
        List<String> remotePaths = new ArrayList<>();
        if (localCP == null || localCP.isEmpty()) {
            return remotePaths;
        }

        for (NClasspathEntry entry : localCP) {
            NPath localPath = entry.path();
            if (localPath == null || !localPath.exists()) {
                continue;
            }
            Path pathObj = localPath.toPath().orNull();
            String fileName = pathObj != null ? pathObj.getFileName().toString() : "lib.jar";
            NId nid = entry.id();
            String uniqueName;
            if (nid != null) {
                uniqueName = nid.groupId() + "." + nid.artifactId() + "-" + nid.version() + ".jar";
            } else {
                CoreDigestHelper dh = new CoreDigestHelper();
                dh.append(localPath.toString().getBytes());
                uniqueName = dh.getDigest() + "-" + fileName;
            }

            String remotePathStr = remoteCacheDir + "/" + uniqueName;
            NPath to = (connectionString != null && !isLocalhost(connectionString))
                    ? NPath.of(connectionString.withPath(remotePathStr))
                    : NPath.of(remotePathStr);

            try {
                if (!to.exists()) {
                    LOG.info(NMsg.ofC("Transferring dependency JAR to remote cache: %s -> %s", localPath, remotePathStr));
                    to.mkParentDirs();
                    localPath.copyTo(to);
                } else {
                    LOG.debug(NMsg.ofC("Remote JAR cache hit: %s", remotePathStr));
                }
            } catch (Exception ex) {
                LOG.error(NMsg.ofC("Failed to transfer JAR %s to remote target: %s", localPath, ex.getMessage()));
            }
            remotePaths.add(remotePathStr);
        }
        return remotePaths;
    }

    private static boolean isLocalhost(NConnectionString c) {
        if (NBlankable.isBlank(c) || NBlankable.isBlank(c.host())) {
            return true;
        }
        String host = c.host();
        return "localhost".equalsIgnoreCase(host) || host.startsWith("127.0.0.");
    }
}
