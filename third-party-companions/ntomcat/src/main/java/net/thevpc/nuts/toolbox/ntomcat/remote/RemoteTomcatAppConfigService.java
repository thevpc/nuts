package net.thevpc.nuts.toolbox.ntomcat.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ntomcat.remote.config.RemoteTomcatAppConfig;
import net.thevpc.nuts.toolbox.ntomcat.remote.config.RemoteTomcatConfig;
import net.thevpc.nuts.toolbox.ntomcat.util._FileUtils;
import net.thevpc.nuts.util.NStringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RemoteTomcatAppConfigService extends RemoteTomcatServiceBase {

    private RemoteTomcatAppConfig config;
    private NSession session;
    private RemoteTomcatConfigService client;
    private String name;

    public RemoteTomcatAppConfigService(String name, RemoteTomcatAppConfig config, RemoteTomcatConfigService client) {
        this.config = config;
        this.client = client;
        this.session = client.session;
        this.name = name;
    }

    public void install() {
        RemoteTomcatConfig cconfig = client.getConfig();
        String localWarPath = this.config.getPath();
        if (!new File(localWarPath).exists()) {
            throw new NExecutionException(session, NMsg.ofC("missing source war file %s", localWarPath), NExecutionException.ERROR_2);
        }
        String remoteTempPath = cconfig.getRemoteTempPath();
        if (NBlankable.isBlank(remoteTempPath)) {
            remoteTempPath = "/tmp";
        }
        String remoteFilePath = ("/" + remoteTempPath + "/" + _FileUtils.getFileName(localWarPath));
        String server = cconfig.getServer();
        if (NBlankable.isBlank(server)) {
            server = "localhost";
        }
        if (!server.startsWith("ssh://")) {
            server = "ssh://" + server;
        }
        NExecCommand.of(session)
                .addCommand(
                        "nsh",
                        "--bot",
                        "cp",
                        "--verbose",
                        "--mkdir",
                        localWarPath,
                        server + "/" + remoteFilePath
                ).setSession(session)
                .run();
        String v = config.getVersionCommand();
        if (NBlankable.isBlank(v)) {
            v = "nsh nversion --color=never %file";
        }
        List<String> cmd = NCmdLine.parseDefault(v).get(session).toStringList();
        boolean fileAdded = false;
        for (int i = 0; i < cmd.size(); i++) {
            if ("%file".equals(cmd.get(i))) {
                cmd.set(i, config.getPath());
                fileAdded = true;
            }
        }
        if (!fileAdded) {
            cmd.add(config.getPath());
        }
        NExecCommand s = NExecCommand.of(session)
                .redirectErrorStream()
                .grabOutputString()
                .addCommand(cmd).run();
        if (s.getResult() == 0) {
            client.execRemoteNuts(
                    "net.thevpc.nuts.toolbox:tomcat",
                    "install",
                    "--name",
                    client.getRemoteInstanceName(),
                    "--app",
                    name,
                    "--version",
                    s.getOutputString().trim(),
                    "--file",
                    remoteFilePath
            );
            client.execRemoteNuts(
                    "nsh",
                    "--bot",
                    "rm",
                    remoteFilePath
            );
        } else {
            throw new NExecutionException(session, NMsg.ofC("unable to detect file version of %s.\n%s",localWarPath ,
                    s.getOutputString()), NExecutionException.ERROR_2);
        }
    }

    public void deploy(String version) {
        client.execRemoteNuts(
                "net.thevpc.nuts.toolbox:tomcat",
                "deploy",
                "--name",
                client.getRemoteInstanceName(),
                "--app",
                name,
                "--version",
                NStringUtils.trim(version)
        );
    }

    public RemoteTomcatAppConfig getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    public RemoteTomcatAppConfigService print(NPrintStream out) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("config-name", getName());
        m.putAll(NElements.of(session).convert(getConfig(), Map.class));
        NObjectFormat.of(session).setValue(m).print(out);
        return this;
    }

    public RemoteTomcatAppConfigService remove() {
        client.getConfig().getApps().remove(name);
        session.out().println(NMsg.ofC("%s app removed.", getBracketsPrefix(name)));
        return this;

    }

    public NString getBracketsPrefix(String str) {
        return NTexts.of(session).ofBuilder()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }

    public RemoteTomcatConfigService getTomcat() {
        return client;
    }
}
