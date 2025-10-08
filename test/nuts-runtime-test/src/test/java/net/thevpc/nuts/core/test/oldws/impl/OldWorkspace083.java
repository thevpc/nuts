package net.thevpc.nuts.core.test.oldws.impl;

import net.thevpc.nuts.command.NExecCmd;
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
        NExecCmd.of().setExecutionType(NExecutionType.SYSTEM)
                .addCommand(resolveJavaFile(), "-jar", resolveJarFile().getPath())
                .addCommand("--workspace=" + workspaceLocation)
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
                //disable creating of bashrc, etc...
                .addCommand("--workspace=" + workspaceLocation)
                .addCommand("--!switch")
                //disable progress indicator
//                    .addCommand("--!progress")
                //disable interactive mode and 'always confirm'
                .addCommand("--yes")
                //enable installing nsh
                .addCommand("--skip-welcome")
                .addCommand("-r=+thevpc,maven-central")
                .addCommand("-w", ws.getAbsolutePath())
                .addCommand("-byZSK")
                .addCommand("-!k")
                .addCommand("--verbose")
                .failFast()
                .run();
    }

    private void downloadNutsJar() {
        NOut.println(NMsg.ofC("downloading %s to %s", NPath.of("https://maven.thevpc.net/net/thevpc/nuts/nuts/" + version + "/nuts-" + version + ".jar"), resolveJarFile()));
        NCp.of().from(NPath.of("https://maven.thevpc.net/net/thevpc/nuts/nuts/" + version + "/nuts-" + version + ".jar"))
                .to(resolveJarFile())
                .run();
    }

}
