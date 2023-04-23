package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NPathFactory;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NConnexionString;

public class SshPathFactory implements NPathFactory {
    @Override
    public NSupported<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
        try{
            if(path.startsWith("ssh:")){
                NConnexionString a=NConnexionString.of(path).orNull();
                if(a!=null) {
                    return NSupported.of(3, () -> new SshNPath(a, session));
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
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

}
