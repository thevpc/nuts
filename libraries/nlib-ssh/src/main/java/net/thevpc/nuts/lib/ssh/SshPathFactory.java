package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NPathFactory;
import net.thevpc.nuts.spi.NPathSPI;

public class SshPathFactory implements NPathFactory {
    @Override
    public NSupported<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
        try{
            if(path.startsWith("ssh:")){
                SshPath a=new SshPath(path);
                return NSupported.of(3,()->new SshNPath(a,session));
            }
        }catch (Exception ex){
            //ignore
        }
        return null;
    }

}
