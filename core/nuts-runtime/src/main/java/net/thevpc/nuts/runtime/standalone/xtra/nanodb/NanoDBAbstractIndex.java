package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

public abstract class NanoDBAbstractIndex<T> implements NanoDBIndex<T> {
    NanoDBSerializer<T> ser;

    public NanoDBAbstractIndex(NanoDBSerializer<T> ser) {
        this.ser = ser;
    }

}