package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsInputSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CharacterizedExecFile implements AutoCloseable {

    private Path contentFile;
    private NutsInputSource streamOrPath;
    private List<Path> temps = new ArrayList<>();
    private NutsDescriptor descriptor;
    private NutsId executor;
    private NutsSession session;

    public CharacterizedExecFile(NutsSession session) {
        this.session = session;
    }

    public void addTemp(Path f) {
        temps.add(f);
    }

    public Path getContentFile() {
        return contentFile;
    }

    public CharacterizedExecFile setContentFile(Path contentFile) {
        this.contentFile = contentFile;
        return this;
    }

    public NutsInputSource getStreamOrPath() {
        return streamOrPath;
    }

    public CharacterizedExecFile setStreamOrPath(NutsInputSource streamOrPath) {
        this.streamOrPath = streamOrPath;
        return this;
    }

    public List<Path> getTemps() {
        return temps;
    }

    public CharacterizedExecFile setTemps(List<Path> temps) {
        this.temps = temps;
        return this;
    }

    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    public CharacterizedExecFile setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public NutsId getExecutor() {
        return executor;
    }

    public CharacterizedExecFile setExecutor(NutsId executor) {
        this.executor = executor;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public CharacterizedExecFile setSession(NutsSession session) {
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
                throw new NutsIOException(session,ex);
            }
            it.remove();
        }
    }
}
