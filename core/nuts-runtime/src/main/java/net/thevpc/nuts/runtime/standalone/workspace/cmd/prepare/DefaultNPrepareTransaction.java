package net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.text.NDescriptorWriter;
import net.thevpc.nuts.util.NBlankable;

class DefaultNPrepareTransaction implements AutoCloseable{
    public NEnv remoteEnv;
    public NConnectionString connectionString = null;
    public NVersion apiVersion = null;
    public NVersion rtVersion = null;
    public NVersion appVersion = null;
    public String remoteUserHome = null;
    public String remoteWorkspace = null;
    public String companionRepository = null;
    public String remotePrivateJdk = null;
    public String remoteJava = null;
    public boolean localHost;

    public DefaultNPrepareTransaction(NVersion apiVersion, NConnectionString connectionString) {
        this.apiVersion = apiVersion;
        this.connectionString = connectionString;
        this.localHost = isLocalhost();
    }

    private boolean isLocalhost() {
        NConnectionString c = connectionString;
        if (NBlankable.isBlank(c)) {
            return true;
        }
        if (NBlankable.isBlank(c.host())) {
            return true;
        }
        String remoteServer = c.host();
        return NBlankable.isBlank(remoteServer) || "localhost".equalsIgnoreCase(remoteServer) || remoteServer.startsWith("127.0.0.");
    }


    public NEnv remoteEnv() {
        if (remoteEnv == null) {
            if (localHost) {
                remoteEnv = NEnv.of();
            } else {
                remoteEnv = NEnv.of(connectionString);
            }
        }
        return remoteEnv;
    }



    public NPath companionJar(NId id) {
        String pathStr = NPath.ofMavenLayout(id, ".jar").toAbsolute(companionRepository).toString();
        if (connectionString != null) {
            return NPath.of(connectionString.withPath(pathStr));
        } else {
            return NPath.of(pathStr);
        }
    }

    public void pushId(NId pid) {
        NDefinition def = NSearch.of().addId(pid).latest(true).getResultDefinitions().findFirst().orElse(null);
        if (def == null) {
            def = NSearch.of().addId(pid.builder().version((String) null).build()).latest(true).getResultDefinitions().findFirst().orElse(null);
        }
        if (def == null) {
            return;
        }
        NPath apiJar = def.content().orNull();
        NId targetId = def.id() != null ? def.id() : pid;
        NPath to;
        if (apiJar != null) {
            String jarPathStr = NPath.ofMavenLayout(targetId, ".jar").toAbsolute(companionRepository).toString();
            to = connectionString != null ? NPath.of(connectionString.withPath(jarPathStr)) : NPath.of(jarPathStr);
            if (!to.exists()) {
                to.mkParentDirs();
                try {
                    apiJar.copyTo(to);
                } catch (Exception ex) {
                    if (connectionString != null && !localHost) {
                        runRemoteAsString("mkdir", "-p", NPath.of(jarPathStr).parent().toString());
                        String userHost = (connectionString.userName() != null ? connectionString.userName() + "@" : "") + connectionString.host();
                        NExec.of().command("scp", "-o", "StrictHostKeyChecking=no", apiJar.toString(), userHost + ":" + jarPathStr)
                                .failFast(true).grabbedAll();
                    } else {
                        throw ex;
                    }
                }
            }
        }
        String nutsPathStr = NPath.ofMavenLayout(targetId, ".nuts").toAbsolute(companionRepository).toString();
        to = connectionString != null ? NPath.of(connectionString.withPath(nutsPathStr)) : NPath.of(nutsPathStr);
        if (!to.exists()) {
            to.mkParentDirs();
            to.writeString(NDescriptorWriter.of().formatPlain(def.descriptor()));
        }
    }

    public String runRemoteAsString(String... cmd) {
        NExec e = NExec.of();
        if (!isLocalhost()) {
            e.at(connectionString);
        }
        e.command(cmd);
        return e.failFast(true).grabbedAll();
    }

    public boolean runRemoteAsStringNoFail(String... cmd) {
        try {
            runRemoteAsString(cmd);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    @Override
    public void close() {
        if(companionRepository!=null){
            NPath.of(connectionString.withPath(companionRepository));
        }
    }
}
