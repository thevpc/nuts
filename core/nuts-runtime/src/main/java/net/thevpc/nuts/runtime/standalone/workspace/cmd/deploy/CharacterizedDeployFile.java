package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CharacterizedDeployFile implements AutoCloseable {

    private Path baseFile;
    private NInputSource contentStreamOrPath;
    private List<Path> temps = new ArrayList<>();
    private NDescriptor descriptor;

    public CharacterizedDeployFile() {
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

    public NInputSource getContentStreamOrPath() {
        return contentStreamOrPath;
    }

    public CharacterizedDeployFile setContentStreamOrPath(NInputSource contentStreamOrPath) {
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

    public NDescriptor getDescriptor() {
        return descriptor;
    }

    public CharacterizedDeployFile setDescriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
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
