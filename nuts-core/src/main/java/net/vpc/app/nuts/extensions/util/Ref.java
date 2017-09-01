package net.vpc.app.nuts.extensions.util;

public class Ref<T> {
    private T value;

    public Ref() {
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
    public boolean isSet(){
        return value!=null;
    }
}
