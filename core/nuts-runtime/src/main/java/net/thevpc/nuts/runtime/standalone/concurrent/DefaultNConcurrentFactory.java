package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.concurrent.NCachedValue;
import net.thevpc.nuts.concurrent.NConcurrentFactory;
import net.thevpc.nuts.concurrent.NStableValue;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.function.Supplier;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNConcurrentFactory implements NConcurrentFactory {

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public <T> NCachedValue<T> createCachedValue(Supplier<T> supplier) {
        return NCachedValueImpl.of(supplier);
    }

    @Override
    public <T> NStableValue<T> createStableValue(Supplier<T> supplier) {
        return NStableValueImpl.of(supplier);
    }
}
