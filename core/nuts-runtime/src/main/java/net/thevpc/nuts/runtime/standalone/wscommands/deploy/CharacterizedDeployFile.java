package net.thevpc.nuts.runtime.standalone.wscommands.deploy;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.runtime.bundles.io.NutsStreamOrPath;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CharacterizedDeployFile implements AutoCloseable {

    public Path baseFile;
    public NutsStreamOrPath contentStreamOrPath;
    public List<Path> temps = new ArrayList<>();
    public NutsDescriptor descriptor;




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
