package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.concurrent.NScorableCallable;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.net.NConnexionString;

public class SshPathFactory implements NPathFactorySPI {
    NWorkspace workspace;

    public SshPathFactory(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NScorableCallable<NPathSPI> createPath(String path, String protocol, ClassLoader classLoader) {
        try{
            if(path.startsWith("ssh:")){
                NConnexionString a= NConnexionString.get(path).orNull();
                if(a!=null) {
                    return NScorableCallable.of(3, () -> new SshNPath(a));
                }
            }
        }catch (Exception ex){
            //ignore
        }
        return null;
    }

    @Override
    public int getScore(NScorableContext context) {
        String path= context.getCriteria();
        if(path.startsWith("ssh:")){
            return DEFAULT_SCORE;
        }
        return UNSUPPORTED_SCORE;
    }

}
