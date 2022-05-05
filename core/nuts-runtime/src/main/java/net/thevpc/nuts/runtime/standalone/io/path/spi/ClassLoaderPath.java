package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.util.Objects;

public class ClassLoaderPath extends URLPath {
    private final String path;
    private final String effectivePath;
    private final ClassLoader loader;
    private final static String fileOf(String path,boolean check, NutsSession session){
        if(path!=null){
            if(path.startsWith("classpath:")){
                String p=path;
                p=p.substring("classpath:".length());
                while(p.startsWith("/")){
                    p=p.substring(1);
                }
                return p;
            }
        }
        if(check){
            throw new NutsIOException(session, NutsMessage.ofCstyle("invalid class path file : %s",path));
        }
        return null;
    }

    public ClassLoaderPath(String path, ClassLoader loader, NutsSession session) {
        super(loader.getResource(fileOf(path,true,session)), session, true);
        this.path = path;
        this.effectivePath = fileOf(path,false,session);
        this.loader = loader;
    }

    @Override
    public String toString() {
        return "classpath:"+effectivePath;
    }

    public String getName(NutsPath basePath) {
        return URLPath.getURLName(effectivePath);
    }

    @Override
    public String getLocation(NutsPath basePath) {
        if (url != null) {
            return super.getLocation(basePath);
        }
        return effectivePath;
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return "classpath";
    }

    protected NutsPath rebuildURLPath(String other) {
        return NutsPath.of(new ClassLoaderPath(other, loader, getSession()),getSession());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;
        ClassLoaderPath that = (ClassLoaderPath) o;
        return Objects.equals(effectivePath, that.effectivePath) && Objects.equals(loader, that.loader);
    }

    @Override
    public int hashCode() {
//        return Objects.hash(super.hashCode(), path, loader);
        return Objects.hash(effectivePath, loader);
    }

    public static class ClasspathFactory implements NutsPathFactory {
        NutsWorkspace ws;

        public ClasspathFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsSessionUtils.checkSession(ws, session);
            try {
                if (path.startsWith("classpath:")) {
                    return NutsSupported.of(10,()->new ClassLoaderPath(path, classLoader, session));
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }
}
