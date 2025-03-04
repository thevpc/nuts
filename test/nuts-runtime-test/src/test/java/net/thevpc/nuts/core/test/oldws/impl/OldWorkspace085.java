package net.thevpc.nuts.core.test.oldws.impl;

import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.NExecutionType;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.core.test.oldws.OldWorkspace;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMsg;

import java.io.File;

public class OldWorkspace085 extends OldWorkspace {
    public OldWorkspace085(File ws) {
        super(ws, "0.8.5");
    }


    public void upgrade() {
        NOut.println(NMsg.ofC("updating workspace %s in %s", version, ws));
        NExecCmd.of().setExecutionType(NExecutionType.SYSTEM)
                .addCommand(resolveJavaFile(), "-jar", resolveJarFile().getPath())
                .addCommand("--yes")
                .addCommand("--verbose")
                .addCommand("update")
                .failFast()
                .run();
    }

    @Override
    public void installWs() {
        downloadNutsJar();
        NOut.println(NMsg.ofC("booting workspace %s in %s", version, ws));
        NExecCmd.of().setExecutionType(NExecutionType.SYSTEM)
                .addCommand(resolveJavaFile(), "-jar", resolveJarFile().getPath())
                .addCommand("--desktop-launcher=unsupported")
                //disable creation of desktop menus
                .addCommand("--menu-launcher=unsupported")
                //disable creation of any icons
                .addCommand("--user-launcher=unsupported")
                //disable creating of bashrc, etc...
                .addCommand("--!switch")
                //disable auto-detection of java
                .addCommand("--!init-platforms")
                //disable auto-creation of nuts scripts
                .addCommand("--!init-scripts")
                //disable auto-creation of nuts icons and menus
                .addCommand("--!init-launchers")
                //disable progress indicator
//                    .addCommand("--!progress")
                //disable interactive mode and 'always confirm'
                .addCommand("--yes")
                //enable installing nsh
                .addCommand("--install-companions=true")
                .addCommand("--skip-welcome")
                .addCommand("-r=+thevpc,+maven?central-only")
                .addCommand("-w", ws.getAbsolutePath())
                .addCommand("-byZSK")
                .addCommand("-!k")
                .addCommand("--verbose")
                .addCommand("---no-local-maven")
                .failFast()
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
