package net.thevpc.nuts.lib.common.collections;

class ValueAndPos<T> {
    T value;
    int pos;

    public ValueAndPos(T value, int pos) {
        this.value = value;
        this.pos = pos;
    }
}
