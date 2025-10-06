package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NSagaCallableBuilder;
import net.thevpc.nuts.concurrent.NSagaCallableFactory;
import net.thevpc.nuts.concurrent.NSagaStore;
import net.thevpc.nuts.reflect.NBeanContainer;

public class NSagaCallableFactoryImpl implements NSagaCallableFactory {
    private NSagaStore store;
    private NBeanContainer beanContainer;

    public NSagaCallableFactoryImpl(NSagaStore store, NBeanContainer beanContainer) {
        this.store = store;
        this.beanContainer = beanContainer;
    }

    @Override
    public NBeanContainer getBeanContainer() {
        return beanContainer;
    }

    @Override
    public NSagaCallableFactory withBeanContainer(NBeanContainer container) {
        if(container==this.beanContainer){
            return this;
        }
        return new NSagaCallableFactoryImpl(store, container);
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
        return new NSagaCallableFactoryImpl(store, beanContainer);
    }

    @Override
    public NSagaCallableBuilder ofBuilder() {
        return new NSagaCallableBuilderImpl(store, beanContainer);
    }
}
