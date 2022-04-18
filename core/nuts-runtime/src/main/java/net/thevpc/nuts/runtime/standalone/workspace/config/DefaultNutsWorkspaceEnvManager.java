package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NutsSettingsNdiSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.SystemNdi;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultNutsWorkspaceEnvManager implements NutsWorkspaceEnvManager {

    public static final Pattern UNIX_USER_DIRS_PATTERN = Pattern.compile("^\\s*(?<k>[A-Z_]+)\\s*=\\s*(?<v>.*)$");
    private final DefaultNutsWorkspaceEnvManagerModel model;
    protected DefaultNutsPlatformModel sdkModel;
    private NutsSession session;

    public DefaultNutsWorkspaceEnvManager(DefaultNutsWorkspaceEnvManagerModel model) {
        this.model = model;
        this.sdkModel = new DefaultNutsPlatformModel(model);
    }

    @Override
    public Map<String, Object> getProperties() {
        checkSession();
        return model.getProperties();
    }

    @Override
    public NutsElement getPropertyElement(String property) {
        checkSession();
        return model.getPropertyElement(property, getSession());
    }

    @Override
    public Object getProperty(String property) {
        checkSession();
        return model.getProperty(property, getSession());
    }

    @Override
    public NutsWorkspaceEnvManager setProperty(String property, Object value) {
        checkSession();
        model.setProperty(property, value);
        return this;
    }

    @Override
    public NutsOsFamily getOsFamily() {
//        checkSession();
        return model.getOsFamily();
    }

    @Override
    public Set<NutsShellFamily> getShellFamilies() {
        return model.getShellFamilies();
    }

    @Override
    public NutsShellFamily getShellFamily() {
        return model.getShellFamily();
    }

    @Override
    public NutsId getDesktopEnvironment() {
        return getDesktopEnvironments().stream().findFirst().get();
    }

    @Override
    public Set<NutsId> getDesktopEnvironments() {
        return model.getDesktopEnvironments(session);
    }

    @Override
    public NutsDesktopEnvironmentFamily getDesktopEnvironmentFamily() {
        return model.getDesktopEnvironmentFamily(session);
    }

    @Override
    public Set<NutsDesktopEnvironmentFamily> getDesktopEnvironmentFamilies() {
        return model.getDesktopEnvironmentFamilies(session);
    }

    @Override
    public NutsPlatformManager platforms() {
        return new DefaultNutsPlatformManager(sdkModel).setSession(getSession());
    }

    @Override
    public NutsId getPlatform() {
//        checkSession();
        return model.getPlatform(session);
    }

    @Override
    public NutsId getOs() {
//        checkSession();
        return model.getOs();
    }

    public NutsId getOsDist() {
//        checkSession();
        return model.getOsDist();
    }

    @Override
    public NutsId getArch() {
//        checkSession();
        return model.getArch();
    }

    @Override
    public NutsArchFamily getArchFamily() {
//        checkSession();
        return model.getArchFamily();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspaceEnvManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }


    @Override
    public boolean isGraphicalDesktopEnvironment() {
        return model.isGraphicalDesktopEnvironment();
    }

    @Override
    public NutsSupportMode getDesktopIntegrationSupport(NutsDesktopIntegrationItem item) {
        checkSession();
        if (item == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing item"));
        }
        String optionName = null;
        switch (item) {
            case DESKTOP: {
                optionName = "---system-desktop-launcher";
                break;
            }
            case MENU: {
                optionName = "---system-menu-launcher";
                break;
            }
            case SHORTCUT: {
                optionName = "---system-custom-launcher";
                break;
            }
        }
        if (optionName != null) {
            NutsArgument bootCustomArgument = session.boot().getBootCustomArgument(optionName);
            String o = bootCustomArgument == null ? null : bootCustomArgument.getString();
            if (!NutsBlankable.isBlank(o)) {
                NutsSupportMode q = NutsSupportMode.parse(o).orElse(null);
                if (q != null) {
                    return q;
                }
            }
        }
        switch (getOsFamily()) {
            case LINUX: {
                switch (item) {
                    case DESKTOP: {
                        return NutsSupportMode.SUPPORTED;
                    }
                    case MENU: {
                        return NutsSupportMode.PREFERRED;
                    }
                    case SHORTCUT: {
                        return NutsSupportMode.PREFERRED;
                    }
                }
                break;
            }
            case UNIX: {
                return NutsSupportMode.UNSUPPORTED;
            }
            case WINDOWS: {
                switch (item) {
                    case DESKTOP: {
                        if (Files.isDirectory(getDesktopPath())) {
                            return NutsSupportMode.PREFERRED;
                        }
                        return NutsSupportMode.SUPPORTED;
                    }
                    case MENU: {
                        return NutsSupportMode.PREFERRED;
                    }
                    case SHORTCUT: {
                        return NutsSupportMode.PREFERRED;
                    }
                }
                break;
            }
            case MACOS: {
                return NutsSupportMode.UNSUPPORTED;
            }
            case UNKNOWN: {
                return NutsSupportMode.UNSUPPORTED;
            }
        }
        return NutsSupportMode.UNSUPPORTED;
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

    public void addLauncher(NutsLauncherOptions launcher) {
        checkSession();
        NutsSession session = getSession();
        SystemNdi ndi = NutsSettingsNdiSubCommand.createNdi(session);
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
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
    }

    public DefaultNutsWorkspaceEnvManagerModel getModel() {
        return model;
    }

    private DefaultNutsWorkspaceConfigModel _configModel() {
        DefaultNutsWorkspaceConfigManager config = (DefaultNutsWorkspaceConfigManager) session.config();
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
//            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing launcher options"));
//        }
//        NutsId id = launcher.getId();
//        if (id == null) {
//            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing id"));
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

}
