package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NTaskSet;
import net.thevpc.nuts.internal.rpi.NConcurrentRPI;

public class DefaultNConcurrentRPI implements NConcurrentRPI {

    @Override
    public NTaskSet taskSet() {
        return new NTaskSetImpl();
    }
}
