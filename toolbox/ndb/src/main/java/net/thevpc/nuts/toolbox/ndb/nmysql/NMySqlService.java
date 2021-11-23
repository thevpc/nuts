package net.thevpc.nuts.toolbox.ndb.nmysql;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.LocalMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.config.LocalMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.RemoteMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.config.RemoteMysqlConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NMySqlService {
    private static final Logger LOG = Logger.getLogger(NMySqlService.class.getName());
    private NutsApplicationContext context;
    private NutsPath sharedConfigFolder;

    public NMySqlService(NutsApplicationContext context) {
        this.context = context;
        sharedConfigFolder = context.getVersionFolder(NutsStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT);
    }

    public LocalMysqlConfigService[] listLocalConfig() {
        return
                sharedConfigFolder.list().filter(
                                pathname -> pathname.isRegularFile() && pathname.getName().toString().endsWith(LocalMysqlConfigService.SERVER_CONFIG_EXT),
                                "isRegularFile() && matches(*" + LocalMysqlConfigService.SERVER_CONFIG_EXT + ")"
                        )
                        .mapUnsafe(
                                NutsUnsafeFunction.of(this::loadLocalMysqlConfig, "loadLocalMysqlConfig")
                                , null)
                        .filterNonNull()
                        .toArray(LocalMysqlConfigService[]::new);
    }

    public LocalMysqlConfigService loadLocalMysqlConfig(String name, NutsOpenMode action) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(name, context);
        if (t.existsConfig()) {
            if (action == NutsOpenMode.CREATE_OR_ERROR) {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("local mysql config already exist: %s", name));
            }
            t.loadConfig();
        } else {
            switch (action) {
                case OPEN_OR_CREATE: {
                    t.setConfig(new LocalMysqlConfig());
                    break;
                }
                case OPEN_OR_ERROR: {
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("no such local mysql config: %s", name));
                }
                case OPEN_OR_NULL: {
                    t = null;
                    break;
                }
            }
        }
        return t;
    }

    public RemoteMysqlConfigService loadRemoteMysqlConfig(String name, NutsOpenMode action) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, context);
        if (t.existsConfig()) {
            if (action == NutsOpenMode.CREATE_OR_ERROR) {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("remote mysql config already exist: %s", name));
            }
            t.loadConfig();
        } else {
            switch (action) {
                case OPEN_OR_CREATE: {
                    t.setConfig(new RemoteMysqlConfig());
                    break;
                }
                case OPEN_OR_ERROR: {
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("no such remote mysql config: %s", name));
                }
                case OPEN_OR_NULL: {
                    t = null;
                    break;
                }
                case CREATE_OR_ERROR: {
                    t = null;
                    break;
                }
            }
        }
        return t;
    }

    public LocalMysqlConfigService loadLocalMysqlConfig(NutsPath file) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(file, context);
        t.loadConfig();
        return t;
    }

    public NutsApplicationContext getContext() {
        return context;
    }

    public RemoteMysqlConfigService loadRemoteMysqlConfig(String name) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, context);
        t.loadConfig();
        return t;
    }

    public RemoteMysqlConfigService[] listRemoteConfig() {
        return
                sharedConfigFolder.list().filter(
                                pathname -> pathname.isRegularFile() && pathname.getName().toString().endsWith(LocalMysqlConfigService.SERVER_CONFIG_EXT),
                                "isRegularFile() && matches(*" + LocalMysqlConfigService.SERVER_CONFIG_EXT + ")"
                        )
                        .mapUnsafe(
                                NutsUnsafeFunction.of(
                                        x -> {
                                            String nn = x.getName();
                                            return loadRemoteMysqlConfig(nn.substring(0, nn.length() - RemoteMysqlConfigService.CLIENT_CONFIG_EXT.length()));
                                        }, "loadRemoteMysqlConfig")
                                , null)
                        .filterNonNull()
                        .toArray(RemoteMysqlConfigService[]::new);
    }
}
