package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SPath {
    public String[] elems;

    public static SPath parse(String elem) {
        return new SPath(NStringUtils.split(elem, ".", true, true).toArray(new String[0]));
    }

    public SPath(String[] elems) {
        this.elems = elems;
        if (elems.length < 1) {
            throw new IllegalArgumentException("empty array");
        }
    }

    public SPath parent() {
        if (elems.length == 1) {
            return null;
        }
        return new SPath(Arrays.copyOfRange(elems, 0, elems.length - 1));
    }

    public SPath resolve(String other) {
        String[] a = new String[elems.length + 1];
        System.arraycopy(a, 0, a, 0, elems.length);
        a[elems.length] = other;
        return new SPath(a);
    }

    @Override
    public String toString() {
        return String.join(".", elems);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SPath sPath = (SPath) o;
        return Objects.deepEquals(elems, sPath.elems);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(elems);
    }

    public String name() {
        return elems[elems.length - 1];
    }

    public SPath resolve(SPath path) {
        List<String> all = new ArrayList<>(elems.length + path.elems.length);
        all.addAll(Arrays.asList(elems));
        all.addAll(Arrays.asList(path.elems));
        return new SPath(all.toArray(new String[0]));
    }

    public SPath skipFirst() {
        if (elems.length == 1) {
            return null;
        }
        return new SPath(Arrays.copyOfRange(elems, 1, elems.length));
    }
}
