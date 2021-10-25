package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;

public class SshPathFactory implements NutsPathFactory {
    @Override
    public NutsSupplier<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
        try{
            if(path.startsWith("ssh:")){
                SshPath a=new SshPath(path);
                return new NutsSupplier<NutsPathSPI>() {
                    @Override
                    public int getLevel() {
                        return 3;
                    }

                    @Override
                    public NutsPathSPI create() {
                        return new SshNutsPath(a,session);
                    }
                };
            }
        }catch (Exception ex){
            //ignore
        }
        return null;
    }

}
