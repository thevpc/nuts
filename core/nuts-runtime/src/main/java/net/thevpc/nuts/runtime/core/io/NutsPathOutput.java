//package net.thevpc.nuts.runtime.core.io;
//
//import net.thevpc.nuts.NutsPath;
//import net.thevpc.nuts.NutsSession;
//import net.thevpc.nuts.runtime.bundles.io.AbstractNutsOutput;
//
//import java.net.URL;
//import java.nio.file.Path;
//
//public abstract class NutsPathOutput extends AbstractNutsOutput {
//    public NutsPathOutput(String name, NutsPath target, NutsSession session) {
//        super(target, target.isFilePath(), target.isURL(), name == null ? target.getName() : name, "nutsPath", session);
//    }
//
//    @Override
//    public Path getFilePath() {
//        return getNutsPath().toFilePath();
//    }
//
//    private NutsPath getNutsPath() {
//        return (NutsPath) getSource();
//    }
//
//    @Override
//    public URL getURL() {
//        return (getNutsPath()).toURL();
//    }
//
//    @Override
//    public void close() {
//
//    }
//
//    @Override
//    public String toString() {
//        return getNutsPath().toString();
//    }
//}
