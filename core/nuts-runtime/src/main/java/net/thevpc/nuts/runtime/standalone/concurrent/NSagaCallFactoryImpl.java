package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NSagaBuilder;
import net.thevpc.nuts.concurrent.NSagaCallFactory;
import net.thevpc.nuts.concurrent.NSagaCallStore;
import net.thevpc.nuts.reflect.NBeanContainer;

public class NSagaCallFactoryImpl implements NSagaCallFactory {
    private NSagaCallStore store;
    private NBeanContainer beanContainer;

    public NSagaCallFactoryImpl(NSagaCallStore store, NBeanContainer beanContainer) {
        this.store = store;
        this.beanContainer = beanContainer;
    }

    @Override
    public NBeanContainer getBeanContainer() {
        return beanContainer;
    }

    @Override
    public NSagaCallFactory withBeanContainer(NBeanContainer container) {
        if(container==this.beanContainer){
            return this;
        }
        return new NSagaCallFactoryImpl(store, container);
    }

    @Override
    public NSagaCallStore getStore() {
        return store;
    }

    @Override
    public NSagaCallFactory withStore(NSagaCallStore store) {
        if(store==this.store){
            return this;
        }
        return new NSagaCallFactoryImpl(store, beanContainer);
    }

    @Override
    public NSagaBuilder of() {
        return new NSagaBuilderImpl(store, beanContainer);
    }
}
