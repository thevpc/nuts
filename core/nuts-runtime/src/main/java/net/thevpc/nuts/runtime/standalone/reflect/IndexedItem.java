package net.thevpc.nuts.runtime.standalone.reflect;

class IndexedItem<T> {
    public int index;
    public T item;

    public IndexedItem(int index, T item) {
        this.index = index;
        this.item = item;
    }

    @Override
    public String toString() {
        return "IndexedItem[" + index + "]{" +
                item +
                '}';
    }
}
