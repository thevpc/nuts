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

    public void addLauncher(NutsLauncherOptions launcher) {
        checkSession();
        if (launcher == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing launcher options"));
        }
        NutsId id = launcher.getId();
        if (id == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing id"));
        }

        NutsWorkspace ws = getSession().getWorkspace();
        List<String> cmdLine = new ArrayList<>();
        cmdLine.add(id.getShortName());
        if (launcher.getArgs() != null) {
            cmdLine.addAll(Arrays.asList(launcher.getArgs()));
        }
        String alias = launcher.getAlias();
        if (alias == null || alias.isEmpty()) {
            alias = id.getArtifactId();
        }
        if (launcher.isCreateAlias()) {
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

        NutsExecCommand cmd = ws.exec().setCommand("settings", "add", "launcher");
        if (alias != null) {
            cmd.addCommand("--name", alias);
        }
        if (!GraphicsEnvironment.isHeadless()) {
            if (launcher.getCreateDesktopShortcut() != null) {
                switch (launcher.getCreateDesktopShortcut()) {
                    case NEVER: {
                        break;
                    }
                    case ALWAYS: {
                        cmd.addCommand("--desktop=always");
                        break;
                    }
                    case PREFERRED: {
                        cmd.addCommand("--desktop=preferred");
                        break;
                    }
                    case SUPPORTED: {
                        cmd.addCommand("--desktop=supported");
                        break;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(getSession(), launcher.getCreateDesktopShortcut());
                    }
                }
            }
            if (launcher.getCreateMenuShortcut() != null) {
                switch (launcher.getCreateMenuShortcut()) {
                    case NEVER: {
                        break;
                    }
                    case ALWAYS: {
                        cmd.addCommand("--menu=always");
                        break;
                    }
                    case PREFERRED: {
                        cmd.addCommand("--menu=preferred");
                        break;
                    }
                    case SUPPORTED: {
                        cmd.addCommand("--menu=supported");
                        break;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(getSession(), launcher.getCreateMenuShortcut());
                    }
                }
            }
            if (launcher.getCreateCustomShortcut() != null) {
                switch (launcher.getCreateCustomShortcut()) {
                    case NEVER: {
                        break;
                    }
                    case ALWAYS: {
                        cmd.addCommand("--shortcut=always");
                        break;
                    }
                    case PREFERRED: {
                        cmd.addCommand("--shortcut=preferred");
                        break;
                    }
                    case SUPPORTED: {
                        cmd.addCommand("--shortcut=supported");
                        break;
                    }
                    default: {
                        throw new NutsUnsupportedEnumException(getSession(), launcher.getCreateCustomShortcut());
                    }
                }
            }
            if (launcher.getShortcutName() != null) {
                cmd.addCommand("--shortcut-name", launcher.getShortcutName());
            }
            if (launcher.getCustomShortcutPath() != null) {
                cmd.addCommand("--shortcut-path", launcher.getCustomShortcutPath());
            }
            if (launcher.getMenuCategory() != null) {
                cmd.addCommand("--menu-category", launcher.getMenuCategory());
            }
            if (launcher.getIcon() != null) {
                cmd.addCommand("--icon", launcher.getMenuCategory());
            }
        }
        cmd.addCommand(id.getLongName());
        cmd.setFailFast(true);
        cmd.setExecutionType(NutsExecutionType.EMBEDDED);
        cmd.run();

    }
}
