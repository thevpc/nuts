package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NSagaCallableBuilder;
import net.thevpc.nuts.concurrent.NSagaCallableFactory;
import net.thevpc.nuts.concurrent.NSagaStore;
import net.thevpc.nuts.reflect.NBeanContainer;

public class NSagaCallableFactoryImpl implements NSagaCallableFactory {
    private NSagaStore store;

    public NSagaCallableFactoryImpl(NSagaStore store) {
        this.store = store;
    }

    @Override
    public NSagaStore getStore() {
        return store;
    }

    @Override
    public NSagaCallableFactory withStore(NSagaStore store) {
        if(store==this.store){
            return this;
        }
        return new NSagaCallableFactoryImpl(store);
    }

    @Override
    public NSagaCallableBuilder ofBuilder() {
        return new NSagaCallableBuilderImpl(store);
    }
}
