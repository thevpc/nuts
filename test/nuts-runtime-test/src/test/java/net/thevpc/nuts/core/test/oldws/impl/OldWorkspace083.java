package net.thevpc.nuts.core.test.oldws.impl;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.core.test.oldws.OldWorkspace;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NMsg;

import java.io.File;

public class OldWorkspace083 extends OldWorkspace {
    public OldWorkspace083(File ws) {
        super(ws, "0.8.3");
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
        NExec.of().executionType(NExecutionType.SYSTEM)
                .command(resolveJavaFile(), "-jar", resolveJarFile().getPath())
                //disable creating of bashrc, etc...
                .command("--workspace=" + workspaceLocation)
                .command("--!switch")
                //disable progress indicator
//                    .addCommand("--!progress")
                //disable interactive mode and 'always confirm'
                .command("--yes")
                //enable installing nsh
                .command("--skip-welcome")
                .command("-r=+thevpc,maven-central")
                .command("-w", ws.getAbsolutePath())
                .command("-byZSK")
                .command("-!k")
                .command("--verbose")
                .failFast(true)
                .run();
    }

    private void downloadNutsJar() {
        NOut.println(NMsg.ofC("downloading %s to %s", NPath.of("https://maven.thevpc.net/net/thevpc/nuts/nuts/" + version + "/nuts-" + version + ".jar"), resolveJarFile()));
        NCp.of().from(NPath.of("https://maven.thevpc.net/net/thevpc/nuts/nuts/" + version + "/nuts-" + version + ".jar"))
                .to(resolveJarFile())
                .run();
    }

}
