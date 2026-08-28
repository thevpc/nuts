package net.thevpc.nuts.runtime.standalone.collections;

public class NValueAndPos<T> {
    T value;
    int pos;

    /**
     * N value and pos.
     *
     * @param value value
     * @param pos pos
     * @return n value and pos result
     */
    public NValueAndPos(T value, int pos) {
        this.value = value;
        this.pos = pos;
    }
}
