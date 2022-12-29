package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.NId;

import java.util.Objects;

public class NIdExt implements Comparable<NIdExt> {

    public NId id;
    public String extra;

    public NIdExt(NId id, String extra) {
        this.id = id;
        this.extra = extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NIdExt nIdExt = (NIdExt) o;
        return Objects.equals(id, nIdExt.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public int compareTo(NIdExt o) {
        int x = this.id.getShortName().compareTo(o.id.getShortName());
        if (x != 0) {
            return x;
        }
        x = -this.id.getVersion().compareTo(o.id.getVersion());
        return x;
    }
}
