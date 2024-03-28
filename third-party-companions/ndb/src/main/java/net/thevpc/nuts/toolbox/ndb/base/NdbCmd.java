package net.thevpc.nuts.toolbox.ndb.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;

import java.util.*;

public abstract class NdbCmd<C extends NdbConfig> {
    protected Set<String> names = new HashSet<>();
    protected NdbSupportBase<C> support;

    public NdbCmd(NdbSupportBase<C> support, String... names) {
        this.names.addAll(Arrays.asList(names));
        this.support = support;
    }

    public String getName() {
        return getNames()[0];
    }

    public NdbSupportBase<C> getSupport() {
        return support;
    }

    public String[] getNames() {
        return names.toArray(new String[0]);
    }

    abstract public void run(NSession session, NCmdLine cmdLine);

    protected boolean fillOption(NCmdLine cmdLine, C options) {
        if (support.fillOption(cmdLine, options)) {
            return true;
        } else if (fillExtraOption(cmdLine, options)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean fillExtraOption(NCmdLine cmdLine, C options) {
        return false;
    }

    protected String asFullName(String name) {
        return asFullName(new AtName(name));
    }

    protected String asFullName(AtName name) {
        String cn = NdbUtils.coalesce(name.getConfigName(), "default");
        String dn = NdbUtils.coalesce(name.getDatabaseName(), "default");
        return cn + "-" + dn;
    }

    protected NPath getSharedConfigFolder() {
        return support.getSharedConfigFolder();
    }

    protected C createConfigInstance() {
        return support.createConfigInstance();
    }

    protected Class<C> getConfigClass() {
        return support.getConfigClass();
    }

    protected void readConfigNameOption(NCmdLine commandLine, NSession session, NRef<AtName> name) {
        commandLine.withNextEntry((v, a, s) -> {
            if (name.isNull()) {
                String name2 = NdbUtils.checkName(a.getStringValue().get(session), session);
                name.set(new AtName(name2));
            } else {
                commandLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
            }
        });
    }

    protected String getDbType() {
        return support.getDbType();
    }

    protected boolean fillOptionLast(NCmdLine commandLine, C options) {
        if (fillOption(commandLine, options)) {
            return true;
        } else if (support.getSession().configureFirst(commandLine)) {
            return true;
        } else {
            commandLine.getSession().configureLast(commandLine);
            return true;
        }
    }

    protected C loadFromName(NRef<AtName> name, C otherOptions) {
        C options = null;
        if (name.isNull()) {
            name.set(new AtName(""));
            options = support.loadConfig(name.get()).orNull();
            if (options == null) {
                options = createConfigInstance();
            }
            options.setNonNull(otherOptions);
        } else {
            options = support.loadConfig(name.get()).get();
            options.setNonNull(otherOptions);
        }
        return options;
    }

    protected void revalidateOptions(C options) {
        getSupport().revalidateOptions(options);
    }

    public boolean isRemoteCommand(C options) {
        return getSupport().isRemoteCommand(options);
    }

    public NExecCmd sysSsh(C options, NSession session) {
        return getSupport().sysSsh(options, session);
    }

    protected void sshPull(NPath remote, NPath local, C options, NSession session) {
        run(sysCmd(session)
                .addCommand("scp", options.getRemoteUser() + "@" + options.getRemoteServer() + ":" + remote)
                .addCommand(local.toString())
        );
    }

    protected void sshPush(NPath local, NPath remote, C options, NSession session) {
        run(sysCmd(session)
                .addCommand("scp", local.toString())
                .addCommand(options.getRemoteUser() + "@" + options.getRemoteServer() + ":" + remote.toString())
        );
    }

    protected void sshRm(NPath upRestorePath, C options, NSession session) {
        run(sysSsh(options, session).addCommand("rm -rf " + upRestorePath.toString()));
    }

    public NExecCmd run(NExecCmd cmd) {
        return getSupport().run(cmd);
    }

    public NExecCmd sysCmd(NSession session) {
        return getSupport().sysCmd(session);
    }
}
