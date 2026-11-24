package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.win.WindowsNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * On Windows only one "nuts" launcher is generated (nuts.cmd), but the
 * PowerShell terminal initializer used to reference nuts.ps1, which does not
 * exist. Every generated script must point to the launcher that was actually
 * created.
 */
public class NdiLauncherTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void termInitUsesTheGeneratedLauncher() {
        WindowsNdi ndi = new WindowsNdi() {
            @Override
            public NShellFamily getPreferredBinScriptFamily() {
                return NShellFamily.WIN_CMD;
            }

            @Override
            public String getExecFileName(String name, NShellFamily shellFamily) {
                return name + (shellFamily == NShellFamily.WIN_CMD ? ".cmd" : ".ps1");
            }
        };
        NdiScriptOptions options = new NdiScriptOptions();
        String path = ndi.getNutsStart(options, NShellFamily.WIN_POWER_SHELL).path().toString();
        Assertions.assertTrue(path.endsWith("nuts.cmd"),
                "the PowerShell init script must call the generated nuts.cmd, but got " + path);
    }

    @Test
    public void powershellInitPrependsTheBinFolderToPath() throws Exception {
        String template;
        try (InputStream is = NdiLauncherTest.class.getResourceAsStream(
                "/net/thevpc/nuts/runtime/settings/template-nuts-init/template-nuts-init.ps1")) {
            Assertions.assertNotNull(is, "missing nuts-init.ps1 template");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = is.read(buffer)) > 0) {
                bos.write(buffer, 0, count);
            }
            template = new String(bos.toByteArray(), StandardCharsets.UTF_8);
        }
        Assertions.assertTrue(template.contains("$env:Path=\"$env:NUTS_WORKSPACE_BINDIR;$env:Path\""),
                "the PowerShell init script must read the NUTS_WORKSPACE_BINDIR environment variable "
                        + "and prepend it to PATH without dropping the existing PATH");
    }

    @Test
    public void installingLaunchersRemovesOlderVersions() throws Exception {
        NdiScriptOptions options = new NdiScriptOptions();
        NPath versionFolder = options.resolveBinFolder().parent();
        NPath artifactFolder = versionFolder.parent();
        NPath staleVersionFolder = artifactFolder.resolve("0.0.1");
        Files.createDirectories(staleVersionFolder.toPath().get());
        Files.write(staleVersionFolder.toPath().get().resolve("stale.txt"),
                "stale".getBytes(StandardCharsets.UTF_8));

        NWorkspaceUtils.of().installScriptsAndLaunchers(false);

        Assertions.assertFalse(Files.exists(staleVersionFolder.toPath().get()),
                "installing launchers must remove launcher folders of older versions");
    }

    @Test
    public void powershellTerminalIsNotNested() {
        NdiScriptOptions options = new NdiScriptOptions();
        WindowsNdi ndi = new WindowsNdi() {
            @Override
            public NdiScriptInfo getNutsTerm(NdiScriptOptions options, NShellFamily shellFamily) {
                return fakeScript("/tmp/nuts/bin/nuts-term.ps1");
            }

            @Override
            public NdiScriptInfo getIncludeNutsTermInit(NdiScriptOptions options, NShellFamily shellFamily) {
                return fakeScript("/tmp/nuts/inc/.nuts-term-init.ps1");
            }

            @Override
            public NOptional<NPath> findPwShOrPowerShell() {
                return NOptional.of(NPath.of("powershell.exe"));
            }
        };
        String[] cmd = ndi.getNutsTermFullCommand(options, NShellFamily.WIN_POWER_SHELL);
        Assertions.assertTrue(Arrays.asList(cmd).contains("-Command"),
                "the Windows terminal shortcut must initialize the current session, got " + Arrays.toString(cmd));
        Assertions.assertFalse(Arrays.asList(cmd).contains("-File"),
                "the Windows terminal shortcut must not launch a nested PowerShell, got " + Arrays.toString(cmd));
        Assertions.assertTrue(cmd[cmd.length - 1].contains("nuts-term.ps1"),
                "the Windows terminal shortcut must dot-source nuts-term.ps1, got " + Arrays.toString(cmd));
    }

    private static NdiScriptInfo fakeScript(String path) {
        return new NdiScriptInfo() {
            @Override
            public NPath path() {
                return NPath.of(path);
            }

            @Override
            public PathInfo create() {
                return null;
            }
        };
    }

    @Test
    public void powershellTermTemplateDoesNotSpawnANewShell() throws Exception {
        String template;
        try (InputStream is = NdiLauncherTest.class.getResourceAsStream(
                "/net/thevpc/nuts/runtime/settings/template-nuts-term/template-nuts-term.ps1")) {
            Assertions.assertNotNull(is, "missing nuts-term.ps1 template");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = is.read(buffer)) > 0) {
                bos.write(buffer, 0, count);
            }
            template = new String(bos.toByteArray(), StandardCharsets.UTF_8);
        }
        Assertions.assertFalse(template.contains("NUTS_SYS_TERM_START = \"powershell.exe"),
                "the default PowerShell terminal must not start a nested powershell.exe");
        Assertions.assertTrue(template.contains(". \"$$SCRIPT_NUTS_TERM_INIT$$\"") || template.contains("else {\n    . "),
                "the PowerShell terminal must source the init script in the current session");
    }
}
