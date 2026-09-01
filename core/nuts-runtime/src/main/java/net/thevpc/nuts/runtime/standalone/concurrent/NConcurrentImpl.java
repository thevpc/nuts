package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NConcurrentImpl implements NConcurrent {

    @Override
    public ExecutorService executorService() {
        return NWorkspaceExt.of().getModel().configModel.executorService();
    }


    @Override
    public NConcurrent sleep(NDuration durationMillis) throws NInterruptedException {
        return sleep(durationMillis == null ? 0 : durationMillis.toMillis());
    }

    @Override
    public NConcurrent sleep(Duration durationMillis) throws NInterruptedException {
        return sleep(durationMillis == null ? 0 : durationMillis.toMillis());
    }

    @Override
    public NConcurrent sleep(long durationMillis) throws NInterruptedException {
        if (durationMillis > 0) {
            try {
                Thread.sleep(durationMillis);
            } catch (InterruptedException e) {
                throw new NInterruptedException(e);
            }
        }
        return this;
    }





}
