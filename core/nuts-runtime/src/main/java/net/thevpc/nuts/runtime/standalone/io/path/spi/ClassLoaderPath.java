package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;

import java.util.Objects;

public class ClassLoaderPath extends URLPath {
    private final String path;
    private final String effectivePath;
    private final ClassLoader loader;
    private final NWorkspace workspace;
    private static String fileOf(String path,boolean check){
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
            throw new NIOException(NMsg.ofC("invalid class path file : %s",path));
        }
        return null;
    }

    public ClassLoaderPath(String path, ClassLoader loader, NWorkspace workspace) {
        super(loader.getResource(fileOf(path,true)), true);
        this.path = path;
        this.effectivePath = fileOf(path,false);
        this.loader = loader;
        this.workspace = workspace;
    }

    @Override
    public String toString() {
        return "classpath:"+effectivePath;
    }

    public String getName(NPath basePath) {
        return URLPath.getURLName(effectivePath);
    }

    @Override
    public String getLocation(NPath basePath) {
        if (url != null) {
            return super.getLocation(basePath);
        }
        return effectivePath;
    }

    @Override
    public String getProtocol(NPath basePath) {
        return "classpath";
    }

    protected NPath rebuildURLPath(String other) {
        return NPath.of(new ClassLoaderPath(other, loader, workspace));
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

    public static class ClasspathFactory implements NPathFactorySPI {
        NWorkspace workspace;

        public ClasspathFactory(NWorkspace workspace) {
            this.workspace = workspace;
        }

        @Override
        public NCallableSupport<NPathSPI> createPath(String path, ClassLoader classLoader) {
            try {
                if (path.startsWith("classpath:")) {
                    return NCallableSupport.of(NConstants.Support.DEFAULT_SUPPORT,()->new ClassLoaderPath(path, classLoader, workspace));
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            String path= context.getConstraints();
            if (path.startsWith("classpath:")) {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
            return NConstants.Support.NO_SUPPORT;
        }
    }
}
