package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CharacterizedExecFile implements AutoCloseable {

    private Path contentFile;
    private NInputSource streamOrPath;
    private List<Path> temps = new ArrayList<>();
    private NDescriptor descriptor;
    private NId executor;

    public CharacterizedExecFile() {
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

    public NInputSource getStreamOrPath() {
        return streamOrPath;
    }

    public CharacterizedExecFile setStreamOrPath(NInputSource streamOrPath) {
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

    public NDescriptor getDescriptor() {
        return descriptor;
    }

    public CharacterizedExecFile setDescriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public NId getExecutor() {
        return executor;
    }

    public CharacterizedExecFile setExecutor(NId executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public void close() {
        for (Iterator<Path> it = temps.iterator(); it.hasNext(); ) {
            Path temp = it.next();
            try {
                Files.delete(temp);
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
            it.remove();
        }
    }
}
