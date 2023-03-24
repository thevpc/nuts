package net.thevpc.nuts.runtime.standalone.format.obj;

import net.thevpc.nuts.io.NPath;

import java.math.BigInteger;
import java.time.Instant;

class PathAndNbr implements Comparable<PathAndNbr> {
    NPath p;
    Instant time;
    BigInteger i;

    public PathAndNbr(NPath p, Instant time, BigInteger i) {
        this.p = p;
        this.i = i;
        this.time = time;
    }

    @Override
    public int compareTo(PathAndNbr o) {
        int e = o.time.compareTo(this.time);
        if (e != 0) {
            return e;
        }
        e = o.i.compareTo(this.i);
        if (e != 0) {
            return e;
        }
        return p.getName().compareTo(o.p.getName());
    }
}
