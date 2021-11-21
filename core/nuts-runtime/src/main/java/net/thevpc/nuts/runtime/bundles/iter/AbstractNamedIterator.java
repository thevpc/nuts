package net.thevpc.nuts.runtime.bundles.iter;

public abstract class AbstractNamedIterator<T> extends IterInfoNodeAware2Base<T> {
    protected String name;

    public AbstractNamedIterator(String name) {
        this.name = name;
    }

    @Override
    protected IterInfoNode info(String type, IterInfoNode... extra) {
        IterInfoNode nfo = super.info(type, extra);
        return name==null?nfo:nfo.withName(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
