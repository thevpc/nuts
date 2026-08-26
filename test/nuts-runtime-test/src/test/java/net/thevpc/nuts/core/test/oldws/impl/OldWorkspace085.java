package net.thevpc.nuts.core.test.oldws.impl;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.core.test.oldws.OldWorkspace;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NMsg;

import java.io.File;

public class OldWorkspace085 extends OldWorkspace {
    public OldWorkspace085(File ws) {
        super(ws, "0.8.5");
    }


    public void upgrade() {
        NOut.println(NMsg.ofC("updating workspace %s in %s", version, ws));
        NExec.of().executionType(NExecutionType.SYSTEM)
                .command(resolveJavaFile(), "-jar", resolveJarFile().getPath())
                .command("--workspace=" + workspaceLocation)
                .command("--yes")
                .command("--verbose")
                .command("update")
                .failFast(true)
                .run();
    }

    @Override
    public void installWs() {
        downloadNutsJar();
        NOut.println(NMsg.ofC("booting workspace %s in %s", version, ws));
        NExec.ofSystem()
                .command(resolveJavaFile(), "-jar", resolveJarFile().getPath())
                .command("--workspace=" + workspaceLocation)
                .command("--desktop-launcher=unsupported")
                //disable creation of desktop menus
                .command("--menu-launcher=unsupported")
                //disable creation of any icons
                .command("--user-launcher=unsupported")
                //disable creating of bashrc, etc...
                .command("--!switch")
                //disable auto-detection of java
                .command("--!init-platforms")
                //disable auto-creation of nuts scripts
                .command("--!init-scripts")
                //disable auto-creation of nuts icons and menus
                .command("--!init-launchers")
                //disable progress indicator
//                    .addCommand("--!progress")
                //disable interactive mode and 'always confirm'
                .command("--yes")
                //enable installing nsh
                .command("--install-companions=true")
                .command("--skip-welcome")
                .command("-r=+thevpc,+maven?central-only")
                .command("-w", ws.getAbsolutePath())
                .command("-byZSK")
                .command("-!k")
                .command("--verbose")
                .command("---no-local-maven")
                .failFast(true)
                .run();
    }

    private void downloadNutsJar() {
        String url = "https://maven.thevpc.net/net/thevpc/nuts/nuts-app/" + version + "/nuts-app-" + version + ".jar";
        NOut.println(NMsg.ofC("downloading %s to %s", NPath.of(url), resolveJarFile()));
        NCp.of().from(NPath.of(url))
                .to(resolveJarFile())
                .run();
    }
}
