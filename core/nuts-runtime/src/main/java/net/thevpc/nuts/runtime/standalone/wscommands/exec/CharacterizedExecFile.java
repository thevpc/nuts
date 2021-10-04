package net.thevpc.nuts.runtime.standalone.wscommands.exec;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.runtime.bundles.io.NutsStreamOrPath;

import java.io.IOException;
import java.io.UncheckedIOException;
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
                throw new UncheckedIOException(ex);
            }
            it.remove();
        }
    }
}
