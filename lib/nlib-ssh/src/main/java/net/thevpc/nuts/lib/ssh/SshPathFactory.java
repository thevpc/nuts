package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;

public class SshPathFactory implements NutsPathFactory {
    @Override
    public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
        try{
            if(path.startsWith("ssh:")){
                SshPath a=new SshPath(path);
                return NutsSupported.of(3,()->new SshNutsPath(a,session));
            }
        }catch (Exception ex){
            //ignore
        }
        return null;
    }

}
