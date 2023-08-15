package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.env.NArchFamily;
import net.thevpc.nuts.env.NDesktopEnvironmentFamily;
import net.thevpc.nuts.env.NDesktopIntegrationItem;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NSettingsNdiSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.SystemNdi;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NComponentScope(NScopeType.SESSION)
public class DefaultNEnvs implements NEnvs {

    public static final Pattern UNIX_USER_DIRS_PATTERN = Pattern.compile("^\\s*(?<k>[A-Z_]+)\\s*=\\s*(?<v>.*)$");
    private final DefaultNWorkspaceEnvManagerModel model;
    protected DefaultNPlatformModel sdkModel;
    private NSession session;

    public DefaultNEnvs(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().envModel;
        this.sdkModel = e.getModel().sdkModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public Map<String, Object> getProperties() {
        checkSession();
        return model.getProperties();
    }

    @Override
    public NOptional<NLiteral> getProperty(String property) {
        checkSession();
        return model.getProperty(property, getSession());
    }

    @Override
    public NEnvs setProperty(String property, Object value) {
        checkSession();
        model.setProperty(property, value);
        return this;
    }

    @Override
    public NOsFamily getOsFamily() {
//        checkSession();
        return model.getOsFamily();
    }

    @Override
    public String getHostName() {
        return model.getHostName();
    }

    @Override
    public String getPid() {
        return model.getPid();
    }

    @Override
    public Set<NShellFamily> getShellFamilies() {
        return model.getShellFamilies();
    }

    @Override
    public NShellFamily getShellFamily() {
        return model.getShellFamily();
    }

    @Override
    public NId getDesktopEnvironment() {
        return getDesktopEnvironments().stream().findFirst().get();
    }

    @Override
    public Set<NId> getDesktopEnvironments() {
        return model.getDesktopEnvironments(session);
    }

    @Override
    public NDesktopEnvironmentFamily getDesktopEnvironmentFamily() {
        return model.getDesktopEnvironmentFamily(session);
    }

    @Override
    public Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies() {
        return model.getDesktopEnvironmentFamilies(session);
    }

    @Override
    public NId getPlatform() {
//        checkSession();
        return model.getPlatform(session);
    }

    @Override
    public NId getOs() {
//        checkSession();
        return model.getOs();
    }

    public NId getOsDist() {
//        checkSession();
        return model.getOsDist();
    }

    @Override
    public NId getArch() {
//        checkSession();
        return model.getArch();
    }

    @Override
    public NArchFamily getArchFamily() {
//        checkSession();
        return model.getArchFamily();
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NEnvs setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }


    @Override
    public boolean isGraphicalDesktopEnvironment() {
        return model.isGraphicalDesktopEnvironment();
    }

    @Override
    public NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem item) {
        checkSession();
        NAssert.requireNonBlank(item, "item", session);
        switch (item) {
            case DESKTOP: {
                NSupportMode a = NBootManager.of(session).getBootOptions().getDesktopLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
            case MENU: {
                NSupportMode a = NBootManager.of(session).getBootOptions().getMenuLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
            case USER: {
                NSupportMode a = NBootManager.of(session).getBootOptions().getUserLauncher().orNull();
                if (a != null) {
                    return a;
                }
                break;
            }
        }
        switch (getOsFamily()) {
            case LINUX: {
                switch (item) {
                    case DESKTOP: {
                        return NSupportMode.SUPPORTED;
                    }
                    case MENU: {
                        return NSupportMode.PREFERRED;
                    }
                    case USER: {
                        return NSupportMode.PREFERRED;
                    }
                }
                break;
            }
            case UNIX: {
                return NSupportMode.NEVER;
            }
            case WINDOWS: {
                switch (item) {
                    case DESKTOP: {
                        if (Files.isDirectory(getDesktopPath())) {
                            return NSupportMode.PREFERRED;
                        }
                        return NSupportMode.SUPPORTED;
                    }
                    case MENU: {
                        return NSupportMode.PREFERRED;
                    }
                    case USER: {
                        return NSupportMode.PREFERRED;
                    }
                }
                break;
            }
            case MACOS: {
                return NSupportMode.NEVER;
            }
            case UNKNOWN: {
                return NSupportMode.NEVER;
            }
        }
        return NSupportMode.NEVER;
    }

    public Path getDesktopPath() {
        switch (getOsFamily()) {
            case LINUX:
            case UNIX:
            case MACOS: {
                File f = new File(System.getProperty("user.home"), ".config/user-dirs.dirs");
                if (f.exists()) {
                    try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("#")) {
                                //ignore
                            } else {
                                Matcher m = UNIX_USER_DIRS_PATTERN.matcher(line);
                                if (m.find()) {
                                    String k = m.group("k");
                                    if (k.equals("XDG_DESKTOP_DIR")) {
                                        String v = m.group("v");
                                        v = v.trim();
                                        if (v.startsWith("\"")) {
                                            int last = v.indexOf('\"', 1);
                                            String s = v.substring(1, last);
                                            s = s.replace("$HOME", System.getProperty("user.home"));
                                            return Paths.get(s);
                                        } else {
                                            return Paths.get(v);
                                        }
                                    }
                                } else {
                                    //this is unexpected format!
                                    break;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        //ignore
                    }
                }
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
            case WINDOWS: {
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
            default: {
                return new File(System.getProperty("user.home"), "Desktop").toPath();
            }
        }
    }

    public void addLauncher(NLauncherOptions launcher) {
        checkSession();
        //apply isolation!
        NIsolationLevel isolation = NBootManager.of(session).getBootOptions().getIsolationLevel().orElse(NIsolationLevel.SYSTEM);
        if (isolation.compareTo(NIsolationLevel.CONFINED) >= 0) {
            launcher.setCreateDesktopLauncher(NSupportMode.NEVER);
            launcher.setCreateMenuLauncher(NSupportMode.NEVER);
            launcher.setCreateUserLauncher(NSupportMode.NEVER);
            launcher.setSwitchWorkspace(false);
            launcher.setSwitchWorkspaceLocation(null);
        }
        NSession session = getSession();
        SystemNdi ndi = NSettingsNdiSubCommand.createNdi(session);
        if (ndi != null) {
            ndi.addScript(
                    new NdiScriptOptions()
                            .setSession(session)
                            .setLauncher(launcher.copy()),
                    new String[]{launcher.getId().builder().getFullName()}
            );
        }
    }

    private void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    public DefaultNWorkspaceEnvManagerModel getModel() {
        return model;
    }

    private DefaultNWorkspaceConfigModel _configModel() {
        DefaultNConfigs config = (DefaultNConfigs) NConfigs.of(session);
        return config.getModel();
    }

//    public boolean matchCondition(NutsSupportCondition request, NutsSupportMode support) {
//        checkSession();
//        if (request == null) {
//            request = NutsSupportCondition.NEVER;
//        }
//        if (support == null) {
//            support = NutsSupportMode.UNSUPPORTED;
//        }
//        switch (support) {
//            case UNSUPPORTED: {
//                return false;
//            }
//            case SUPPORTED: {
//                switch (request) {
//                    case NEVER:
//                        return false;
//                    case ALWAYS:
//                    case SUPPORTED: {
//                        return true;
//                    }
//                    case PREFERRED: {
//                        return false;
//                    }
//                    default: {
//                        throw new NutsUnsupportedEnumException(getSession(), request);
//                    }
//                }
//            }
//            case PREFERRED: {
//                switch (request) {
//                    case NEVER:
//                        return false;
//                    case ALWAYS:
//                    case PREFERRED: {
//                        return true;
//                    }
//                    case SUPPORTED: {
//                        return false;
//                    }
//                    default: {
//                        throw new NutsUnsupportedEnumException(getSession(), request);
//                    }
//                }
//            }
//            default: {
//                throw new NutsUnsupportedEnumException(getSession(), support);
//            }
//        }
//    }

//    public void addLauncherOld(NutsLauncherOptions launcher) {
//        checkSession();
//        if (launcher == null) {
//            throw new NutsIllegalArgumentException(getSession(), NMsg.ofC("missing launcher options"));
//        }
//        NutsId id = launcher.getId();
//        if (id == null) {
//            throw new NutsIllegalArgumentException(getSession(), NMsg.ofC("missing id"));
//        }
//
//        NutsWorkspace ws = getSession().getWorkspace();
//        List<String> cmdLine = new ArrayList<>();
//        cmdLine.add(id.getShortName());
//        if (launcher.getArgs() != null) {
//            cmdLine.addAll(Arrays.asList(launcher.getArgs()));
//        }
//        String alias = launcher.getAlias();
//        if (alias == null || alias.isEmpty()) {
//            alias = id.getArtifactId();
//        }
//        if (launcher.isCreateAlias()) {
//            if (!ws.commands().commandExists(alias)) {
//                ws.commands()
//                        .addCommand(
//                                new NutsCommandConfig()
//                                        .setName(alias)
//                                        .setOwner(getSession().getAppId())
//                                        .setCommand(ws.commandLine().create(cmdLine.toString()).toString())
//                        );
//            }
//        }
//
//        NutsExecCommand cmd = ws.exec().setCommand("settings", "add", "launcher");
//        if (alias != null) {
//            cmd.addCommand("--name", alias);
//        }
//        if (!GraphicsEnvironment.isHeadless()) {
//            if (launcher.getCreateDesktopShortcut() != null) {
//                switch (launcher.getCreateDesktopShortcut()) {
//                    case NEVER: {
//                        break;
//                    }
//                    case ALWAYS: {
//                        cmd.addCommand("--desktop=always");
//                        break;
//                    }
//                    case PREFERRED: {
//                        cmd.addCommand("--desktop=preferred");
//                        break;
//                    }
//                    case SUPPORTED: {
//                        cmd.addCommand("--desktop=supported");
//                        break;
//                    }
//                    default: {
//                        throw new NutsUnsupportedEnumException(getSession(), launcher.getCreateDesktopShortcut());
//                    }
//                }
//            }
//            if (launcher.getCreateMenuShortcut() != null) {
//                switch (launcher.getCreateMenuShortcut()) {
//                    case NEVER: {
//                        break;
//                    }
//                    case ALWAYS: {
//                        cmd.addCommand("--menu=always");
//                        break;
//                    }
//                    case PREFERRED: {
//                        cmd.addCommand("--menu=preferred");
//                        break;
//                    }
//                    case SUPPORTED: {
//                        cmd.addCommand("--menu=supported");
//                        break;
//                    }
//                    default: {
//                        throw new NutsUnsupportedEnumException(getSession(), launcher.getCreateMenuShortcut());
//                    }
//                }
//            }
//            if (launcher.getCreateCustomShortcut() != null) {
//                switch (launcher.getCreateCustomShortcut()) {
//                    case NEVER: {
//                        break;
//                    }
//                    case ALWAYS: {
//                        cmd.addCommand("--shortcut=always");
//                        break;
//                    }
//                    case PREFERRED: {
//                        cmd.addCommand("--shortcut=preferred");
//                        break;
//                    }
//                    case SUPPORTED: {
//                        cmd.addCommand("--shortcut=supported");
//                        break;
//                    }
//                    default: {
//                        throw new NutsUnsupportedEnumException(getSession(), launcher.getCreateCustomShortcut());
//                    }
//                }
//            }
//            if (launcher.getShortcutName() != null) {
//                cmd.addCommand("--shortcut-name", launcher.getShortcutName());
//            }
//            if (launcher.getCustomShortcutPath() != null) {
//                cmd.addCommand("--shortcut-path", launcher.getCustomShortcutPath());
//            }
//            if (launcher.getMenuCategory() != null) {
//                cmd.addCommand("--menu-category", launcher.getMenuCategory());
//            }
//            if (launcher.getIcon() != null) {
//                cmd.addCommand("--icon", launcher.getMenuCategory());
//            }
//        }
//        if (launcher.isOpenTerminal()) {
//            cmd.addCommand("--terminal");
//        }
//        cmd.addCommand(id.getLongName());
//        cmd.setFailFast(true);
//        cmd.setExecutionType(NutsExecutionType.EMBEDDED);
//        cmd.run();
//    }


    @Override
    public List<String> buildEffectiveCommand(String[] cmd, NRunAs runAsMode, Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich, Boolean gui, String rootName, String userName, String[] executorOptions) {
        return NSysExecUtils.buildEffectiveCommand(cmd, runAsMode, de, sysWhich, gui, rootName, userName, executorOptions, session);
    }
}
