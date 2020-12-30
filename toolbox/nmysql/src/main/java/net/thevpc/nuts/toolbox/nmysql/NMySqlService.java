package net.thevpc.nuts.toolbox.nmysql;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsOpenMode;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.toolbox.nmysql.local.LocalMysqlConfigService;
import net.thevpc.nuts.toolbox.nmysql.local.config.LocalMysqlConfig;
import net.thevpc.nuts.toolbox.nmysql.remote.RemoteMysqlConfigService;
import net.thevpc.nuts.toolbox.nmysql.remote.config.RemoteMysqlConfig;

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
    private Path sharedConfigFolder;

    public NMySqlService(NutsApplicationContext context) {
        this.context=context;
        sharedConfigFolder = Paths.get(context.getVersionFolderFolder(NutsStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT));
    }

    public LocalMysqlConfigService[] listLocalConfig() {
        List<LocalMysqlConfigService> all = new ArrayList<>();
        if (Files.isDirectory(sharedConfigFolder)) {
            try (DirectoryStream<Path> configFiles = Files.newDirectoryStream(sharedConfigFolder, pathname -> pathname.getFileName().toString().endsWith(LocalMysqlConfigService.SERVER_CONFIG_EXT))) {
                for (Path file1 : configFiles) {
                    try {
                        LocalMysqlConfigService c = loadLocalMysqlConfig(file1);
                        all.add(c);
                    } catch (Exception ex) {
                        LOG.log(Level.FINE, "Error loading config url : " + file1, ex);//e.printStackTrace();
                        //ignore
                    }
                }

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return all.toArray(new LocalMysqlConfigService[0]);
    }



    public LocalMysqlConfigService loadLocalMysqlConfig(String name, NutsOpenMode action) {
        LocalMysqlConfigService t = new LocalMysqlConfigService(name, context);
        if (t.existsConfig()) {
            if(action==NutsOpenMode.CREATE_OR_ERROR){
                throw new NutsIllegalArgumentException(context.getWorkspace(),"local mysql config already exist: " + name);
            }
            t.loadConfig();
        } else {
            switch (action){
                case OPEN_OR_CREATE:{
                    t.setConfig(new LocalMysqlConfig());
                    break;
                }
                case OPEN_OR_ERROR:{
                    throw new NutsIllegalArgumentException(context.getWorkspace(),"no such local mysql config: " + name);
                }
                case OPEN_OR_NULL:{
                    t=null;
                    break;
                }
            }
        }
        return t;
    }

    public RemoteMysqlConfigService loadRemoteMysqlConfig(String name, NutsOpenMode action) {
        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, context);
        if (t.existsConfig()) {
            if(action==NutsOpenMode.CREATE_OR_ERROR){
                throw new NutsIllegalArgumentException(context.getWorkspace(),"remote mysql config already exist: " + name);
            }
            t.loadConfig();
        } else {
            switch (action){
                case OPEN_OR_CREATE:{
                    t.setConfig(new RemoteMysqlConfig());
                     break;
                }
                case OPEN_OR_ERROR:{
                    throw new NutsIllegalArgumentException(context.getWorkspace(),"no such remote mysql config: " + name);
                }
                case OPEN_OR_NULL:{
                    t=null;
                    break;
                }
                case CREATE_OR_ERROR:{
                    t=null;
                    break;
                }
            }
        }
        return t;
    }

    public LocalMysqlConfigService loadLocalMysqlConfig(Path file) {
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
        List<RemoteMysqlConfigService> all = new ArrayList<>();
        if(Files.isDirectory(sharedConfigFolder)) {
            try (DirectoryStream<Path> configFiles = Files.newDirectoryStream(sharedConfigFolder, x -> x.getFileName().toString().endsWith(RemoteMysqlConfigService.CLIENT_CONFIG_EXT))) {
                for (Path file1 : configFiles) {
                    try {
                        String nn = file1.getFileName().toString();
                        RemoteMysqlConfigService c = loadRemoteMysqlConfig(nn.substring(0, nn.length() - RemoteMysqlConfigService.CLIENT_CONFIG_EXT.length()));
                        all.add(c);
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return all.toArray(new RemoteMysqlConfigService[0]);
    }
}
