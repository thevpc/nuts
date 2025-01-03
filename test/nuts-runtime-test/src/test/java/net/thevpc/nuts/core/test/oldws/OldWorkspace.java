package net.thevpc.nuts.core.test.oldws;

import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.NExecutionType;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.core.test.Test06_UpdateTest;
import net.thevpc.nuts.core.test.oldws.impl.OldWorkspace083;
import net.thevpc.nuts.core.test.oldws.impl.OldWorkspace084;
import net.thevpc.nuts.core.test.oldws.impl.OldWorkspace085;
import net.thevpc.nuts.util.NIOUtils;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.io.IOException;

public abstract class OldWorkspace {
    protected File ws;
    protected String version;

    public static OldWorkspace ofTest(String version, String baseFolder) {
        switch (version) {
            case "0.8.3":
                return new OldWorkspace083(new File(baseFolder, version));
            case "0.8.4":
                return new OldWorkspace084(new File(baseFolder, version));
            case "0.8.5":
                return new OldWorkspace085(new File(baseFolder, version));
        }
        throw new IllegalArgumentException("unsupported version " + version);
    }

    public OldWorkspace(File ws, String version) {
        this.ws = ws;
        this.version = version;
    }

    public void reset() {
        NSession.of().out().println("reset workspace " + ws);
        NIOUtils.delete(ws);
    }

    protected String resolveJavaFile() {
        return resolveJavaFile(null);
    }

    protected String resolveJavaFile(String version) {
        return "java";
    }

    protected File resolveJarFile() {
        File file = new File(ws + "/..", "nuts-" + version + ".jar");
        try {
            return file.getCanonicalFile().getAbsoluteFile();
        } catch (IOException e) {
            return file.getAbsoluteFile();
        }
    }

    public abstract void installWs();

    public abstract void upgrade();

    public void showVersion() {
        NSession.of().out().println(NMsg.ofC("show workspace version %s", version));
        NExecCmd.of().setExecutionType(NExecutionType.SYSTEM)
                .addCommand(resolveJavaFile(), "-jar", resolveJarFile().getPath())
                .addCommand("version")
                .failFast()
                .run();
    }

}
