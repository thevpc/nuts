package net.thevpc.nuts.toolbox.ndb;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.toolbox.ndb.util.SqlHelper;
import net.thevpc.nuts.util.NRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class RdbSupport<C extends NdbConfig> extends NdbSupportBase<C> {
    protected String dbDriverPackage;
    protected String dbDriverClass;

    public RdbSupport(String dbType, Class<C> configClass, NApplicationContext appContext, String dbDriverPackage, String dbDriverClass) {
        super(dbType, configClass, appContext);
        this.dbDriverPackage = dbDriverPackage;
        this.dbDriverClass = dbDriverClass;
    }

    @Override
    protected boolean runExtraCommand(NApplicationContext appContext, NCommandLine commandLine) {
        if (super.runExtraCommand(appContext, commandLine)) {
            return true;
        } else if (commandLine.withNextBoolean((value, arg, session) -> {
            runSQL(commandLine, session);
        }, "run-sql")) {
            return true;
        }else{
            return false;
        }
    }

    protected void runSQL(NCommandLine commandLine, NSession session) {
        commandLine.setCommandName(dbType + " run-sql");
        NRef<AtName> name = NRef.ofNull(AtName.class);
        List<String> sql = new ArrayList<>();
        NRef<Boolean> forceShowSQL = NRef.ofNull(Boolean.class);
        C otherOptions = createConfigInstance();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                String name2 = NdbUtils.checkName(a.getStringValue().get(session), session);
                                name.set(new AtName(name2));
                            } else {
                                commandLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--show-sql": {
                        commandLine.withNextBoolean((v, a, s) -> {
                            forceShowSQL.set(v);
                        });
                        break;
                    }
                    default: {
                        if (fillOption(commandLine, otherOptions)) {

                        } else if (appContext.configureFirst(commandLine)) {

                        } else {
                            session.configureLast(commandLine);
                        }
                    }
                }
            } else {
                sql.add(commandLine.next().flatMap(NValue::asString).get(session));
            }
        }
        if (sql.isEmpty()) {
            commandLine.throwMissingArgument(NMsg.ofPlain("sql"));
        }

        C options = null;
        if (name.isNull()) {
            name.set(new AtName(""));
            options = loadConfig(name.get()).orNull();
            if (options == null) {
                options = createConfigInstance();
            }
            options.setNonNull(otherOptions);
        } else {
            options = loadConfig(name.get()).get();
            options.setNonNull(otherOptions);
        }
        runSQL(sql, options, forceShowSQL.get(), session);
    }

    protected void runSQL(List<String> sql, AtName name, Boolean forceShowSQL, NSession session) {
        C options = loadConfig(name).get();
        runSQL(sql, options, forceShowSQL, session);
    }

    protected void runSQL(List<String> sql, C options, Boolean forceShowSQL, NSession session) {
        if (options == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("missing config"));
        }
        revalidateOptions(options);
        if (isRemoteCommand(options)) {
            //call self remotely
            NPrepareCommand.of(session)
                    .setUserName(options.getRemoteUser())
                    .setTargetServer(options.getRemoteServer())
                    .addIds(Arrays.asList(NId.of(dbDriverPackage).get()))
                    .run();
            run(sysSsh(options, session)
                    .addCommand("nuts")
                    .addCommand(appContext.getAppId().toString())
                    .addCommand("postgresql")
                    .addCommand("run-sql")
                    .addCommand("--host=" + options.getHost())
                    .addCommand("--port=" + options.getPort())
                    .addCommand("--dbname=" + options.getDatabaseName())
                    .addCommand("--user=" + options.getUser())
                    .addCommand("--password=" + options.getPassword())
            );
            return;
        }
        String jdbcUrl =createJdbcURL(options);
        SqlHelper.runAndWaitFor(sql, jdbcUrl, dbDriverPackage, dbDriverClass,
                options.getUser(), options.getPassword(), null,
                forceShowSQL, session);
    }

    protected abstract String createJdbcURL(C options);

    protected abstract void revalidateOptions(C options) ;

}
