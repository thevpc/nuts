/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndb.postgres;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.toolbox.ndb.NdbSupport;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMySqlConfigVersions;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.toolbox.ndb.util.SqlHelper;
import net.thevpc.nuts.util.NutsMaps;
import net.thevpc.nuts.util.NutsRef;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class NPostgreSQLMain implements NdbSupport {

    private NutsApplicationContext appContext;
    private NutsPath sharedConfigFolder;
    private String dbType = "postgres";

    @Override
    public void run(NutsApplicationContext appContext, NutsCommandLine commandLine) {
        this.appContext = appContext;
        sharedConfigFolder = appContext.getVersionFolder(NutsStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT)
                .resolve(dbType);
        NutsArgument a;
        commandLine.setCommandName(dbType);
        while (commandLine.hasNext()) {
            if (commandLine.withNextBoolean((value, arg, session) -> {
                addConfig(commandLine);
            }, "add")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                updateConfig(commandLine);
            }, "update")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                removeConfig(commandLine);
            }, "remove")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                runSQL(commandLine, session);
            }, "run-sql")) {
            } else if (commandLine.withNextBoolean((value, arg, session) -> {
                showTables(commandLine, session);
            }, "show-tables")) {
            } else {
                commandLine.getSession().configureLast(commandLine);
            }
        }
    }

    private void validateConfig(PostgresOptions options) {
        if (NutsBlankable.isBlank(options.name)) {
            throw new RuntimeException("missing name");
        }
    }

    private void addConfig(NutsCommandLine commandLine) {
        PostgresOptions options = new PostgresOptions();
        NutsRef<Boolean> update = NutsRef.of(false);
        while (commandLine.hasNext()) {
            if (fillOption(commandLine, options)) {
                //
            } else if (
                    commandLine.withNextBoolean((v, a, s) -> {
                        update.set(v);
                    }, "--update")
            ) {

            } else {
                commandLine.throwUnexpectedArgument();
            }
        }
        options.name = NutsStringUtils.trimToNull(options.name);
        if (NutsBlankable.isBlank(options.name)) {
            options.name = "default";
        }

        NutsPath file = sharedConfigFolder.resolve(asFullName(options.name) + NdbUtils.SERVER_CONFIG_EXT);
        NutsElements json = NutsElements.of(commandLine.getSession()).setNtf(false).json();
        if (file.exists()) {
            if (update.get()) {
                PostgresOptions old = json.parse(file, PostgresOptions.class);
                String oldName = old.name;
                old.setNonNull(options);
                old.setName(oldName);
                json.setValue(options).print(file);
            } else {
                throw new RuntimeException("already found");
            }
        } else {
            json.setValue(options).print(file);
        }
    }

    private void updateConfig(NutsCommandLine commandLine) {
        PostgresOptions options = new PostgresOptions();
        while (commandLine.hasNext()) {
            if (fillOption(commandLine, options)) {
                //
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }
        options.name = NutsStringUtils.trimToNull(options.name);
        if (NutsBlankable.isBlank(options.name)) {
            options.name = "default";
        }

        NutsPath file = sharedConfigFolder.resolve(asFullName(options.name) + NdbUtils.SERVER_CONFIG_EXT);
        if (!file.exists()) {
            throw new RuntimeException("not found");
        }
        NutsElements json = NutsElements.of(commandLine.getSession()).setNtf(false).json();
        PostgresOptions old = json.parse(file, PostgresOptions.class);
        String oldName = old.name;
        old.setNonNull(options);
        old.setName(oldName);
        json.setValue(options).print(file);
    }

    private String asFullName(String name) {
        return asFullName(new AtName(name));
    }

    private String asFullName(AtName name) {
        String cn = NdbUtils.coalesce(name.getConfigName(), "default");
        String dn = NdbUtils.coalesce(name.getDatabaseName(), "default");
        return cn + "-" + dn;
    }

    private NutsOptional<PostgresOptions> loadConfig(AtName name) {
        NutsPath file = sharedConfigFolder.resolve(asFullName(name) + NdbUtils.SERVER_CONFIG_EXT);
        if (!file.exists()) {
            return NutsOptional.ofNamedEmpty("config " + name);
        }
        NutsElements json = NutsElements.of(appContext.getSession()).setNtf(false).json();
        return NutsOptional.ofNamed(json.parse(file, PostgresOptions.class), "config " + name);
    }

    private void removeConfig(NutsCommandLine commandLine) {
        NutsRef<AtName> name = NutsRef.ofNull(AtName.class);
        NutsSession session = commandLine.getSession();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                name.set(new AtName(a.getStringValue().get(session)));
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    default: {
                        session.configureLast(commandLine);
                    }
                }
            } else {
                if (name.isNull()) {
                    name.set(new AtName(commandLine.next().get(session).asString().get(session)));
                } else {
                    commandLine.throwUnexpectedArgument();
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        removeConfig(name.get());
    }

    private void removeConfig(AtName name) {
        NutsPath file = sharedConfigFolder.resolve(asFullName(name) + NdbUtils.SERVER_CONFIG_EXT);
        if (file.exists()) {
            file.delete();
        }
    }

    private void runSQL(NutsCommandLine commandLine, NutsSession session) {
        commandLine.setCommandName(dbType + " run-sql");
        NutsRef<AtName> name = NutsRef.ofNull(AtName.class);
        List<String> sql = new ArrayList<>();
        NutsRef<Boolean> forceShowSQL = NutsRef.ofNull(Boolean.class);
        PostgresOptions otherOptions = new PostgresOptions();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                String name2 = NdbUtils.checkName(a.getStringValue().get(session), session);
                                name.set(new AtName(name2));
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"));
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

                        } else {
                            session.configureLast(commandLine);
                        }
                    }
                }
            } else {
                sql.add(commandLine.next().flatMap(NutsValue::asString).get(session));
            }
        }
        if (sql.isEmpty()) {
            commandLine.throwMissingArgument(NutsMessage.ofPlain("sql"));
        }

        PostgresOptions options = null;
        if (name.isNull()) {
            name.set(new AtName(""));
            options = loadConfig(name.get()).orNull();
            if (options == null) {
                options = new PostgresOptions();
            }
            options.setNonNull(otherOptions);
        } else {
            options = loadConfig(name.get()).get();
            options.setNonNull(otherOptions);
        }
        runSQL(sql, options, forceShowSQL.get(), session);
    }

    private void showTables(NutsCommandLine commandLine, NutsSession session) {
        commandLine.setCommandName(dbType + " show-tables");
        NutsRef<AtName> name = NutsRef.ofNull(AtName.class);
        PostgresOptions otherOptions = new PostgresOptions();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                String name2 = NdbUtils.checkName(a.getStringValue().get(session), session);
                                name.set(new AtName(name2));
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    default: {
                        if (fillOption(commandLine, otherOptions)) {

                        } else {
                            session.configureLast(commandLine);
                        }
                    }
                }
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }

        PostgresOptions options = null;
        if (name.isNull()) {
            name.set(new AtName(""));
            options = loadConfig(name.get()).orNull();
            if (options == null) {
                options = new PostgresOptions();
            }
            options.setNonNull(otherOptions);
        } else {
            options = loadConfig(name.get()).get();
            options.setNonNull(otherOptions);
        }
        runSQL(Arrays.asList("SELECT schemaname,tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'"), options, true, session);
    }

    private void runSQL(List<String> sql, AtName name, Boolean forceShowSQL, NutsSession session) {
        PostgresOptions options = loadConfig(name).get();
        runSQL(sql, options, forceShowSQL, session);
    }

    private void runSQL(List<String> sql, PostgresOptions options, Boolean forceShowSQL, NutsSession session) {
        if (options == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("missing config"));
        }
        String jdbcUrl = NutsMessage.ofVstyle("jdbc:postgresql://${server}:${port}/${database}",
                NutsMaps.of(
                        "server", NutsOptional.of(options.host).ifBlank("localhost").get(),
                        "port", NutsOptional.of(options.port).mapIf(x -> x <= 0, x -> null, x -> x).ifBlank(5432).get(),
                        "database", NutsOptional.of(options.databaseName).ifBlank("db").get()
                )).toString();
        String user = options.user;
        String password = options.password;
        if (NutsBlankable.isBlank(user) && NutsBlankable.isBlank(password)) {
            user = "postgres";
            password = "postgres";
        } else if ("postgres".equals(user) && NutsBlankable.isBlank(password)) {
            password = "postgres";
        }
        SqlHelper.runAndWaitFor(sql, jdbcUrl, "org.postgresql:postgresql#42.5.1", "org.postgresql.Driver",
                user, password, null,
                forceShowSQL, session);
    }

    private boolean fillOption(NutsCommandLine cmdLine, PostgresOptions options) {
        NutsSession session = appContext.getSession();
        NutsArgument a;
        if ((a = cmdLine.nextString("--name").orNull()) != null) {
            options.name = a.getStringValue().get(session);
            return true;
        } else if ((a = cmdLine.nextString("-h", "--host").orNull()) != null) {
            options.host = a.getStringValue().get(session);
            return true;
        } else if ((a = cmdLine.nextString("-p", "--port").orNull()) != null) {
            options.port = a.getValue().asInt().get(session);
            return true;
        } else if ((a = cmdLine.nextString("-n", "--dbname").orNull()) != null) {
            options.databaseName = a.getStringValue().get(session);
            return true;
        } else if ((a = cmdLine.nextString("-u", "--user").orNull()) != null) {
            options.user = a.getStringValue().get(session);
            return true;
        } else if ((a = cmdLine.nextString("-P", "--password").orNull()) != null) {
            options.password = a.getStringValue().get(session);
            return true;
        } else if (appContext.configureFirst(cmdLine)) {
            return true;
        } else {
            return false;
        }
    }


}
