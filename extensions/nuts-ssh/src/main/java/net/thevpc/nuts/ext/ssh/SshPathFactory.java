package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.concurrent.NScoredCallable;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.spi.NDefaultScorableContext;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.net.NConnectionString;

public class SshPathFactory implements NPathFactorySPI {
    NWorkspace workspace;

    public SshPathFactory(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NScoredCallable<NPathSPI> createPath(String path, String protocol, ClassLoader classLoader) {
        try{
            if(path.startsWith("ssh:")){
                NConnectionString a= NConnectionString.get(path).orNull();
                if(a!=null) {
                    NScoredCallable<SshConnection> s = ScoredConnectionFactory.resolveBinSshConnectionPool(a);
                    return NScoredCallable.of(s.getScore(new NDefaultScorableContext()), () -> new SshNPath(a));
                }
            }
        }catch (Exception ex){
            //ignore
        }
        return null;
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String path= context.getCriteria();
        if(path.startsWith("ssh:")){
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

}
