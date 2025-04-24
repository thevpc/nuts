package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.DefaultNConnexionString;
import net.thevpc.nuts.util.DefaultNConnexionStringBuilder;
import net.thevpc.nuts.util.NConnexionString;

public class SshPathFactory implements NPathFactorySPI {
    NWorkspace workspace;

    public SshPathFactory(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NCallableSupport<NPathSPI> createPath(String path, String protocol, ClassLoader classLoader) {
        try{
            if(path.startsWith("ssh:")){
                NConnexionString a= NConnexionString.get(path).orNull();
                if(a!=null) {
                    return NCallableSupport.of(3, () -> new SshNPath(a));
                }
            }
        }catch (Exception ex){
            //ignore
        }
        return null;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String path= context.getConstraints();
        if(path.startsWith("ssh:")){
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        return NConstants.Support.NO_SUPPORT;
    }

}
