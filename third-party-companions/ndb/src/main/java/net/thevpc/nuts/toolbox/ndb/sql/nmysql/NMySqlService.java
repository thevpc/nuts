package net.thevpc.nuts.toolbox.ndb.sql.nmysql;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.LocalMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.config.LocalMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.RemoteMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.config.RemoteMysqlConfig;
import net.thevpc.nuts.util.NUnsafeFunction;

import java.util.logging.Logger;

public class NMySqlService {
    private static final Logger LOG = Logger.getLogger(NMySqlService.class.getName());
    private NApplicationContext context;
    private NPath sharedConfigFolder;

    public NMySqlService(NApplicationContext context) {
        this.context = context;
        sharedConfigFolder = context.getVersionFolder(NStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT);
    }

    public LocalMysqlConfigService[] listLocalConfig() {
        return
                sharedConfigFolder.stream().filter(
                                pathname -> pathname.isRegularFile() && pathname.getName().toString().endsWith(LocalMysqlConfigService.SERVER_CONFIG_EXT),
                                "isRegularFile() && matches(*" + LocalMysqlConfigService.SERVER_CONFIG_EXT + ")"
                        )
                        .mapUnsafe(
                                NUnsafeFunction.of(this::loadLocalMysqlConfig, "loadLocalMysqlConfig")
                                , null)
                        .filterNonNull()
                        .toArray(LocalMysqlConfigService[]::new);
    }

    public LocalMysqlConfigService loadLocalMysqlConfig(String name, NOpenMode action) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(name, context);
        if (t.existsConfig()) {
            if (action == NOpenMode.CREATE_OR_ERROR) {
                throw new NIllegalArgumentException(context.getSession(), NMsg.ofCstyle("local mysql config already exist: %s", name));
            }
            t.loadConfig();
        } else {
            switch (action) {
                case OPEN_OR_CREATE: {
                    t.setConfig(new LocalMysqlConfig());
                    break;
                }
                case OPEN_OR_ERROR: {
                    throw new NIllegalArgumentException(context.getSession(), NMsg.ofCstyle("no such local mysql config: %s", name));
                }
                case OPEN_OR_NULL: {
                    t = null;
                    break;
                }
            }
        }
        return t;
    }

    public RemoteMysqlConfigService loadRemoteMysqlConfig(String name, NOpenMode action) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, context);
        if (t.existsConfig()) {
            if (action == NOpenMode.CREATE_OR_ERROR) {
                throw new NIllegalArgumentException(context.getSession(), NMsg.ofCstyle("remote mysql config already exist: %s", name));
            }
            t.loadConfig();
        } else {
            switch (action) {
                case OPEN_OR_CREATE: {
                    t.setConfig(new RemoteMysqlConfig());
                    break;
                }
                case OPEN_OR_ERROR: {
                    throw new NIllegalArgumentException(context.getSession(), NMsg.ofCstyle("no such remote mysql config: %s", name));
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

    public LocalMysqlConfigService loadLocalMysqlConfig(NPath file) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(file, context);
        t.loadConfig();
        return t;
    }

    public NApplicationContext getContext() {
        return context;
    }

    public RemoteMysqlConfigService loadRemoteMysqlConfig(String name) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, context);
        t.loadConfig();
        return t;
    }

    public RemoteMysqlConfigService[] listRemoteConfig() {
        return
                sharedConfigFolder.stream().filter(
                                pathname -> pathname.isRegularFile() && pathname.getName().toString().endsWith(LocalMysqlConfigService.SERVER_CONFIG_EXT),
                                "isRegularFile() && matches(*" + LocalMysqlConfigService.SERVER_CONFIG_EXT + ")"
                        )
                        .mapUnsafe(
                                NUnsafeFunction.of(
                                        x -> {
                                            String nn = x.getName();
                                            return loadRemoteMysqlConfig(nn.substring(0, nn.length() - RemoteMysqlConfigService.CLIENT_CONFIG_EXT.length()));
                                        }, "loadRemoteMysqlConfig")
                                , null)
                        .filterNonNull()
                        .toArray(RemoteMysqlConfigService[]::new);
    }
}
