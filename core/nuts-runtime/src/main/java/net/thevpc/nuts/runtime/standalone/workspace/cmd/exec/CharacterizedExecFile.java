package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.io.util.NutsStreamOrPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CharacterizedExecFile implements AutoCloseable {

    public Path contentFile;
    public NutsStreamOrPath streamOrPath;
    public List<Path> temps = new ArrayList<>();
    public NutsDescriptor descriptor;
    public NutsId executor;
    public NutsSession session;

    public CharacterizedExecFile(NutsSession session) {
        this.session = session;
    }

    public void addTemp(Path f) {
        temps.add(f);
    }

    @Override
    public void close() {
        for (Iterator<Path> it = temps.iterator(); it.hasNext(); ) {
            Path temp = it.next();
            try {
                Files.delete(temp);
            } catch (IOException ex) {
                throw new NutsIOException(session,ex);
            }
            it.remove();
        }
    }
}
