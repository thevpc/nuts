package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultCustomCommandManager implements NutsCustomCommandManager {

    public DefaultCustomCommandsModel model;
    public NutsSession session;

    public DefaultCustomCommandManager(DefaultCustomCommandsModel model) {
        this.model = model;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsCommandFactoryConfig[] getCommandFactories() {
        checkSession();
        return model.getFactories(session);
    }

    @Override
    public void addCommandFactory(NutsCommandFactoryConfig commandFactoryConfig) {
        checkSession();
        model.addFactory(commandFactoryConfig, session);
    }

    @Override
    public void removeCommandFactory(String commandFactoryId) {
        checkSession();
        model.removeFactory(commandFactoryId, session);
    }

    @Override
    public boolean removeCommandFactoryIfExists(String commandFactoryId) {
        checkSession();
        return model.removeFactoryIfExists(commandFactoryId, session);
    }

    @Override
    public boolean commandExists(String command) {
        checkSession();
        return findCommand(command) != null;
    }

    @Override
    public boolean commandFactoryExists(String factoryId) {
        checkSession();
        return model.commandFactoryExists(factoryId, session);
    }

    @Override
    public boolean addCommand(NutsCommandConfig command) {
        checkSession();
        return model.add(command, session);
    }

    @Override
    public boolean updateCommand(NutsCommandConfig command) {
        checkSession();
        return model.update(command, session);
    }

    @Override
    public void removeCommand(String command) {
        checkSession();
        model.remove(command, session);
    }

    @Override
    public boolean removeCommandIfExists(String name) {
        checkSession();
        if (model.find(name, session) != null) {
            model.remove(name, session);
            return true;
        }
        return false;
    }

    @Override
    public NutsWorkspaceCustomCommand findCommand(String name, NutsId forId, NutsId forOwner) {
        checkSession();
        return model.find(name, forId, forOwner, session);
    }

    @Override
    public NutsWorkspaceCustomCommand findCommand(String name) {
        checkSession();
        return model.find(name, session);
    }

    @Override
    public List<NutsWorkspaceCustomCommand> findAllCommands() {
        checkSession();
        return model.findAll(session);
    }

    @Override
    public List<NutsWorkspaceCustomCommand> findCommandsByOwner(NutsId id) {
        checkSession();
        return model.findByOwner(id, session);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCustomCommandManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public void createLauncher(NutsLauncherOptions launcher) {
        checkSession();
        if(launcher==null){
            throw new NutsIllegalArgumentException(getSession(),NutsMessage.cstyle("missing launcher options"));
        }
        NutsId id = launcher.getId();
        if(id==null){
            throw new NutsIllegalArgumentException(getSession(),NutsMessage.cstyle("missing id"));
        }

        NutsWorkspace ws = getSession().getWorkspace();
        List<String> cmdLine = new ArrayList<>();
        cmdLine.add(id.getShortName());
        if(launcher.getArgs()!=null) {
            cmdLine.addAll(Arrays.asList(launcher.getArgs()));
        }
        String alias=launcher.getAlias();
        if(alias==null|| alias.isEmpty()){
            alias=id.getArtifactId();
        }
        if(launcher.isCreateAlias()) {
            if (!ws.commands().commandExists(alias)) {
                ws.commands()
                        .addCommand(
                                new NutsCommandConfig()
                                        .setName(alias)
                                        .setOwner(getSession().getAppId())
                                        .setCommand(ws.commandLine().create(cmdLine.toString()).toString())
                        );
            }
        }
        boolean installedNadmin = ws.search().addIds("net.thevpc.nuts.toolbox:nadmin")
                .setTargetApiVersion(ws.getApiId().getVersion())
                .setInstallStatus(ws.filters().installStatus().byDefaultValue(true))
                .getResultIds().first() != null;
        if (!installedNadmin && launcher.isInstallExtensions()) {
            ws.install().setId(
                    ws.search().addIds("net.thevpc.nuts.toolbox:nadmin")
                            .setTargetApiVersion(ws.getApiId().getVersion())
                            .setLatest(true)
                            .getResultIds().required()
            ).run();
            installedNadmin = true;
        }
        if (installedNadmin) {
            NutsExecCommand cmd = ws.exec().setCommand(
                    "net.thevpc.nuts.toolbox:nadmin",
                    "add", "script"
            );
            if (alias != null) {
                cmd.addCommand("--name",alias);
            }
            if (!GraphicsEnvironment.isHeadless()) {
                if(launcher.isCreateDesktopShortcut()){
                    cmd.addCommand("--desktop");
                }
                if(launcher.isCreateMenuShortcut()){
                    cmd.addCommand("--menu");
                }
                if(launcher.isCreateCustomShortcut()){
                    cmd.addCommand("--shortcut");
                }
                if(launcher.getShortcutName()!=null){
                    cmd.addCommand("--shortcut-name",launcher.getShortcutName());
                }
                if(launcher.getCustomShortcutPath()!=null){
                    cmd.addCommand("--shortcut-path",launcher.getCustomShortcutPath());
                }
                if(launcher.getMenuCategory()!=null){
                    cmd.addCommand("--menu-category",launcher.getMenuCategory());
                }
                if(launcher.getIcon()!=null){
                    cmd.addCommand("--icon",launcher.getMenuCategory());
                }
            }
            cmd.addCommand(id.getLongName());
            cmd.setFailFast(true);
            cmd.run();
        }
    }
}
