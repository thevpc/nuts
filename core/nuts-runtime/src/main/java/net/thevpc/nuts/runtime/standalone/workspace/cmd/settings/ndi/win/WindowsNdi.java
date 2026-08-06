package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.win;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptInfoBase;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.runtime.standalone.xtra.shell.NShellHelper;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class WindowsNdi extends BaseSystemNdi {
    private static volatile NOptional<NPath> cachedPwshOrPowerShell;
    private static volatile Boolean restrictedMode;

    public WindowsNdi() {
        super();
    }

    protected NShellFamily[] getShellGroups() {
        Set<NShellFamily> all = new LinkedHashSet<>(NEnv.of().shellFamilies());
        all.retainAll(Arrays.asList(NShellFamily.WIN_CMD, NShellFamily.WIN_POWER_SHELL));
        return all.toArray(new NShellFamily[0]);
    }

    public NPath getProfilePath() {
        try {
            String[] cmd = {"powershell", "-NoProfile", "-Command", "echo $PROFILE.CurrentUserAllHosts"};
            String s = NStringUtils.strip(NExec.ofSystem(cmd).grabbedAll());
            if (NStringUtils.isBlank(s)) {
                return null;
            }
            return NPath.of(s);
        } catch (Exception ex) {
            //
        }
        return null;
    }

    public boolean isRestrictedMode() {
        if (restrictedMode == null) {
            String profilePath = null;
            try {
                String[] cmd = {findPwShOrPowerShell().orDefault().toString(), "-NoProfile", "-Command", "Get-ExecutionPolicy"};
                profilePath = NExec.ofSystem(cmd).grabbedAll().trim();
            } catch (Exception ex) {
                //
            }
            switch (NStringUtils.strip(profilePath).toLowerCase()) {
                case "remotesigned":
                case "unrestricted":
                case "bypass": {
                    restrictedMode = false;
                    break;
                }
                case "allsigned":
                case "restricted": {
                    restrictedMode = true;
                    break;
                }
                default: {
                    restrictedMode = true;
                }
            }
        }
        return restrictedMode;
    }

    public NdiScriptInfo getSysRc(NdiScriptOptions options, NShellFamily shellFamily) {
        if (shellFamily != NShellFamily.WIN_POWER_SHELL) {
            return null;
        }
        String ext = extension(shellFamily);
        if (NBlankable.isBlank(ext)) {
            return null;
        }
        if (isRestrictedMode()) {
            return null;
        }
        NPath profilePath = getProfilePath();
        if (profilePath != null) {
            return new NdiScriptInfoBase(profilePath) {
                @Override
                public PathInfo create() {
                    NPath apiConfigFile = path();
                    NShellHelper sh = NShellHelper.of(shellFamily);
                    return addFileLine("sysrc",
                            options.resolveNutsApiId(),
                            apiConfigFile, getCommentLineConfigHeader(),
                            sh.getCallScriptCommand("_NUTS_INIT", getIncludeNutsInit(options, shellFamily).path().toString()),
                            sh.getShebanSh(), shellFamily);
                }
            };
        }
        return null;
    }

    @Override
    public NdiScriptInfo getIncludeNutsCompletion(NdiScriptOptions options, NShellFamily shellFamily) {
        if (shellFamily != NShellFamily.WIN_POWER_SHELL) {
            return null;
        }
        String ext = extension(shellFamily);
        if (NBlankable.isBlank(ext)) {
            return null;
        }
        return new NdiScriptInfoBase(options.resolveIncFolder().resolve(".nuts-complete." + ext)) {
            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("nuts-complete", shellFamily, "nuts-complete", options.resolveNutsApiId(), options)
                        .setPath(path())
                        .build();
            }
        };
    }

    @Override
    protected String createNutsScriptContent(NId fnutsId, NdiScriptOptions options, NShellFamily shellFamily) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts", shellFamily)).append(" ").append(NShellHelper.of(shellFamily).varRef("NUTS_OPTIONS")).append(" ");
        if (options.getLauncher().nutsOptions() != null) {
            for (String o : options.getLauncher().nutsOptions()) {
                command.append(" ").append(o);
            }
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" %*");
        return command.toString();
    }

    public void onPostGlobal(NdiScriptOptions options, PathInfo[] updatedPaths) {

    }


    @Override
    public String getExecFileName(String name, NShellFamily shellFamily) {
        String ext = extension(shellFamily);
        if (NBlankable.isBlank(ext)) {
            ext = "cmd";
        }
        return name + "." + ext;
    }

    @Override
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new WindowFreeDesktopEntryWriter(
                NEnv.of().desktopPath() == null ? null : NPath.of(NEnv.of().desktopPath())
        );
    }

    public String[] getNutsTermFullCommand(NdiScriptOptions options, NShellFamily shellFamily) {
        if (shellFamily == NShellFamily.WIN_POWER_SHELL) {
            return new String[]{
                    findPwShOrPowerShell().orDefault().toString()
                    , "-NoExit", "-ExecutionPolicy", "Bypass", "-File", getNutsTerm(options, shellFamily).path().toString()};
        }
        return super.getNutsTermFullCommand(options, shellFamily);
    }


    public NOptional<NPath> findPwShOrPowerShell() {
        if (cachedPwshOrPowerShell == null) {
            NOptional<NPath> a = findPwsh();
            if (a.isPresent()) {
                cachedPwshOrPowerShell = a;
            } else {
                cachedPwshOrPowerShell = findPowerShell();
            }
        }
        return cachedPwshOrPowerShell;
    }

    public NOptional<NPath> findPowerShell() {
        return findCommand("powershell.exe").withDefault(NPath.of("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe"));
    }

    public NOptional<NPath> findPwsh() {
        return findCommand("pwsh.exe");
    }

    public NOptional<NPath> findCommand(String cmd) {
        for (String line : NStringUtils.splitLines(NExec.ofSystem("where.exe", cmd).grabbedAll().trim())) {
            if (!NStringUtils.isBlank(line)) {
                NPath p = NPath.of(line);
                if (p.exists()) {
                    return NOptional.of(p);
                }
            }
        }
        String path = NEnv.of().env().get("PATH");
        if (path != null) {
            for (String s : path.split(File.pathSeparator)) {
                if (!NBlankable.isBlank(s)) {
                    NPath p = NPath.of(path).resolve(cmd);
                    if (p.isRegularFile()) {
                        return NOptional.of(p);
                    }
                }
            }
        }
        return NOptional.<NPath>ofNamedEmpty(cmd).withDefault(NPath.of("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe"));
    }

    protected int resolveIconExtensionPriority(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            //support only ico
            case "ico":
                return 3;
        }
        return -1;
    }

    public boolean isShortcutFileNameUserFriendly() {
        return true;
    }


    @Override
    public String getTemplateName(String name, NShellFamily shellFamily) {
        String ext = extension(shellFamily);
        if (NBlankable.isBlank(ext)) {
            ext = "cmd";
        }
        String n = "template-" + name;
        return n + "/" + n + "." + ext;
    }


    public NdiScriptInfo[] getNutsTerm(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getNutsTerm(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);

    }

    public NdiScriptInfo getNutsTerm(NdiScriptOptions options, NShellFamily shellFamily) {
        String ext = extension(shellFamily);
        if (NBlankable.isBlank(ext)) {
            return null;
        }
        if (!OptionalMsLinkHelper.isSupported()) {
            return null;
        }
        return new NdiScriptInfoBase(options.resolveBinFolder().resolve("nuts-term." + ext)) {

            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("nuts-term", shellFamily, "nuts-term", options.resolveNutsApiId(), options)
                        .setPath(path())
                        .build();
            }
        };
    }

    public NdiScriptInfo getIncludeNutsEnv(NdiScriptOptions options, NShellFamily shellFamily) {
        String ext = extension(shellFamily);
        if (NBlankable.isBlank(ext)) {
            return null;
        }
        return new NdiScriptInfoBase(options.resolveIncFolder().resolve(".nuts-env." + ext)) {

            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("nuts-env", shellFamily, "nuts-env", options.resolveNutsApiId(), options)
                        .setPath(path())
                        .build();
            }
        };
    }

    public NdiScriptInfo getIncludeNutsTermInit(NdiScriptOptions options, NShellFamily shellFamily) {
        String ext = extension(shellFamily);
        if (NBlankable.isBlank(ext)) {
            return null;
        }
        if (!OptionalMsLinkHelper.isSupported()) {
            return null;
        }
        return
                new NdiScriptInfoBase(options.resolveIncFolder().resolve(".nuts-term-init." + ext)) {

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-term-init", shellFamily, "nuts-term-init", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                }
                ;
    }

    public NdiScriptInfo getIncludeNutsInit(NdiScriptOptions options, NShellFamily shellFamily) {
        String ext = extension(shellFamily);
        if (NBlankable.isBlank(ext)) {
            return null;
        }
        return new NdiScriptInfoBase(options.resolveIncFolder().resolve(".nuts-init." + ext)) {
            @Override
            public PathInfo create() {
                NPath apiConfigFile = path();
                return scriptBuilderTemplate("nuts-init", shellFamily, "nuts-init", options.resolveNutsApiId(), options)
                        .setPath(apiConfigFile)
                        .buildAddLine(WindowsNdi.this);
            }
        };
    }


    private String extension(NShellFamily shellFamily) {
        switch (shellFamily) {
            case WIN_CMD: {
                return "cmd";
            }
            case WIN_POWER_SHELL: {
                return "ps1";
            }
        }
        return "";
    }

    public NShellFamily getPreferredBinScriptFamily() {
        Set<NShellFamily> shellGroupsSet = NEnv.of().shellFamilies();
        NShellFamily[] shellGroupsArr = shellGroupsSet.toArray(new NShellFamily[0]);
        NShellFamily expected = NShellFamily.WIN_CMD;
        if (shellGroupsSet.contains(expected) || shellGroupsSet.isEmpty()) {
            return expected;
        }
        return shellGroupsArr[0];
    }

}
