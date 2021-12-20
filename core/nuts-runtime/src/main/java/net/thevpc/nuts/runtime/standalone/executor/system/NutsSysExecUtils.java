package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.io.util.NonBlockingInputStream;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NutsSysExecUtils {
    public static Path sysWhich(String commandName) {
        Path[] p = sysWhichAll(commandName);
        if (p.length > 0) {
            return p[0];
        }
        return null;
    }

    public static Path[] sysWhichAll(String commandName) {
        if (commandName == null || commandName.isEmpty()) {
            return new Path[0];
        }
        List<Path> all = new ArrayList<>();
        String p = System.getenv("PATH");
        if (p != null) {
            for (String s : p.split(File.pathSeparator)) {
                try {
                    if (!s.trim().isEmpty()) {
                        Path c = Paths.get(s, commandName);
                        if (Files.isRegularFile(c)) {
                            if (Files.isExecutable(c)) {
                                all.add(c);
                            }
                        }
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new Path[0]);
    }

    public static PipeRunnable pipe(String name, String cmd, String desc, final NonBlockingInputStream in, final OutputStream out, NutsSession session) {
        return new PipeRunnable(name, cmd, desc, in, out, true, session);
    }
}
