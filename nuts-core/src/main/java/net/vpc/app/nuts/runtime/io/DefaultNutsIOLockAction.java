package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.NutsLock;
import net.vpc.app.nuts.NutsLockException;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultNutsIOLockAction extends AbstractNutsIOLockAction {
    public DefaultNutsIOLockAction(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsLock create() {
        Object s = getSource();
        Object lr = getLockResource();
        Path lrPath=null;
        if(lr==null){
            if(s==null){
                throw new NutsLockException(getWs(), "Unsupported lock for null", null, null);
            }
            Path p = toPath(s);
            if(p==null){
                throw new NutsLockException(getWs(), "Unsupported lock for " + s.getClass().getName(), null, s);
            }
            lrPath= p.resolveSibling(p.getFileName().toString() + ".lock");
        }else{
            lrPath=toPath(lr);
            if(lrPath==null){
                throw new NutsLockException(getWs(), "Unsupported lock " + lr.getClass().getName(), lr, s);
            }
        }
        return new DefaultFileNutsLock(lrPath, s, getWs());
    }

    private Path toPath(Object lockedObject) {
        if (lockedObject instanceof Path) {
            return (Path) lockedObject;
        } else if (lockedObject instanceof File) {
            return ((File) lockedObject).toPath();
        } else if (lockedObject instanceof String) {
            return Paths.get((String) lockedObject);
        }
        return null;
    }
}
