package net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;

import java.util.HashSet;
import java.util.Set;

public class DefaultNPrepareCommand extends AbstractNPrepareCommand {
    public DefaultNPrepareCommand(NSession session) {
        super(session);
    }

    private boolean isLocalhost() {
        String remoteServer = getTargetServer();
        if (NBlankable.isBlank(remoteServer) || "localhost".equalsIgnoreCase(remoteServer) || remoteServer.startsWith("127.0.0.")) {
            return true;
        }
        return false;
    }

    @Override
    public NPrepareCommand run() {
        String version = getVersion();
        getValidUser();
        String currentVersion = session.getWorkspace().getApiVersion().toString();
        if (version == null) {
            version = currentVersion;
        }
        mkdirs(remoteHomeFile("bin"));
        NId apiId = session.getWorkspace().getApiId();

        if (NBlankable.isBlank(version)) {
            apiId = apiId.builder().setVersion(version).build();
        }
        NPath javaPath = remoteJavaCommand(apiId.getVersion());
        if (javaPath == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("missing java"));
        }
        pushId(apiId, null);
        Set<NId> deps = new HashSet<>();
        deps.add(session.getWorkspace().getRuntimeId());
        deps.addAll(session.search().addId("net.thevpc.nuts.toolbox:nsh").setOptional(false).setLatest(true).setContent(true).setTargetApiVersion(apiId.getVersion()).setDependencyFilter(NDependencyFilters.of(session).byRunnable()).setBasePackage(true).setDependencies(true).getResultIds().toList());
        if(ids!=null){
            for (NId id : deps) {
                deps.addAll(session.search().addId(id).setOptional(false).setLatest(true).setContent(true).setTargetApiVersion(apiId.getVersion()).setDependencyFilter(NDependencyFilters.of(session).byRunnable()).setBasePackage(true).setDependencies(true).getResultIds().toList());
            }
        }
        for (NId dep : deps) {
            pushId(dep, apiId.getVersion());
        }
        runRemoteAsString(javaPath.toString(), "-jar", remoteIdMavenJar(apiId));
        return this;
    }

    private void pushId(NId pid, NVersion apiIdVersion) {
        NDefinition def = session.search().addId(pid).setOptional(false).setLatest(true).setContent(true).setTargetApiVersion(apiIdVersion).getResultDefinitions().required();
        NPath apiJar = def.getContent().get();
        if (!runRemoteAsStringNoFail("ls " + remoteIdMavenJar(def.getApiId()))) {
            if (!isLocalhost()) {
                String targetServer = getTargetServer();
                NExecCommand.of(getSession()).addCommand("scp")
                        .addCommand(apiJar.toString()).addCommand(getValidUser() + "@" + targetServer + ":" + remoteIdMavenJar(def.getApiId()))
                        .setFailFast(true).grabOutputString().setRedirectErrorStream(true)
                        .getOutputString();
            } else {
                NPath to = NPath.of(remoteIdMavenJar(def.getApiId()), session);
                to.getParent().mkdirs();
                apiJar.copyTo(to);
            }
        }
    }

    private String remoteIdMavenJar(NId apiId) {
        return remoteHomeFile(".m2/repository/" + String.join("/", apiId.getGroupId().split("[.]"))) + "/" + apiId.getArtifactId() + "/" + apiId.getVersion() + "/" + apiId.getArtifactId() + "-" + apiId.getVersion() + ".jar";
    }

    private NPath remoteJavaCommand(NVersion apiVersion) {
        //check JDK8 ?
        return NPath.of("java", session);
    }

    private NPath remoteNutsCommand() {
        if (version == null) {
            version = session.getWorkspace().getApiVersion().toString();
        }
        NPath e = remoteHomeFile("bin/nuts-" + version);
        if (runRemoteAsStringNoFail("ls " + e)) {
            return e;
        }
        if (runRemoteAsStringNoFail("ls /bin/nuts-" + version)) {
            return e;
        }
        return null;
    }

    private NPath remoteHomeFile(String path) {
        if (NBlankable.isBlank(targetHome)) {
            String user = getValidUser();
            return NPath.of("/home/" + user + "/" + path, session);
        } else if (targetHome.startsWith("/")) {
            return NPath.of(targetHome + "/" + path, session);
        } else {
            String user = getValidUser();
            return NPath.of("/home/" + user + "/" + targetHome + "/" + path, session);
        }
    }

    private String getValidUser() {
        String user = getUserName();
        if (NBlankable.isBlank(user)) {
            user = System.getProperty("user.name");
        }
        return user;
    }


    private boolean runRemoteAsStringNoFail(String... cmd) {
        try {
            runRemoteAsString(cmd);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void mkdirs(NPath path) {
        if (!isLocalhost()) {
            runRemoteAsString("mkdir", "-p", path.toString());
        } else {
            path.mkdirs();
        }
    }

    private String runRemoteAsString(String... cmd) {
        String remoteUser = getValidUser();
        NExecCommand e = NExecCommand.of(getSession());

        if (!isLocalhost()) {
            String targetServer = getTargetServer();
            e.addCommand("ssh", remoteUser + "@" + targetServer);
        }
        return e.addCommand(cmd).setFailFast(true).grabOutputString().setRedirectErrorStream(true).getOutputString();
    }

}
