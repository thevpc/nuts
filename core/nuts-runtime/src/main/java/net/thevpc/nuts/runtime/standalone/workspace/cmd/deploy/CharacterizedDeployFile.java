package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsInputSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CharacterizedDeployFile implements AutoCloseable {

    private Path baseFile;
    private NutsInputSource contentStreamOrPath;
    private List<Path> temps = new ArrayList<>();
    private NutsDescriptor descriptor;
    private NutsSession session;

    public CharacterizedDeployFile(NutsSession session) {
        this.session = session;
    }

    public void addTemp(Path f) {
        temps.add(f);
    }

    public Path getBaseFile() {
        return baseFile;
    }

    public CharacterizedDeployFile setBaseFile(Path baseFile) {
        this.baseFile = baseFile;
        return this;
    }

    public NutsInputSource getContentStreamOrPath() {
        return contentStreamOrPath;
    }

    public CharacterizedDeployFile setContentStreamOrPath(NutsInputSource contentStreamOrPath) {
        this.contentStreamOrPath = contentStreamOrPath;
        return this;
    }

    public List<Path> getTemps() {
        return temps;
    }

    public CharacterizedDeployFile setTemps(List<Path> temps) {
        this.temps = temps;
        return this;
    }

    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    public CharacterizedDeployFile setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public CharacterizedDeployFile setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public void close() {
        for (Iterator<Path> it = temps.iterator(); it.hasNext(); ) {
            Path temp = it.next();
            try {
                Files.delete(temp);
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
            it.remove();
        }
    }

}
