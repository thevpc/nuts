package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NSagaBuilder;
import net.thevpc.nuts.concurrent.NSagaFactory;
import net.thevpc.nuts.concurrent.NSagaStore;
import net.thevpc.nuts.reflect.NBeanContainer;

public class NSagaFactoryImpl implements NSagaFactory {
    private NSagaStore store;
    private NBeanContainer beanContainer;

    public NSagaFactoryImpl(NSagaStore store, NBeanContainer beanContainer) {
        this.store = store;
        this.beanContainer = beanContainer;
    }

    @Override
    public NBeanContainer getBeanContainer() {
        return beanContainer;
    }

    @Override
    public NSagaFactory withBeanContainer(NBeanContainer container) {
        if(container==this.beanContainer){
            return this;
        }
        return new NSagaFactoryImpl(store, container);
    }

    @Override
    public NSagaStore getStore() {
        return store;
    }

    @Override
    public NSagaFactory withStore(NSagaStore store) {
        if(store==this.store){
            return this;
        }
        return new  NSagaFactoryImpl(store, beanContainer);
    }

    @Override
    public NSagaBuilder of() {
        return new NSagaBuilderImpl(store, beanContainer);
    }
}
