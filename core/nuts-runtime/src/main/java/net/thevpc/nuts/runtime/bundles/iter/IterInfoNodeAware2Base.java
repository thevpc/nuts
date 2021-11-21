package net.thevpc.nuts.runtime.bundles.iter;

public abstract class IterInfoNodeAware2Base<T> implements IterInfoNodeAware2<T>{
    protected IterInfoNode attachedInfo;

    protected IterInfoNode info(String type,IterInfoNode... extra) {
        return new IterInfoNode(attachedInfo,extra).withType(type);
    }

    @Override
    public void attachInfo(IterInfoNode nfo) {
        this.attachedInfo=nfo;
    }

}
